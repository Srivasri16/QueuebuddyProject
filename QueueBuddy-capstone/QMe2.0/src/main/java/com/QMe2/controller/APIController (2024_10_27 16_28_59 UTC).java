package com.QMe2.controller;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.socket.WebSocketSession;

import com.QMe2.Application;
import com.QMe2.SMSClasses.SmsSender;
import com.QMe2.bean.Business;
import com.QMe2.bean.BusinessPhoneNumber;
import com.QMe2.bean.BusinessUpdateHandler;
import com.QMe2.bean.LineManager;
import com.QMe2.bean.Queue;
import com.QMe2.bean.Queuer;
import com.QMe2.bean.SMSTracker;
import com.QMe2.bean.ScheduleException;

import com.QMe2.bean.SessionHandlerControl;
import com.QMe2.bean.SimpleSchedule;
import com.QMe2.bean.TestSMS;
import com.QMe2.enums.QueueStatus;
import com.QMe2.enums.QueuerStatus;
import com.QMe2.enums.SMSTypeNotification;
import com.QMe2.exceptions.NoScheduleAvailable;
import com.QMe2.exceptions.StoreIsClosed;
import com.QMe2.helpers.QueueHelper;
import com.QMe2.helpers.RespondText;
import com.QMe2.helpers.SessionHandlerBean;
import com.QMe2.repo.LineManagerRepo;
import com.QMe2.service.MailManController;
import com.QMe2.service.RepoController;
import com.twilio.exception.ApiException;
import com.twilio.twiml.TwiMLException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/callAPI")
public class APIController {
	@Autowired
	private RepoController rc;
	
	@Autowired
	private MailManController mailMan;
	
	@Autowired
	private SessionHandlerControl sessionHandl;
	
	@Autowired
	private QueueHelper qH;
	
	
	
	
	@PostMapping("/sendTXT")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void sendTxt(HttpServletRequest request, HttpServletResponse response) {
		//Use the below number to register the request with a business
		String messageSidn = request.getParameter("SmsMessageSid");
		String fromNum = request.getParameter("From");
		String body = request.getParameter("Body");
		
	    response.setContentType("application/xml");

	    try {
	    	
	      SmsSender.sendText(fromNum, "Hello");
	    } catch (TwiMLException e) {
	      e.printStackTrace();
	    }
	    catch(ApiException e) {
	    	System.out.println(fromNum + " is blacklisted");
	    }

	}
	private LineManagerRepo lineManagerRepo;
	//Request handler
	@PostMapping("/clientRequest")
	
