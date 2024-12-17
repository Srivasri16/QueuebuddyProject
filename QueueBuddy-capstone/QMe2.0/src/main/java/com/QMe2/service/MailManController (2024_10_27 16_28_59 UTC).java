package com.QMe2.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.QMe2.SMSClasses.SmsSender;
import com.QMe2.bean.Business;
import com.QMe2.bean.BusinessPhoneNumber;
import com.QMe2.bean.LineManager;
import com.QMe2.bean.Queue;
import com.QMe2.bean.Queuer;
import com.QMe2.bean.SMSTracker;

import com.QMe2.bean.SessionHandlerControl;
import com.QMe2.enums.SMSTypeNotification;
import com.QMe2.helpers.QueueHelper;
import com.QMe2.helpers.RespondText;
import com.QMe2.helpers.SessionHandlerBean;
import com.twilio.exception.ApiException;

@Component
public class MailManController {
	@Autowired
	private SessionHandlerControl sessionHandl;
	
	@Autowired
	private RepoController rc;
	
	@Autowired
	private JavaMailSender javaMailSender;
	
	@Autowired
	private QueueHelper lengthOfQueue;
	
	public MailManController() {
		
	}
	
	public void messageCurrentClient(Queuer queuer) throws IOException {
		String message = RespondText.clientCalled(queuer.getId());
		if(queuer.getSessionId() != null ) {
			WebSocketSession session = sessionHandl.getSession(queuer.getSessionId());
			if(session != null && session.isOpen()) {
				session.sendMessage(new TextMessage("{\"requestType\": 359, \"command\": \""+message+"\"}"));
			}
		}
		
		if(queuer.getPhoneNum() != null) {
			String txtMessage = RespondText.clientCalledTxt(queuer);
			System.out.println(txtMessage);
			sendSMS(queuer, txtMessage);
		}
		
		if(queuer.getEmail() != null) {
			String txtMessage = RespondText.clientCalledTxt(queuer);
			sendEmailAsync(queuer.getEmail(), queuer.getQueue().getBussiness().getNameOfStore() + "- UPDATE AVAILABLE", txtMessage);
		}
	}
	
	
	public void brodcastUpdate(Queuer queuer) throws IOException {
		
		
		List<Queuer> queuerList = lengthOfQueue.getQueuersInLine(queuer.getQueue());
		
		int count = 0;
		for(Queuer tmp : queuerList) {
			
			if(tmp.getSessionId() != null) {
				WebSocketSession session = sessionHandl.getSession(tmp.getSessionId());
				if(session != null && session.isOpen()) {
					updateUser(session, tmp);
				}
			}
			
			if(count++ == 0) {
				int position = lengthOfQueue.getStatus(tmp);
				String nameOfBusiness =tmp.getQueue().getBussiness().getNameOfStore();

				String messagetmp = RespondText.nextClientSMS(nameOfBusiness);

				
				if(tmp.getPhoneNum()!= null) {
					
					//Message if client is Paused
					if(!tmp.isPaused()) {
						sendSMS(tmp, messagetmp);
						
						//message first time it let someone go first
					} else if(tmp.isPaused() && tmp.getTimesPaused() == 1) {
						messagetmp = RespondText.clientUpadateTextPause(tmp,position,nameOfBusiness, SMSTypeNotification.REGULAR);
						sendSMS(tmp, messagetmp);
						
						//message when it's the last time letting someone go first
					} else if(tmp.isPaused() && tmp.getTimesPaused() == 6) {
						messagetmp = RespondText.clientUpadateTextPause(tmp, position, nameOfBusiness, SMSTypeNotification.LASTWARNING);
						sendSMS(tmp, messagetmp);
					}
					else if(tmp.isPaused() && queuerList.size() == 1) {
						messagetmp = RespondText.clientUpadateTextPause(tmp, position, nameOfBusiness, SMSTypeNotification.LASTONE);
						sendSMS(tmp, messagetmp);
					}
					
				}
				if(tmp.getEmail() != null) {
					sendEmailAsync(tmp.getEmail(), nameOfBusiness + "- UPDATE AVAILABLE", messagetmp);
				}
			}
			
		}
		updateLineManager(queuer);
		
		
		
	}
	
	
	
	public void updateLineManager(Queuer queuer) throws IOException {
		LineManager lineManager = queuer.getQueue().getBussiness().getLineManager();
		if(lineManager != null) {
			if(lineManager.getSessionID() != null) {
				WebSocketSession session = sessionHandl.getSession(lineManager.getSessionID());
				if(session != null && session.isOpen()) {
					sendUpdateQueueSize(session,  queuer.getQueue());
					Long size = lengthOfQueue.getQueuersInLineSize(queuer.getQueue());
					System.out.println(size);
					if(size == 0)
						session.sendMessage(new TextMessage("{\"requestType\": 735, \"command\": \""+RespondText.bussinessUpdate(size)+"\"}"));
					
					//sendUpdateQueueSize(session, queuer.getQueue());
				}
			}
		}
		
		
		//SMS alert line is not empty any more
		/*if(queuer.getQueue().length() == 1 && queuer.getQueue().getBussiness().getBusinessPhoneNumber().size() != 0) {
			for(BusinessPhoneNumber phoneNum : queuer.getQueue().getBussiness().getBusinessPhoneNumber()) {
				String messId = SmsSender.sendText(phoneNum.getPhoneNumber(), "You got a client waiting!");
				rc.getSmsTrackerRepo().saveAndFlush(SMSTracker.builder().business(queuer.getQueue().getBussiness()).messageSID(messId).build());
			}
		}*/
	}
	
