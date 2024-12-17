package com.QMe2.helpers;


import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.QMe2.bean.Queue;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class SessionHandlerBean {
	private long id;
	
	private WebSocketSession session;
	
	
	@Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        

        if (!SessionHandlerBean.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        final SessionHandlerBean other = (SessionHandlerBean) obj;
        return this.id == other.id;
    }
	
	@Override
    public int hashCode() {
        return (int) (63 * this.id);
    }
	
	
}
