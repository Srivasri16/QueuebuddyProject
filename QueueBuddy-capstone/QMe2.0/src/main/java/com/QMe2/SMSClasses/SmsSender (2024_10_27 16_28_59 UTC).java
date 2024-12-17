package com.QMe2.SMSClasses;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.twilio.exception.*;
public class SmsSender {
    // Find your Account Sid and Auth Token at twilio.com/console
	//but you should use environment variables to keep them secret before deploying to production
    private static final String ACCOUNT_SID =
            "AC48c5117e8b9170580f59ea96ad6e8b81";
    private static final String AUTH_TOKEN =
            "f9bbff489a9cc3228a2d43f9a2698d17";
    
    private static final String FROM_PHONE ="+16473720057";

    public static String sendText(String toNum, String messageTxt) throws ApiException {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        
        Message message = Message
                .creator(new PhoneNumber(toNum), // to
                        new PhoneNumber(FROM_PHONE), // from
                        messageTxt)
                .create();
        
        return message.getSid();
    }
    
    
    
}