	public void notifyLineNotEmpty(Business business) {
		for(BusinessPhoneNumber phoneNum : business.getBusinessPhoneNumber()) {
			try {
				String messId = SmsSender.sendText(phoneNum.getPhoneNumber(), "You got a client waiting!");
				rc.getSmsTrackerRepo().saveAndFlush(SMSTracker.builder().business(business).messageSID(messId).build());
			}
			catch(ApiException e) {
				System.out.println(phoneNum.getPhoneNumber() + " is blacklisted");
			}
			
		}
	}
	
	public void sendUpdateQueueSize(WebSocketSession session, Queue queue) throws IOException {
		session.sendMessage(new TextMessage("{\"requestType\": 845, \"command\": \""+ lengthOfQueue.getQueuersInLineSize(queue)+" People\"}"));

	}
	
	public void updateUser(WebSocketSession thisSession, Queuer user) throws IOException {
		//Queuer user = rcon.getQueuerRepo().findBySessionId(index.getId());
		String messagetmp = "";
		if(user == null) {
			messagetmp = RespondText.clientNoLineAv();
			
			return;
		}
		
		Queue queueFound = user.getQueue();
		int position = lengthOfQueue.getStatus(user);
		
		try {
			
			if(!user.isPaused()) {
				messagetmp = RespondText.clientUpdateWebApp(queueFound.getBussiness().getNameOfStore(), position);
			} else {
				messagetmp = RespondText.cleintUpdateWebAppOnPause(user, position,lengthOfQueue.getQueuersInLineSize(queueFound), queueFound.getBussiness().getNameOfStore());

			}
			
			
		} catch(Exception ex) {
			messagetmp = "Sorry there was an error, please try again later";
		}
		thisSession.sendMessage(new TextMessage("{\"requestType\": 359, \"command\": \""+messagetmp+"\"}"));
		
		if(queueFound.getAverageEntryTime() != null)
		{
			thisSession.sendMessage(new TextMessage("{\"requestType\": 998, \"command\": \""+ queueFound.getAverageEntryTime() +"\"}"));
		}
		
		
		/*
		 * Send message (as above)
		 * Code 998
		 * 
		 * send user.getLogEntry().getLog().getAverageEntryTime(); (if not null)
		 * 
		 * */
		//thisSession.sendMessage(new TextMessage(messagetmp));
	}
	
	public void updateUserCancel(List<Queuer> queuerList) throws IOException {
		int count = 0;
		String message = "Sorry the line has closed";
		for(Queuer tmp : queuerList) {
			String nameOfBusiness =tmp.getQueue().getBussiness().getNameOfStore();
			if(count++ == 0) {
				message = RespondText.cancelMessage(nameOfBusiness);
			}
			
			
			if(tmp.getSessionId() != null) {
				WebSocketSession session = sessionHandl.getSession(tmp.getSessionId());
				if(session != null && session.isOpen()) {
					session.sendMessage(new TextMessage("{\"requestType\": 999}"));
					
					session.sendMessage(new TextMessage("{\"requestType\": 359, \"command\": \""+message+"\"}"));
	    			sessionHandl.removeSession(tmp.getSessionId());
				}
			}

			if(tmp.getPhoneNum() != null) {
				sendSMS(tmp, message);
			}
			
			if(tmp.getEmail() != null) {
				sendEmailAsync(tmp.getEmail(), nameOfBusiness + "- UPDATE AVAILABLE", message);
			}
		}
	}

	public void sendSMS(Queuer q, String message) {
		try {
			String messId = SmsSender.sendText(q.getPhoneNum(), message);
			rc.getSmsTrackerRepo().saveAndFlush(SMSTracker.builder().business(q.getQueue().getBussiness()).messageSID(messId).build());
		//System.out.println("SMS sent: Message:" + message);
		} catch(com.twilio.exception.ApiException e) {
			System.out.println(q.getPhoneNum() + " Is blacklisted");
		}
		catch(Exception e) {
			System.out.println(e.toString());
		}
	}
	
	
	
	
	private void sendEmail(String to, String subject, String text) {
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setFrom("non-reply@gmail.com");
		msg.setTo(to);
		msg.setSubject(subject);
		msg.setText(text);
		
		
		
			
		
		try {
			
			javaMailSender.send(msg);
			System.out.println("Message sent");
		} catch(Exception ex) {
			System.out.println(ex.toString());
			
		}
		
	}
	
	private CompletableFuture<Void> sendEmailAsync(String to, String subject, String text) {
		CompletableFuture<SimpleMailMessage> mailMessageFuture = CompletableFuture.supplyAsync(() -> {
			SimpleMailMessage msg = new SimpleMailMessage();
			msg.setFrom("non-reply@gmail.com");
			msg.setTo(to);
			msg.setSubject(subject);
			msg.setText(text);
			return msg;
			
			
		});
		CompletableFuture<Void> sendMailFuture = mailMessageFuture.thenAcceptAsync(msg -> {
			try {
				javaMailSender.send(msg);
			}
			catch(Exception ex) {
				System.out.println(ex.toString());
			}
			
			
		});
		return sendMailFuture;
		
		
		
		
	}
	
	
	
}
