package com.QMe2.bean;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.springframework.beans.factory.annotation.Autowired;

import com.QMe2.enums.QueuerStatus;
import com.QMe2.exceptions.StoreIsClosed;
import com.QMe2.repo.QueuerRepo;
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
public class Queue {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	
	@OneToMany(mappedBy= "queue", orphanRemoval=true, cascade=CascadeType.PERSIST)
	private List<Queuer> queuerList;
	
	@JsonIgnore
	@OneToOne(mappedBy = "queue")
	private SimpleSchedule simpleSchedule;
	
	@JsonIgnore
	@OneToOne(mappedBy = "queue")
	private ScheduleException scheduleException;
	
	@Builder.Default
	private boolean isClosed = false;
	
	@Column(nullable = true)
	Double averageEntryTime;

	public void calculateETA()
	{
		double[] timeToEntry = new double[queuerList.size()];
		for(int i = 0; i< timeToEntry.length; i++ ){
			Queuer entry = queuerList.get(i);
			if(entry.getStoreEntryTime() != null){
				long arrival = entry.getArrivalTime().getEpochSecond();
				long entryTime = entry.getStoreEntryTime().getEpochSecond();
				long diff = entryTime - arrival;

				timeToEntry[i] = ((double) diff);
			}
		}
		Mean mean = new Mean();

		double result = mean.evaluate(timeToEntry,0,timeToEntry.length);
		averageEntryTime=result;

		
	}

	
	public boolean add(Queuer queuer) throws StoreIsClosed {
		if(isClosed) {
			throw new StoreIsClosed();
		}
		
		/*if(simpleSchedule != null && simpleSchedule.isClosed() == true) {
			throw new StoreIsClosed();
		}
		
		if(scheduleException != null && scheduleException.isClosed() == true) {
			throw new StoreIsClosed();
		}*/
		return queuerList.add(queuer);
	}
	
	public void reset() {
		this.isClosed = false;
		queuerList.clear();
	}
	
	
	
	public Business getBussiness() {
		if(simpleSchedule != null) {
			return simpleSchedule.getBusiness();
		}
		if(scheduleException != null) {
			return simpleSchedule.getBusiness();
		}
		throw new NullPointerException();
	}
	
	/**
	 * Closes the store and dequeues the entire queue.
	 * @return the reminder appointments
	 */
	public List<Queuer> cancelAndClose() {
		isClosed= true;
		
		List<Queuer> queuerListtmp = new ArrayList<Queuer>();
		for(Queuer q : queuerList){
			if(q.getStatus().equals(QueuerStatus.INLINE)) {
				q.setStatus(QueuerStatus.CANCELED);
				queuerListtmp.add(q);
			}
		}
		
		return queuerListtmp;
		
	}
	
	/**
	 * Closes the store but keeps the people still in line
	 */
	public void close() {
		isClosed = true;
	}
	
	/**
	 * Reopens the store
	 */
	public void open() {
		isClosed = false;
	}
	

	
	/**
	 * Cancels appointment
	 * @param queuer
	 * @return
	 */
	public void cancel(Queuer queuer) {
		queuer.setStatus(QueuerStatus.CANCELED);
		
	}
	
	

	
	
}
