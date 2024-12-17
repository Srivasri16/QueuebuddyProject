package com.QMe2.bean;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.QMe2.helpers.SessionHandlerBean;
import com.QMe2.repo.BusinessPhoneNumberRepo;
import com.QMe2.repo.BusinessRepo;
import com.QMe2.repo.QueueRepo;
import com.QMe2.repo.QueuerRepo;
import com.QMe2.repo.SMSTrackerRepo;
import com.QMe2.repo.ScheduleExceptionRepo;
import com.QMe2.repo.ScheduleRepo;
import com.QMe2.service.RepoController;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
//is the right one

@Component
public class SessionHandlerControl {
	
	//private List<SessionHandlerBean> session;
	private Dictionary<String, WebSocketSession> sessions;
	
	
	public String addSession(WebSocketSession wb) {
		/*SessionHandlerBean returned = SessionHandlerBean.builder().id(id++).session(wb).build();
		
		session.add(returned);*/
		String sessionId = "";
		do {
			UUID uuid = UUID.randomUUID();
			sessionId = uuid.toString();
		}while(sessions.get(sessionId) != null);
		
		WebSocketSession session = sessions.put(sessionId, wb);
		return sessionId;
		//return returned;
	}
	
	public WebSocketSession removeSession(String id) {
		return sessions.remove(id);
	}
	
	public WebSocketSession setSession(String id, WebSocketSession newSession) {
		return sessions.put(id, newSession);
	}
	
	public WebSocketSession getSession(String id) {
		return sessions.get(id);
		/*if(session.contains(SessionHandlerBean.builder().id(id).build())) {
			int index = session.indexOf(SessionHandlerBean.builder().id(id).build());
			return session.get(index);
		} else {
			return null;
		}*/
	}
	public int size() {
		return sessions.size();
	}
	
	public SessionHandlerControl(Dictionary<String, WebSocketSession> list) {
		this.sessions = list;
		
	}
}
