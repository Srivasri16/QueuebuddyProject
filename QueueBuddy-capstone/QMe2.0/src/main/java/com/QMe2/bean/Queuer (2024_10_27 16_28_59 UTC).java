package com.QMe2.bean;



import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.websocket.Session;

import com.QMe2.enums.QueuerStatus;
import com.QMe2.helpers.SessionHandlerBean;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Queuer {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String phoneNum;
	private String email;
	
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY, optional= false)
	@JoinColumn(name = "queue_id", nullable = false)
	private Queue queue;
	
	@Column(unique=true)
	private String sessionId;


	@Builder.Default
	private boolean paused = false;
	
	@Builder.Default
	private int timesPaused = 0;

	@Column(nullable = true)
	private Instant ETA;
	
	@Column(nullable = true)
	private Instant arrivalTime;
	
	@Column(nullable = true)
	private Instant storeEntryTime;
	
	
	
	@Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!Queuer.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        final Queuer other = (Queuer) obj;
        if(sessionId != null && other.sessionId != null) {
        	return this.id == other.id || this.sessionId.equals( other.sessionId) ; 
        }
        return this.id == other.id;
    }
	
	@Column(nullable = true)
	private QueuerStatus status;
	
	@Override
    public int hashCode() {
        return (int) (63 * this.id);
    }
	
	
	
	
}
