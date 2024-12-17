package com.QMe2.helpers;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;

import com.QMe2.bean.Queuer;
import com.QMe2.enums.SMSTypeNotification;

public class RespondText {
	
	
	/**
	 * Queuers methods
	 */
	public static String joinQueue(String storeName, int people) {
		String answer = "Hey, thanks for Queueing!\n" + 
				storeName + " currently has "+ people +" people in line. "+ "ETA 10:35" +" We’ll text you when it’s your turn.\n"+ 
				"Reply HELP for help";
		
		if(people == 0) {
			answer = "Hey, thanks for Queueing!\n" + 
					"You are first in line! Make your way to the store and we'll text you when it's your turn.\n"+
					"Reply HELP for help";
		}
		
		return answer;
	}
	
	public static String joinQueueWebApp(String storeName, Long prevCount) {
		String answer = "Store Name: "+ storeName +".\\n \\n"
						+ "People in front of you: "+prevCount+" \\n\\n"
						+ "You can sign up for text/email notifications below \\n";
		
		if(prevCount == 0) {
			answer = "Store Name: "+ storeName +".\\n \\n"
					+ "People in front of you: 0 \\n \\n"
					+ "**Make your way to the store** \\n \\n";
		}
		
		return answer;
	}
	
	public static String clientUpdate(String storeName, int people) {
		String answer = "You are currently waiting to get into "+ storeName +". There are currently " + people + " people in front of you.\n" + 
				"Reply CANCEL if you wish to get out of the line.";
		return answer;
	}
	
	public static String clientUpdateWebApp(String storeName, int people) {
		String answer = "Store Name: "+ storeName +".\\n \\n"
				+ "People in front of you: "+people+" \\n \\n"; 
		if (people == 0) {
			answer = "Store Name: "+ storeName +".\\n \\n"
					+ "People in front of you: 0 \\n \\n"
					+ "**Make your way to the store** \\n \\n";
		}
		return answer;
	}
	
	public static String cleintUpdateWebAppOnPause(Queuer queuer, int people, long size, String storeName) {
		String answer = "Store Name: "+ storeName +".\\n \\n"
				+ "People in front of you: "+people+" \\n \\n"
				+ "You are currenlty leting people behind you go first: "+queuer.getTimesPaused()+"/5 allowed \\n \\n";
		
		
		//int size = queuer.getQueue().getQueuersInLine().size();
		
		if (people == 0 && queuer.getTimesPaused() <= 5) {
			answer = "Store Name: "+ storeName +".\\n \\n"
					+ "People in front of you: 0 \\n \\n"
					+ "You are currenlty leting people behind you go first: "+queuer.getTimesPaused()+"/5 allowed \\n \\n";
		}
		
		if(people == 0 && queuer.getTimesPaused() > 5) {
			answer = "Store Name: "+ storeName +".\\n \\n"
					+ "People in front of you: 0 \\n \\n"
					+ "We can no longer save your spot in the line, you are been called next. \\n \\n"
					+ "**Make your way to the store** \\n \\n";
		}
		
		if(size == 1 && !(queuer.getTimesPaused() > 5)) {
			answer = "Store Name: "+ storeName +".\\n \\n"
					+ "It seems you are the only one in line, unless someone else joins you'll be called next. \\n \\n";
			
		}
		
		
		return answer;
	}

	public static String clientUpadateTextPause(Queuer queuer, int people, String storeName, SMSTypeNotification typeOfNot) {
		String answer = null;
		String tmp = "ETA: 10:55";
		//regular update
		if ( typeOfNot.equals(SMSTypeNotification.REGULAR)) {
			answer = "Store Name: "+ storeName +".\n"
					+ "People in front of you: " +  people + "\n"
					+ "You are currenlty leting people behind you go first: "+queuer.getTimesPaused()+"/5 allowed\n" + 
					"Reply PAUSE again to unpause the line";
		}
		
		//update to user letting him know that 5 people where called before him
		if(typeOfNot.equals(SMSTypeNotification.LASTWARNING)) {
			answer = "Store Name: "+ storeName +".\n"
					+ "We can no longer save your spot in the line, you are been called next.";
		}
		
		//update letting user know he is the only one in the line
		if(typeOfNot.equals(SMSTypeNotification.LASTONE)) {
			answer = "Store Name: "+ storeName +".\n "
					+ "It seems you are the only one in line, unless someone else joins you'll be called next.\n ";
			
		}
		
		
		return answer;
	}
	
	public static String pausedSuccessfulWebApp() {
		String answer = "You've sucesfully paused the line, you are leting people behind you go first. \\n \\n"
				+ "You can only let 5 people behind you go before loosing your spot in the line. \\n \\n";
		return answer;
	}
	
	public static String unPausedSuccessfulWebApp() {
		String answer = "You are no longet letting people behind you go first. \\n \\n";
		return answer;
	}
	
	public static String pausedSuccessfuText() {
		String answer = "You've sucesfully paused the line, you are leting people behind you go first.\n"
				+ "You can only let 5 people behind you go before loosing your spot in the line.\n"
				+ "Reply PAUSE to unpause the line";
		return answer;
	}
	
	public static String pausedUnsuccessfulText() {
		String answer = "Unfortunatelly, we can no longer save yor spot in the line. \n"
				+ "Please be ready for when your number is called.";
		return answer;
	}
	
	public static String unPausedSuccessfulText() {
		String answer = "You are no longet letting people behind you go first. \\n \\n";
		return answer;
	}
	