	public void clientHandler(HttpServletRequest request, HttpServletResponse response) {
		
		TestSMS sms = TestSMS.builder().phoneNum(request.getParameter("From")).message(request.getParameter("Body")).build();
		System.out.println(sms.toString());
		String rec = sms.getMessage();
		String message = "";
		if(rec.length() == 11) {
			message = addToQueue(sms);
		
		}  
		else if(rec.equalsIgnoreCase("UPDATE")) {
			message = getStatus(sms);
		}
		
		else if(rec.equalsIgnoreCase("CANCEL")) {
			try {
				message = cancelAppointment(sms);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		else if(rec.equalsIgnoreCase("PAUSE")) {
			message = pauseLine(sms);
		}
		
		else if(rec.equalsIgnoreCase("HELP")) {
			
			return;
		}
		
		
		else {
			message = RespondText.notRecognized();
		}
		try {
			String messId = SmsSender.sendText(sms.getPhoneNum(), message);
			System.out.println(messId.toString());
			rc.getSmsTrackerRepo().saveAndFlush(SMSTracker.builder().business(sms.getBusiness()).messageSID(messId).build());
		}
		catch(com.twilio.exception.ApiException e) {
			System.out.println(sms.getPhoneNum() + " is blacklisted");
			System.out.println(e.toString());
		}
		catch(Exception e) {
			System.out.println(e.toString());
			
		}
		
		System.out.println(message);
	
	}
	
	@PostMapping("/bussinessRequest")
	public String bussinessHandler(HttpServletRequest request, HttpServletResponse response) {
		TestSMS sms = TestSMS.builder().phoneNum(request.getParameter("From")).message(request.getParameter("Body")).build();
		String rec = sms.getMessage();
		String message = "";

		//if(rec.length() == 11) {
			//message = addToQueue(sms);
		//}
		if(rec.equalsIgnoreCase("NEXT")) {
			message = callNext(sms);
		}
		
		else if(rec.equalsIgnoreCase("UPDATE")) {
			message = storeUpdate(sms);
		}
		
		else if(rec.equalsIgnoreCase("CLOSE")) {
			message = storeClose(sms);
		}
		
		else if(rec.equalsIgnoreCase("OPEN")) {
			message = storeOpen(sms);
		}
		
		else if(rec.equalsIgnoreCase("CANCEL LINE")) {
			message = storeCancel(sms);
		}
		
		else if(rec.equalsIgnoreCase("HELP ME")) {
			message = RespondText.helpBussiness();
		} else {
			message = RespondText.notRecognized();
		}
		rc.getSmsTrackerRepo().saveAndFlush(SMSTracker.builder().business(sms.getBusiness()).messageSID(request.getParameter("SmsMessageSid")).build());
		//String messId = SmsSender.sendText(sms.getPhoneNum(), message);
		//rc.getSmsTrackerRepo().saveAndFlush(SMSTracker.builder().business(sms.getBusiness()).messageSID(messId).build());
		return message;
	}
	
	
	
	
	//Store Customers
	@PostMapping("/addQueue")
	public String addToQueue(@RequestBody TestSMS sms)  {
		String message = "";
		//Checks to see if user is already a queue
		Queuer alreadyInQueue = rc.getQueuerRepo().findByPhoneNumAndStatus(sms.getPhoneNum(),QueuerStatus.INLINE);
		
		if(alreadyInQueue != null && alreadyInQueue.getStatus() == QueuerStatus.INLINE) {
			message = RespondText.clientAlreadyInLine();
			return message;
		}
		
		
		//check code length is equal to 11
		if(sms.getMessage().length() != 11) {
			message = RespondText.clientWrongCode();
			return message;
		}
		
		//check to see if code is valid
		Business bFound = rc.getBusinessRepo().findByCode(sms.getMessage());
		sms.setBusiness(bFound);
		if(bFound == null) {
			message = RespondText.clientWrongCode();
			return message;
		}
		

		//Adds to queue
		Queue queue;
		try {
			queue = bFound.retriveQueue();
			Queuer queuer = Queuer.builder().phoneNum(sms.getPhoneNum()).queue(queue).status(QueuerStatus.INLINE).arrivalTime(Instant.now()).build();
			
			queue.add(queuer);
			
			rc.getBusinessRepo().flush();
			rc.getQueueRepo().flush();
			

			message = RespondText.joinQueue(bFound.getNameOfStore(), qH.getStatus(queuer));
			mailMan.updateLineManager(queuer);
		} catch (NoScheduleAvailable e) {
			message = RespondText.queuerError(bFound.getNameOfStore());
			//System.out.println(e);
		} catch (StoreIsClosed e) {
			message = RespondText.queuerError(bFound.getNameOfStore());
			//System.out.println(e);
		} catch (IOException ex) {
			//error
		}
		
		return  message;
		
	}
	
	@PostMapping("/requestUpdateUserQueue")
	public String getStatus(@RequestBody TestSMS sms) {
		String answer = "";
		Queuer user = rc.getQueuerRepo().findByPhoneNumAndStatus(sms.getPhoneNum(),QueuerStatus.INLINE);
		if(user == null) {
			answer = RespondText.clientNoLineAv();
			//System.out.println("user is not in any queue");
			return answer;
		}
		sms.setBusiness(user.getQueue().getBussiness());
		Queue queueFound = user.getQueue();
		try {
			if(user.isPaused()) {
				answer = RespondText.clientUpadateTextPause(user, qH.getStatus(user), queueFound.getBussiness().getNameOfStore(), SMSTypeNotification.REGULAR);
			} else {
				answer = RespondText.clientUpdate(queueFound.getBussiness().getNameOfStore(), qH.getStatus(user));
			}
			
			
		} catch(Exception ex) {
			answer = "Sorry there was an error, pleas try again later";
		}
		
		return answer;
		
	}
	
	public String pauseLine(TestSMS sms) {
		String answer = "";
		Queuer user = rc.getQueuerRepo().findByPhoneNumAndStatus(sms.getPhoneNum(),QueuerStatus.INLINE);
		if(user == null) {
			answer = RespondText.clientNoLineAv();
			//System.out.println("user is not in any queue");
			return answer;
		}
		
		String messagetmp = null;
		if(!user.isPaused()) {
			if(user.getTimesPaused() < 5) {
				user.setPaused(true);
				rc.getQueuerRepo().saveAndFlush(user);
				messagetmp = RespondText.pausedSuccessfuText();
			}
			if(user.getTimesPaused() >= 5) {
				messagetmp = RespondText.pausedUnsuccessfulText();
			}
		} else {
			user.setPaused(false);
			rc.getQueuerRepo().saveAndFlush(user);
			messagetmp = RespondText.unPausedSuccessfulWebApp();
		}
		
		return messagetmp;
		
	}
	
	@PostMapping("/cancelAppointment")
	public String cancelAppointment(@RequestBody TestSMS sms) throws IOException {
		String answer = "";
		Queuer user = rc.getQueuerRepo().findByPhoneNumAndStatus(sms.getPhoneNum(),QueuerStatus.INLINE);
		if(user == null) {
			answer = RespondText.clientNoLineAv();
			return answer;
		}
		Queue queueFound = user.getQueue();
		queueFound.cancel(user);
		sms.setBusiness(queueFound.getBussiness());
		answer = RespondText.clientCancel(queueFound.getBussiness().getNameOfStore());
		rc.getQueueRepo().saveAndFlush(queueFound);
		mailMan.brodcastUpdate(user);
		return answer;
	}
	
	//Store Owners
	
	private BusinessPhoneNumber verifyNumber(TestSMS sms) {
		BusinessPhoneNumber bpn = rc.getBusinessPhoneNumberRepo().findByPhoneNumber(sms.getPhoneNum());
		
		if(bpn == null) {
			throw new NullPointerException("not valid phone number");
		}
		sms.setBusiness(bpn.getBusiness());
		return bpn;
	}
	
	@PostMapping("/callNext")
	public String callNext(@RequestBody TestSMS sms) {
		String answer = "";
		try {
			BusinessPhoneNumber bpn = verifyNumber(sms);
			Queue queue = bpn.getBusiness().retriveQueue();
			if(queue == null) {
				answer = RespondText.bussinessNoLine();
				return answer;
			}
			Queuer queuer =  qH.next(queue.getId());
			rc.getQueueRepo().saveAndFlush(queue);
			mailMan.messageCurrentClient(queuer);
			answer = RespondText.bussinessNext(queuer.getId().toString());
			mailMan.brodcastUpdate(queuer);
			if(queuer.getSessionId() != null) {
				WebSocketSession session = sessionHandl.getSession(queuer.getSessionId());
				if(session != null) {
					sessionHandl.removeSession(queuer.getSessionId());
				}
			}
		} catch (NullPointerException ex) {
			answer = RespondText.bussinessWrongPhoneNumber();
		} catch(IndexOutOfBoundsException ex) {
			answer = RespondText.bussinessEmptyLine();
		} catch (NoScheduleAvailable e) {
			answer = RespondText.bussinessNoLine();

		} catch(IOException ex) {
			//error
		}
		return answer;
		
	}
	
	@PostMapping("/storeStaffUpdate")
	public String storeUpdate(@RequestBody TestSMS sms) {
		String answer = "";
		try {
			BusinessPhoneNumber bpn = verifyNumber(sms);
			Queue queue = bpn.getBusiness().retriveQueue();
			answer = RespondText.bussinessUpdate(qH.getQueuersInLineSize(queue));
		} catch (NullPointerException ex) {
			answer = RespondText.bussinessWrongPhoneNumber();
		} catch (NoScheduleAvailable e) {
			answer = RespondText.bussinessNoLine();
		}
		return answer;
	}
	
	@PostMapping("/storeStaffClose")
	public String storeClose(@RequestBody TestSMS sms) {
		String answer = "";
		try {
			BusinessPhoneNumber bpn = verifyNumber(sms);
			Queue queue = bpn.getBusiness().retriveQueue();
			
			queue.close();
			rc.getQueueRepo().flush();
			answer = RespondText.bussinessClose();
			return answer;
		} catch (NullPointerException ex) {
			answer = RespondText.bussinessWrongPhoneNumber();
		} catch (NoScheduleAvailable e) {
			answer = RespondText.bussinessNoLine();
		}
		return answer;
	}

	@GetMapping("/getQueueStatus")
	public ResponseEntity<QueueStatus> getQueueStatus(String storeCode) {

		ResponseEntity<QueueStatus> result = ResponseEntity.ok().body(QueueStatus.DoesNotExsist);
		Business business = rc.getBusinessRepo().findByCode(storeCode);
		if (business != null) {
			try {
				Queue currentQueue = business.retriveQueue();
				if (currentQueue.isClosed()) {
					result = ResponseEntity.ok().body(QueueStatus.Closed);
				} else {
					result = ResponseEntity.ok().body(QueueStatus.Open);
				}
			} catch (NoScheduleAvailable e) {
				return result;
			}


		}

		return result;
	}
	
	@PostMapping("/storeStaffOpen")
	public String storeOpen(@RequestBody TestSMS sms) {
		String answer = "";
		try {
			BusinessPhoneNumber bpn = verifyNumber(sms);
			Queue queue = bpn.getBusiness().retriveQueue();
			queue.open();
			rc.getQueueRepo().flush();
			answer = RespondText.bussinessOpen();
			return answer;
		} catch (NullPointerException ex) {
			answer = RespondText.bussinessWrongPhoneNumber();
		} catch (NoScheduleAvailable e) {
			answer = RespondText.bussinessNoLine();
		}
		return answer;
	}
	
	@PostMapping("/storeStaffCancel")
	public String storeCancel(@RequestBody TestSMS sms) {
		String answer = "";
		try {
			BusinessPhoneNumber bpn = verifyNumber(sms);
			Queue queue = bpn.getBusiness().retriveQueue();
			List<Queuer> remainingLine = queue.cancelAndClose();
			mailMan.updateUserCancel(remainingLine);
			rc.getQueueRepo().saveAndFlush(queue);
			answer = RespondText.bussinessCancel();
		} catch (NullPointerException ex) {
			answer = RespondText.bussinessWrongPhoneNumber();
		} catch (NoScheduleAvailable e) {
			answer = RespondText.bussinessNoLine();
		} catch (IOException ex){
			//error
		}
		return answer;
	}


	@PostMapping("/addNewBusiness")
	public ResponseEntity<Business> addNewBussiness( @RequestBody Business business) {
		business.setCode(bussinessCodeGenerator(business));
		Business created =rc.getBusinessRepo().save(business);

		ArrayList<SimpleSchedule> defaultSchedule = new ArrayList<SimpleSchedule>();

		for(DayOfWeek day : DayOfWeek.class.getEnumConstants()) {
			SimpleSchedule ss = SimpleSchedule.builder().dayofWeek(day).isClosed(true).build();

			ss.setBusiness(created);
			Queue queue = Queue.builder().build();
			Queue tmpQueue = rc.getQueueRepo().saveAndFlush(queue);
			ss.setQueue(tmpQueue);
			SimpleSchedule tmpSched = rc.getScheduleRepo().saveAndFlush(ss);

			queue.setSimpleSchedule(tmpSched);
			defaultSchedule.add(tmpSched);
		}

		try {
			created.setSimpleSchedule(defaultSchedule);
		}
		catch(StackOverflowError e) {
			System.out.print("Stack overflow");
		}

		return ResponseEntity.ok().body(created);
		
	}
	
	@PostMapping("/testCity")
	public String bussinessCodeGenerator(@RequestBody Business business) {
		if(business.getCity() == null || business.getCountry() == null) {
			throw new IndexOutOfBoundsException();
		}
		String city = business.getCity().replaceAll("//s", "").replaceAll("[^A-Za-z0-9]", "");
		String country = business.getCountry().replaceAll("//s", "").replaceAll("[^A-Za-z0-9]", "");
		
		if(city.length() < 3) {
			String firstLetter = city.substring(0, 1);
			city = city + firstLetter + firstLetter + firstLetter;
			
		} 
		if(country.length() < 4) {
			String firstLetter = country.substring(0, 1);
			country = country + firstLetter + firstLetter + firstLetter;
		} 
		System.out.println("City issue: " + city);
		city = city.substring(0, 4).toUpperCase();
		
		country = country.substring(0, 3).toUpperCase();
		String ccCode = country + city;
		List<Business> listOfBussiness = rc.getBusinessRepo().findByCodeContaining(ccCode);
		int tentId = listOfBussiness.size() +1;
		String padded = String.format("%04d" , tentId);
		Business found = rc.getBusinessRepo().findByCode(ccCode+padded);
		
		do {
			if(found != null) {
				tentId++;
				padded = String.format("%04d" , tentId);
			}
		} while (found != null);
		
		return ccCode+padded;
	}
	
	/**
	 * Adds new phone numbers to manage the line
	 * @param buph must have referenceID and businessPhoneNumber
	 * @return true if successful or false if not
	 */
	@PostMapping("/addNewManagerPhoneNumber")
	public boolean addNewManagerPhoneNumber(@RequestBody BusinessUpdateHandler buph) {
		try {
			Business businessFound = checkIfBusinessEx(buph.getReferenceID());
			for(BusinessPhoneNumber b: buph.getBusinessPhoneNumber()) {
				if(rc.getBusinessPhoneNumberRepo().findByPhoneNumber(b.getPhoneNumber()) != null) {
					return false;
				}
			}
			businessFound.getBusinessPhoneNumber().clear();
			rc.getBusinessRepo().flush();
			for(BusinessPhoneNumber b: buph.getBusinessPhoneNumber()) {
				b.setBusiness(businessFound);
				businessFound.getBusinessPhoneNumber().add(b);
			}
			rc.getBusinessRepo().flush();
			return true;
		} catch(NullPointerException ex) {
			System.out.println(ex.getMessage());
			return false;
		} catch( Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}
	}
	
	/**
	 * Adds simple schedule, if there are previous schedules it will just edit the info
	 * Without reseting the line
	 * @param buph must have referenceID and simpleSchedule set
	 * @return true if successfully updated
	 */
	@PostMapping("/addSchedule")
	public boolean addSchedule(@RequestBody BusinessUpdateHandler buph) {
		try {

			if (buph.getSimpleSchedule().size() != 7) {

				throw new NullPointerException("Missing days");
			}
			Business businessFound = checkIfBusinessEx(buph.getReferenceID());

			if (businessFound.getSimpleSchedule().isEmpty()) {
				for (SimpleSchedule ss : buph.getSimpleSchedule()) {
					if (ss.isClosed() == false && (ss.getClose() == null || ss.getOpen() == null)) {
						throw new NullPointerException("Store is not closed and close or open are null");
					}
					ss.setBusiness(businessFound);
					Queue queue = Queue.builder().build();
					Queue tmpQueue = rc.getQueueRepo().saveAndFlush(queue);
					ss.setQueue(tmpQueue);	
					SimpleSchedule tmpSched = rc.getScheduleRepo().saveAndFlush(ss);
					
					queue.setSimpleSchedule(tmpSched);
					businessFound.getSimpleSchedule().add(tmpSched);
				}
			} else {
				for (SimpleSchedule ss : businessFound.getSimpleSchedule()) {
					SimpleSchedule match = getMatchingDay(ss, buph.getSimpleSchedule());
					if (match.isClosed() == true) {
						ss.getQueue().close();
						ss.setClosed(true);
						ss.setOpen(null);
						ss.setClose(null);
					} else {
						ss.getQueue().open();
						ss.setClosed(false);

						if (match.getOpen() == null || match.getClose() == null) {
							throw new NullPointerException("Store is not closed and close or open are null!");
						}

						ss.setOpen(match.getOpen());
						ss.setClose(match.getClose());
					}

				}

			}
			rc.getBusinessRepo().flush();
			return true;
		} catch (NullPointerException ex) {
			System.out.println(ex.getMessage());
			return false;
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}
	}
	
	public SimpleSchedule getMatchingDay(SimpleSchedule toFind, List<SimpleSchedule> listSimple) {
		for(SimpleSchedule ss: listSimple) {
			if(ss.getDayofWeek().equals(toFind.getDayofWeek())) {
				return ss;
			}
		}
		return null;
	}
	
	
	/**
	 * Adds a special schedule to a business. Note that this action will reset
	 * the queue
	 * @param buph must have referenceID and scheduleException.
	 * @return
	 */
	@PostMapping("/addSpecialSchedule")
	public boolean addSpecialSchedule(@RequestBody BusinessUpdateHandler buph) {
		try {
			System.out.println("hello " + buph.getScheduleException().get(0).getDayInEffect().getTime());
			Business businessFound = checkIfBusinessEx(buph.getReferenceID());
			
			List<ScheduleException> tmp = new ArrayList<ScheduleException>();
			for(ScheduleException ss: buph.getScheduleException()) {
				if(ss.getDayInEffect() == null) {
					throw new NullPointerException("No day specified");
				}
				if(ss.isClosed() == false && (ss.getOpen() == null || ss.getClose() == null)) {
					throw new NullPointerException("Store is open but no open or closed vallues set");
				}
				Queue queuetmp = Queue.builder().scheduleException(ss).build();
				ss.setQueue(queuetmp);
				ss.setBusiness(businessFound);
				tmp.add(ss);
			}
			
			businessFound.getScheduleException().clear();
			rc.getBusinessRepo().flush();
			businessFound.getScheduleException().addAll(tmp);
			
			rc.getBusinessRepo().flush();
			return true;
		} catch(NullPointerException ex) {
			System.out.println(ex);
			return false;
		} catch( Exception ex) {
			System.out.println(ex);
			return false;
		}
	}
	
	public Business checkIfBusinessEx(String ref) {
		if(ref == null) {
			throw new NullPointerException("No ref id found");
		}
		
		Business bussinessFound = rc.getBusinessRepo().findByReferenceID(ref);
		if(bussinessFound == null) {
			
			throw new NullPointerException("No bussinessFound");
		}
		return bussinessFound;
		
	}
	
	/**
	 * Gets all the phone numbers that can manage that line
	 * @param buph needs referenceID
	 * @return a list of all the business phone numbers
	 */
	@PostMapping("/getBusinessPhoneNumber")
	public List<BusinessPhoneNumber> getPhoneNumber(@RequestBody BusinessUpdateHandler buph) {
		try {
			Business businessFound = checkIfBusinessEx(buph.getReferenceID());
			return businessFound.getBusinessPhoneNumber();
		} catch(NullPointerException ex) {
			System.out.println(ex.getMessage());
			return null;
		}catch(Exception ex) {
			System.out.println(ex.getMessage());
			return null;
		}
	}
	
	/**
	 * Gets all the schedules available for that business
	 * @param buph needs referenceID
	 * @return a list of all simple schedules
	 */
	@PostMapping("/getSchedules")
	public List<SimpleSchedule> getSchedules(@RequestBody BusinessUpdateHandler buph) {
		try {
			Business businessFound = checkIfBusinessEx(buph.getReferenceID());
			return businessFound.getSimpleSchedule();
		} catch(NullPointerException ex) {
			System.out.println(ex.getMessage());
			return null;
		}catch(Exception ex) {
			System.out.println(ex.getMessage());
			return null;
		}
	}
	
	/**
	 * Gets all schedule Exception for a business
	 * @param buph needs referenceID
	 * @return a list of all the ScheduleExceptions
	 */
	@PostMapping("/getScheduleException")
	public List<ScheduleException> getScheduleException(@RequestBody BusinessUpdateHandler buph) {
		try {
			Business businessFound = checkIfBusinessEx(buph.getReferenceID());
			return businessFound.getScheduleException();
		} catch(NullPointerException ex) {
			System.out.println(ex.getMessage());
			return null;
		}catch(Exception ex) {
			System.out.println(ex.getMessage());
			return null;
		}
	}
	
	@PostMapping("/getBusinessInfo")
	public Business getBusinessInfo(@RequestBody BusinessUpdateHandler buph)
	{
		try {
			Business businessFound = checkIfBusinessEx(buph.getReferenceID());
			return businessFound;
		} catch(NullPointerException ex) {
			System.out.println(ex.getMessage());
			return null;
		}catch(Exception ex) {
			System.out.println(ex.getMessage());
			return null;
		}
		
		
	}
	
	@PostMapping("/updateBusinessInfo")
	public Business updateBusinessInfo(@RequestBody BusinessUpdateHandler buph)
	{
		Business businessFound = checkIfBusinessEx(buph.getReferenceID());
		businessFound.setNameOfOwner(buph.getBusiness().getNameOfOwner());
		businessFound.setEmail(buph.getBusiness().getEmail());
		businessFound.setPhoneNumber(buph.getBusiness().getPhoneNumber());
		return rc.getBusinessRepo().save(businessFound);
	}
	
	
	
	@PostMapping("/setLineManager")
	public ResponseEntity<LineManager> setLineManager(@RequestBody BusinessUpdateHandler buph)
	{
		
		ResponseEntity<LineManager> response;
		if(buph.getLineManager() != null && buph.getReferenceID() != null)
		{
			Business businessFound = checkIfBusinessEx(buph.getReferenceID());
			if(businessFound != null)
			{
				LineManager exsistingManager = businessFound.getLineManager();
				buph.getLineManager().setBusiness(businessFound);
				LineManager lineManager = rc.getLineManagerRepo().saveAndFlush(buph.getLineManager());
				businessFound.setLineManager(lineManager);
				rc.getBusinessRepo().save(businessFound);
				response = new ResponseEntity<LineManager>(lineManager,HttpStatus.OK);
				if(exsistingManager != null)
				{
					
					lineManagerRepo.deleteById(exsistingManager.getId());
					
				}
				
			}
			
			else
			{
				response = new ResponseEntity<LineManager>(buph.getLineManager(),HttpStatus.BAD_REQUEST);
			}
		}
		
		else {
			response = new ResponseEntity<LineManager>(buph.getLineManager(),HttpStatus.BAD_REQUEST);
		}
		
		return response;	
	}
	@PostMapping("/getLineManager")
	public ResponseEntity<LineManager> getLineManager(@RequestBody BusinessUpdateHandler buph)
	{
		ResponseEntity<LineManager> response = null;
		try {
			LineManager lineManager = rc.getLineManagerRepo().findByUsername(buph.getLineManager().getUsername());
			if(lineManager == null) {
				response = ResponseEntity.badRequest().body(buph.getLineManager());
				System.out.println("User not found");
			}
			else {
				response = ResponseEntity.ok().body(lineManager);
			}
			
			return response;
		} catch(NullPointerException ex) {
			return new ResponseEntity<LineManager>(buph.getLineManager(),HttpStatus.BAD_REQUEST);
			
		}catch(Exception ex) {
			return new ResponseEntity<LineManager>(buph.getLineManager(),HttpStatus.BAD_REQUEST);
		}
		
		
		
	}
	//TESTING METHODS
	
	@PostMapping("/bussinessInfoTest")
	public Business returnBussinessInfo() {
		Business bFound = rc.getBusinessRepo().findByCode("CANORAN0001");
		return bFound;
	}
	
	@PostMapping("/addPhoneNumTest")
	public void addPhoneNumber() {
		Business bFound = rc.getBusinessRepo().findByCode("CADMISS0001");
		//TODO: check if number is present maybe try and catch
		bFound.getBusinessPhoneNumber().add(BusinessPhoneNumber.builder().phoneNumber("+13414123265").business(bFound).build());
		rc.getBusinessRepo().flush();
	}
	
	@PostMapping("/addScheduleTest")
	public Business addScheduleTest() {
		Business bFound = rc.getBusinessRepo().findByCode("CADMISS0001");
		if(bFound == null) {
			return null;
		}
		List<SimpleSchedule> tmp = new ArrayList<SimpleSchedule>();
		for(DayOfWeek dof : DayOfWeek.values()) {
			
			Queue tmpQueue = rc.getQueueRepo().saveAndFlush(Queue.builder().build());
			SimpleSchedule sSchedCreated = SimpleSchedule.builder().business(bFound).dayofWeek(dof).open(LocalTime.of(10, 0, 0, 0)).close(LocalTime.of(16, 30, 0, 0)).queue(tmpQueue).build();
			SimpleSchedule tmpSched = rc.getScheduleRepo().saveAndFlush(sSchedCreated);
			tmp.add(tmpSched);
		}
		bFound.setSimpleSchedule(tmp);
		rc.getBusinessRepo().flush();
		return bFound;
	}
	
	public static class MessageDetailsTwo {
        public List<String> numbers;
        public String message;
    }
	
	
	
	
	
}
