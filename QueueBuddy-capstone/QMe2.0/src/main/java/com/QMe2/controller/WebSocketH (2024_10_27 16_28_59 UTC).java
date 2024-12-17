package com.QMe2.controller;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;


import com.QMe2.bean.Business;
import com.QMe2.bean.BusinessPhoneNumber;
import com.QMe2.bean.LineManager;
import com.QMe2.bean.Log;
import com.QMe2.bean.LogEntry;
import com.QMe2.bean.Queue;
import com.QMe2.bean.Queuer;

import com.QMe2.bean.SessionHandlerControl;
import com.QMe2.bean.WebAppRequestMess;
import com.QMe2.enums.QueuerStatus;
import com.QMe2.exceptions.NoScheduleAvailable;
import com.QMe2.exceptions.StoreIsClosed;
import com.QMe2.helpers.QueueHelper;
import com.QMe2.helpers.RespondText;
import com.QMe2.helpers.SessionHandlerBean;
import com.QMe2.repo.BusinessRepo;
import com.QMe2.service.MailManController;
import com.QMe2.service.RepoController;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import lombok.AllArgsConstructor;
import at.favre.lib.crypto.bcrypt.*;

@Component
public class WebSocketH extends AbstractWebSocketHandler{
	@Autowired
	private RepoController rcon;
	
	@Autowired
	private SessionHandlerControl sessionHandl;

	@Autowired
	private MailManController mailMan;
	
	@Autowired
	private QueueHelper qH;
	
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException, NoScheduleAvailable {
		Gson gson = new Gson();
		try {
	
			if(message.getPayload().length() >= 5) {
				WebAppRequestMess entity = gson.fromJson(message.getPayload(), WebAppRequestMess.class);
				//System.out.println(entity);
				if(entity.isBusinessRequest() == false) {
					processClientRequest( entity, session);
				} else {
					processBusinessRequest(entity, session);
				}
				
			}
			
			
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
		}
		
	    
	    
	}
	
	private void processBusinessRequest(WebAppRequestMess message, WebSocketSession session) throws IOException, NoScheduleAvailable {
		//User wants to create a new connection.
		/**
		if(message.getRequestType() == 678) {
			if(message.getPassword() != null && message.getUsername() != null) {
				LineManager lm = rcon.getLineManagerRepo().findByUsername(message.getUsername());
				if(lm != null) {
					BCrypt.Result result = BCrypt.verifyer().verify(message.getPassword().toCharArray(),lm.getHash());
					if(result.verified) {
						Long id = createNewSession( message,  session);
						lm.setSessionID(id);
						rcon.getLineManagerRepo().saveAndFlush(lm);
					}
					
				} else { 
					session.sendMessage(new TextMessage("{\"requestType\": 517, \"command\": false}"));
				}
				return;
			}
			
		}**/
		
		if(message.getRequestType() == 678) {
 			if(message.getPassword() != null && message.getUsername() != null) {
 				LineManager lm = rcon.getLineManagerRepo().findByUsername(message.getUsername());
 				if(lm != null) {
 					if(lm.getSessionID() == null) {
 						createNewSessionBusiness(lm,  session);
 						

 					} else {
 						WebSocketSession foundSess = sessionHandl.getSession(lm.getSessionID());
 						if(foundSess == null) {
 							createNewSessionBusiness(lm,  session);

 						} else {
 							String newID = sessionHandl.addSession(session);
 							foundSess.close();
 							sessionHandl.removeSession(lm.getSessionID());
 							lm.setSessionID(newID);
 							rcon.getLineManagerRepo().save(lm);
 	 						session.sendMessage(new TextMessage("{\"requestType\": 678, \"command\": \""+lm.getSessionID()+"\"}"));
 						}
 						
 					}
 				} else { 
 					session.sendMessage(new TextMessage("{\"requestType\": 517, \"command\": false}"));
 				}
 				return;
 			}

 		}
		
		
		//User wants to execute a command
		if(message.getRequestType() == 389) {
			if(message.getCommand().equalsIgnoreCase("NEXT")) {
				String update = businessNext(message.getId());
				session.sendMessage(new TextMessage("{\"requestType\": 735, \"command\": \""+update+"\"}"));
			}
			if(message.getCommand().equalsIgnoreCase("UPDATE")) {
				checkForBusinessUpdate(message.getId(), session);
			}
			if(message.getCommand().equalsIgnoreCase("CLOSE")) {
				String update = businessClose(message.getId());
				session.sendMessage(new TextMessage("{\"requestType\": 735, \"command\": \""+update+"\"}"));
			}
			if(message.getCommand().equalsIgnoreCase("OPEN")) {
				String update = businessOpen(message.getId());
				session.sendMessage(new TextMessage("{\"requestType\": 735, \"command\": \""+update+"\"}"));
			}
			if(message.getCommand().equalsIgnoreCase("CANCEL")) {
				String update = businessCancel(message.getId(), session);
				session.sendMessage(new TextMessage("{\"requestType\": 735, \"command\": \""+update+"\"}"));
			}
			
			if(message.getCommand().equalsIgnoreCase("CODE")) {
				String update = businessCancel(message.getId(), session);
				session.sendMessage(new TextMessage("{\"requestType\": 735, \"command\": \""+update+"\"}"));
			}
			
		}
		
	}
	
