package com.QMe2.helpers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.QMe2.bean.Queue;
import com.QMe2.bean.Queuer;
import com.QMe2.enums.QueuerStatus;
import com.QMe2.repo.QueuerRepo;
import com.QMe2.service.RepoController;

@Component
public class QueueHelper {
	@Autowired
	private RepoController rc;
	
	public List<Queuer> getQueuersInLine(Queue q){
			
			List<Queuer> list = rc.getQueuerRepo().findByQueue_idAndStatus(q.getId(), QueuerStatus.INLINE);
			return list;
			
	}
	
	public Long getQueuersInLineSize(Queue q) {
		Long num = rc.getQueuerRepo().countByQueue_idAndStatus(q.getId(), QueuerStatus.INLINE);
		return num;
	}
	
	public int getStatus(Queuer q) {
		int num = rc.getQueuerRepo().findByQueue_idAndStatus(q.getQueue().getId(), QueuerStatus.INLINE).indexOf(q);
		return num;
	}
	
	public Queuer next(Long id) {
		List<Queuer> list = rc.getQueuerRepo().findByQueue_idAndStatus(id, QueuerStatus.INLINE);
		System.out.println("I got the list of size: " + list.size());
		for( Queuer tmp : list) {
			if(tmp.isPaused() ) {
				if(tmp.getTimesPaused() > 5 || list.size() == 1) {
					tmp.setStatus(QueuerStatus.CALLED);
					return tmp;
				} else {
					tmp.setTimesPaused(tmp.getTimesPaused() + 1);
					rc.getQueuerRepo().saveAndFlush(tmp);
				}
			} else {
				tmp.setStatus(QueuerStatus.CALLED);
				return tmp;
			}
		}
		//in case there is nothing in the list 
		return list.remove(0);
	}
}
