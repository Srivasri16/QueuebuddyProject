package com.QMe2.bean;



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebAppRequestMess {
	int requestType;
	String id;
	String command;
	String email;
	String phoneNum;
	boolean businessRequest;
	String username;
	String password;
	
}