	private String businessClose(String id) {
		String answer = "";
		try {
			LineManager bpn = rcon.getLineManagerRepo().findBySessionID(id).get();
			Queue queue = bpn.getBusiness().retriveQueue();
			
			queue.close();
			rcon.getQueueRepo().saveAndFlush(queue);
			answer = RespondText.bussinessClose();
			return answer;
		} catch (NullPointerException ex) {
			answer = RespondText.bussinessWrongPhoneNumber();
		} catch (NoScheduleAvailable e) {
			answer = RespondText.bussinessNoLine();
		}
		return answer;
	}
	
	private String businessOpen(String id) {
		String answer = "";
		try {
			LineManager bpn = rcon.getLineManagerRepo().findBySessionID(id).get();
			Queue queue = bpn.getBusiness().retriveQueue();
			queue.open();
			rcon.getQueueRepo().saveAndFlush(queue);

			
			

			answer = RespondText.bussinessOpen();
			return answer;
		} catch (NullPointerException ex) {
			answer = RespondText.bussinessWrongPhoneNumber();
		} catch (NoScheduleAvailable e) {
			answer = RespondText.bussinessNoLine();
		}
		return answer;
	}
	
	
	
	private String businessCancel(String id, WebSocketSession session) throws IOException {
		String answer = "";
		try {
			LineManager bpn = rcon.getLineManagerRepo().findBySessionID(id).get();
			Queue queue = bpn.getBusiness().retriveQueue();
			
			List<Queuer> remainingLine = queue.cancelAndClose();
			mailMan.updateUserCancel(remainingLine);
			//queue.getLog().getLogEntry().clear();
			
			rcon.getQueuerRepo().saveAll(queue.getQueuerList());
			rcon.getQueuerRepo().flush();
			answer = RespondText.bussinessCancel();
			sendUpdateQueueSize(session, queue);
		} catch (NullPointerException ex) {
			answer = RespondText.bussinessWrongPhoneNumber();
		} catch (NoScheduleAvailable e) {
			answer = RespondText.bussinessNoLine();
		}
		return answer;
	}
	