	public static String pausedUnsuccessfulWebApp() {
		String answer = "Unfortunatelly, we can no longer save yor spot in the line. \\n \\n"
				+ "Please be ready for when your number is called. \\n \\n";
		return answer;
	}
	
	public static String nextClientSMS(String storeName) {
		return "You are next in line for " + storeName + ". Please make your way back.";
	}
	
	public static String nextClientSMSPaused(String storeName) {
		return "You are next in line for " + storeName + ".\n"
				+ "You are currently letting people behind you go first, however, we can only let 5 people go before you before you are next.\n"
				+ "Reply PAUSE to unpause and let your number be called";
	}
	
	public static String clientCalled(Long id) {
		String answer = "Your number has been called. Number ID: " + id;
		return answer;
	}
	
	public static String clientCalledTxt(Queuer q) {
		String answer = "Your number has been called for "+ q.getQueue().getBussiness().getNameOfStore()+". ID: " + q.getId();
		return answer;
	}
	

	
	public static String clientCancel(String storeName) {
		String answer = "Thanks for canceling, you are no longer waiting to get into "+ storeName +". Have a nice day!";
		return answer;
	}
	
	public static String clientCancelWebApp(String storeName) {
		String answer = "Thanks for canceling, you are no longer waiting to get into "+ storeName +". Have a nice day!";
		return answer;
	}
	
	public static String clientAlreadyInLine() {
		String answer = "Sorry, it seems you are already wiating in antother line, reply CANCEL if you with to get out of that line";
		return answer;
	}
	
	public static String clientWrongCode() {
		String answer = "Sorry, we don't recognize that store code, are you sure you typed it correctly?";
		return answer;
	}
	
	public static String clientNoLineAv() {
		String answer = "Sorry, you don't seem to be waiting in any line at the moment, please reply with the location code.";
		return answer;
	}
	
	public static String queuerError(String storeName) {
		String answer = "Sorry, it seems " + storeName + " is not currenlty taking clients, if you think this is an error please reach out to them directly";
		return answer;
	}
	
	public static String youAreNext(String id) {
		String answer = "You are next! Please make your way inside the facilities and show the following id: " + id ;
		return answer;
	}
	
	public static String helpQueuer() {
		String answer = "Hey, In order to use this service you first need to text us the 10 digit code located at the store front, once you are in the queue you can text us using the following words:\n" + 
				"CANCEL: to get out of the line\n" + 
				"UPDATE: to see how many people are before you."+ 
				"Reply PAUSE if you wish to let people behind you go first.";
		return answer;
	}
	
	public static String cancelMessage(String storeName) {
		String answer = "Unfortunatelly, " + storeName + " has decided to close the store. If you think this is a mistake please contact the store directly." ;
		return answer;
	}
	
	public static String smsNotification(String storeName) {
		return "You've signed up to be notified when you are next to get into "+ storeName+", you can still see a live update or get out of the line using the WEBSITE";
	}
	
	
	/**
	 * Business Methods
	 */
	public static String bussinessNext(String id) {
		String answer = "We just notified the next customer. Please verify their ID.\n "
				+ "ID number: " + id;
		return answer;
	}
	
	public static String bussinessNextWebApp(String id) {
		String answer = "We just notified the next customer. Please verify their ID. ID number: " + id;
		return answer;
	}
	
	public static String bussinessNoLine() {
		String answer = "Sorry, it seems that there is no schedule available for today, please check your account if this is a mistake";
		return answer;
	}
	
	public static String bussinessEmptyLine() {
		String answer = "Hey, it seems no one is waiting in line, we'll text you when the first customer arrives";
		return answer;
	}
	
	public static String bussinessEmptyLineWebApp() {
		String answer = "Hey, it seems no one is waiting in line, we'll let you when the first customer arrives";
		return answer;
	}
	
	public static String bussinessWrongPhoneNumber() {
		String answer = "Sorry, it seems you are not authorized to manage a queue, please ask the owner of the account to add you to the list.";
		return answer;
	}
	
	public static String bussinessUpdate(Long size) {
		if(size ==0) {
			return bussinessEmptyLine();
		}
		String answer = "There are currently "+size+" people waiting in the line.";
		return answer;
	}
	
	public static String bussinessUpdateWebApp(Long long1) {
		if(long1 ==0) {
			System.out.println("BussinessUPPdateWebApp");
			return bussinessEmptyLineWebApp();
		}
		String answer = "There are currently "+long1+" people waiting in the line.";
		return answer;
	}
	
	public static String bussinessClose() {
		String answer = "We just closed the store, we are no longer accepting any more people get in the line";
		return answer;
	}
	
	public static String bussinessFirstClient() {
		String answer = "You have someone waiting in line! Reply NEXT to let them know you can receive them.";
		return answer;
	}
	
	public static String bussinessCancel() {
		String answer = "We've closed the store and told the remaining people in the line that we cannot recive them today.";
		return answer;
	}
	
	public static String bussinessOpen() {
		String answer = "We've opened the store for you. People can now wait in line";
		return answer;
	}
	
	public static String helpBussiness() {
		String answer = "Hey, during the business hour you register with us you can text us the following:\n" + 
				"NEXT – We notify the next customer that they can come in and we will give you their ID\n" + 
				"UPDATE – We tell you how many people are waiting in line\n" + 
				"CLOSE – We close the line, meaning no more people can join the line, however there may be still people waiting\n" + 
				"CANCEL – we close the line and tell the remaining people that you will not be receiving them today.";
		return answer;
	}
	
	public static String notRecognized() {
		String answer = "Sorry, but I don't understand, reply HELP if you need help";
		return answer;
	}
}