	private String businessNext(String id) throws IOException {
		String answer = "";
		try {
			LineManager bpn = rcon.getLineManagerRepo().findBySessionID(id).get();
			Queue queue = bpn.getBusiness().retriveQueue();
			if(queue == null) {
				answer = RespondText.bussinessNoLine();
				return answer;
			}

			Queuer queuer =  qH.next(queue.getId());
			
			queuer.setStoreEntryTime(Instant.now());
			rcon.getQueuerRepo().saveAndFlush(queuer);
			queue.calculateETA();
			rcon.getQueueRepo().saveAndFlush(queue);
		
						
			

			mailMan.messageCurrentClient(queuer);
			answer = RespondText.bussinessNextWebApp(queuer.getId().toString());
			mailMan.brodcastUpdate(queuer);
			
			/*
			 * get logEntry from queuer
			 * set storeEntryTime to now
			 * save db changes
			 * */
			
			if(queuer.getSessionId() != null) {
				
				WebSocketSession session = sessionHandl.getSession(queuer.getSessionId());
				if(session != null) {
					session.sendMessage(new TextMessage("{\"requestType\": 999}"));
					sessionHandl.removeSession(queuer.getSessionId());
					
				}
			}
			

		} catch (NullPointerException ex) {
			System.out.println("ERROR" + ex.getMessage());
			answer = RespondText.bussinessWrongPhoneNumber();
		} catch(IndexOutOfBoundsException ex) {
			System.out.println("Index out of bounce");
			answer = RespondText.bussinessEmptyLineWebApp();
		} catch (NoScheduleAvailable e) {
			answer = RespondText.bussinessNoLine();

		}
		return answer;
	}
	
	
	private void checkForBusinessUpdate(String id, WebSocketSession wbs) throws IOException {
		String answer = "";
		
		try {
			LineManager bpn = rcon.getLineManagerRepo().findBySessionID(id).get();
			Queue queue = bpn.getBusiness().retriveQueue();
			if(bpn.getBusiness().retriveQueue().isClosed()) {
				answer = "The line is currently closed, open it if you want to accept customers.";
				wbs.sendMessage(new TextMessage("{\"requestType\": 735, \"command\": \""+answer+"\"}"));
			} else {
				answer = RespondText.bussinessUpdateWebApp(qH.getQueuersInLineSize(queue));

			}
			wbs.sendMessage(new TextMessage("{\"requestType\": 945, \"command\": \""+queue.getBussiness().getCode()+"\"}"));
			sendUpdateQueueSize(wbs, queue);

		} catch (NullPointerException ex) {
			answer = "ERROR";
			System.out.println("Null pointer exception");
		} catch (NoScheduleAvailable e) {
			answer = "ERROR";
			System.out.println("No schedule");
		}
		
	}
	
	
	
	
	//Stores a new session into the arrayList and returns sessionID
	private String createNewSession(WebSocketSession session) throws IOException {
		
		return sessionHandl.addSession(session);
	    
	    
	}
	
	private void createNewSessionBusiness(LineManager lm, WebSocketSession session) throws IOException{
		String id= sessionHandl.addSession(session);
		lm.setSessionID(id);
		System.out.println("this is the session id" + id);
		rcon.getLineManagerRepo().saveAndFlush(lm);
		session.sendMessage(new TextMessage("{\"requestType\": 678, \"command\": \""+lm.getSessionID()+"\"}"));

		
	    
	}
	
	
	
	private void processClientRequest(WebAppRequestMess message, WebSocketSession session) throws IOException {
	    	
	    	//Reconnects session needs ID
	    	if(message.getRequestType() == 793 && message.getId() != null) {
	    		
	    		/*Optional<Queuer> queuerOp = rcon.getQueuerRepo().findById(Long.parseLong(message.getId()));
	    		Queuer queuer =queuerOp.get();
	    		WebSocketSession tmp = sessionHandl.getSession(queuer.getSessionId());
	    		if(tmp != null) {
	    			tmp.close();
	    			sessionHandl.addSession(session);
	    		}*/
	    		
	    		
	    		//session.sendMessage(new TextMessage("Code Recived: 793"));
	    		try {
	    			Optional<Queuer> queuerOp = rcon.getQueuerRepo().findBySessionId(message.getId());
		    		Queuer queuer =queuerOp.get();
		    		WebSocketSession tmp = sessionHandl.getSession(queuer.getSessionId());
		    		//user not found
		    		if(queuerOp.isPresent() == false) {
		    			
		    			String newID = createNewSession(session);
		    			queuer.setSessionId(newID);
						rcon.getQueuerRepo().saveAndFlush(queuer);
						
						
						session.sendMessage(new TextMessage("{\"requestType\": 678, \"command\": \""+queuer.getSessionId()+"\"}"));
						
		    			//session.sendMessage(new TextMessage("{\"requestType\": 793, \"command\": false}"));
		    			//session.sendMessage(new TextMessage("793 false"));
		    			
		    			
		    		} else {
		    			//user found
		    			/*tmp.close();
		    			sessionHandl.addSession(session);*/
		    			WebSocketSession oldSession = sessionHandl.setSession(queuer.getSessionId(), session);
		    			if(oldSession != null) {
		    				oldSession.close();
		    			}
		    			
		    			//session.sendMessage(new TextMessage("793 true"));
		    			
		    			WebSocketSession index = sessionHandl.getSession(queuer.getSessionId());
			    		//Check if id exist
			    		if(index != null){
			    			session.sendMessage(new TextMessage("{\"requestType\": 793, \"command\": true}"));
			    			//session.sendMessage(new TextMessage("SessionFound, inside of array with ID: " + index.getId()));
			    			mailMan.updateUser(session, queuer);
			    		} else {
			    			session.sendMessage(new TextMessage("{\"requestType\": 793, \"command\": false}"));
			    			//session.sendMessage(new TextMessage("SessionFound, no Queue"));
			    		}
		    		}
	    		} catch (Exception ex) {
	    			session.sendMessage(new TextMessage("{\"requestType\": 793, \"command\": false}"));
	    		}
	    		
	    		
	    		
	    	}
	    	
	    		
	    		//Sending a command, needs ID
	    	if(message.getRequestType() == 958 && message.getId() != null) {
	    		
	    		String messagetmp = "";
	    		if(message.getCommand() == null) {
	    			
	    			session.sendMessage(new TextMessage("{\"requestType\": 404, \"command\": formatError}"));
	    			return;
	    		}
	    		
	    		Optional<Queuer> queuerOp = rcon.getQueuerRepo().findBySessionId(message.getId());
	    		if(!queuerOp.isPresent()) {
	    			session.sendMessage(new TextMessage("{\"requestType\": 404, \"command\": noQueuerFound}"));
	    			return;
	    			
	    		}
	    		Queuer queuer =queuerOp.get();
	    		WebSocketSession index = sessionHandl.getSession(queuer.getSessionId());

	    		
	    		//User requests update
	    		if(message.getCommand().equalsIgnoreCase("UPDATE")) {
	    			mailMan.updateUser(session, queuer);
	    			return;
	    		} 
	    		
	    		//User Cancels place in line
	    		else if((message.getCommand().equalsIgnoreCase("OUT"))) {
	    			
	    				Queue queueFound = queuer.getQueue();	    
	    				
		    			queuer.setStatus(QueuerStatus.CANCELED);
		    			
		    			messagetmp = RespondText.clientCancelWebApp(queueFound.getBussiness().getNameOfStore());
		    			rcon.getQueuerRepo().saveAndFlush(queuer);
		    			sessionHandl.removeSession(queuer.getSessionId());
		    			mailMan.brodcastUpdate(queuer);
		    	}
	    		
	    		
	    		else if(message.getCommand().equalsIgnoreCase("PAUSE")) {
	    			if(!queuer.isPaused()) {
	    				if(queuer.getTimesPaused() < 5) {
	    					queuer.setPaused(true);
	    					rcon.getQueuerRepo().saveAndFlush(queuer);
	    					messagetmp = RespondText.pausedSuccessfulWebApp();
	    				}
	    				if(queuer.getTimesPaused() >= 5) {
	    					messagetmp = RespondText.pausedUnsuccessfulWebApp();
	    				}
	    			} else {
	    				queuer.setPaused(false);
    					rcon.getQueuerRepo().saveAndFlush(queuer);
    					messagetmp = RespondText.unPausedSuccessfulWebApp();
	    			}
	    			System.out.println("PAUSE was clicked");
	    			
	    		}
    			session.sendMessage(new TextMessage("{\"requestType\": 359, \"command\": \""+messagetmp+"\"}"));

	    		//session.sendMessage(new TextMessage(messagetmp));
	    	}
	    		
		    		
		    		
	    		//User want's to join the line.
		    	if(message.getRequestType() == 678) {
		    		String messagetmp = "";
	    			Business bFound = rcon.getBusinessRepo().findByCode((message.getCommand()));
					if(bFound == null) {
						session.sendMessage(new TextMessage("{\"requestType\": 404, \"command\": No business found}"));
						//session.sendMessage(new TextMessage("No business found"));
						
					} else {
						
						
						//Adds to queue
						Queue queue;
						try {
							
							queue = bFound.retriveQueue();
							if(!queue.isClosed()) {
								//System.out.println("ID: " + queue.getSimpleSchedule().getId());
								Long prevCount = qH.getQueuersInLineSize(queue);
								String newID = createNewSession(session);
								Queuer queuer = Queuer.builder().sessionId(newID).queue(queue).arrivalTime(Instant.now()).build(); //log entry queuer
								queuer.setStatus(QueuerStatus.INLINE);
								queue.add(queuer);
								rcon.getQueuerRepo().saveAndFlush(queuer);
							
								
								if(prevCount == 0) {
									mailMan.notifyLineNotEmpty(queue.getBussiness());
									//logEntry.setStoreEntryTime(Instant.now());
								}
								
								rcon.getQueueRepo().saveAndFlush(queue);
								session.sendMessage(new TextMessage("{\"requestType\": 678, \"command\": \""+queuer.getSessionId()+"\"}"));
								try {
									Double avgEntry = queue.getAverageEntryTime();
									
									session.sendMessage(new TextMessage("{\"requestType\": 998, \"command\": \""+ avgEntry +"\"}"));
								}
								catch(java.lang.NullPointerException e) {
									
								}

								
								
								messagetmp = RespondText.joinQueueWebApp(bFound.getNameOfStore(), prevCount);
								mailMan.updateLineManager(queuer);
							} else {
								messagetmp = RespondText.queuerError(bFound.getNameOfStore());
							}
						} catch (NoScheduleAvailable e) {
							messagetmp = RespondText.queuerError(bFound.getNameOfStore());
							//System.out.println(e);
						} catch (StoreIsClosed e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
						
					}
					
					//session.sendMessage(new TextMessage(messagetmp));
					System.out.println(messagetmp);
					session.sendMessage(new TextMessage("{\"requestType\": 359, \"command\": \""+messagetmp+"\"}"));
					
	    		}
		    	
		    	
		    	//Update contact information
		    	if(message.getRequestType() == 834 && message.getId() != null) {
		    		Optional<Queuer> queuerOp = rcon.getQueuerRepo().findBySessionId(message.getId());
		    		Queuer user =queuerOp.get();
	    			
	    				
	    			user.setEmail(message.getEmail());
	    			user.setPhoneNum(message.getPhoneNum());
	    			
	    			if(user.getPhoneNum() != null) {
	    				mailMan.sendSMS(user, RespondText.smsNotification(user.getQueue().getBussiness().getNameOfStore()));

	    			}
	    				
	    				
		    		rcon.getQueuerRepo().saveAndFlush(user);
		    	
		    			
	    			
	    			
	    			session.sendMessage(new TextMessage("{\"requestType\": 834, \"command\": true}"));
	    			//session.sendMessage(new TextMessage(messagetmp));
		    	}
	}
	
	private void sendUpdateQueueSize(WebSocketSession session, Queue queue) throws IOException {
		session.sendMessage(new TextMessage("{\"requestType\": 845, \"command\": \""+qH.getQueuersInLineSize(queue)+" People\"}"));
		
		

	}
	
	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws IOException {
	    System.out.println("New Binary Message Received");
	    session.sendMessage(message);
	}
	
}
