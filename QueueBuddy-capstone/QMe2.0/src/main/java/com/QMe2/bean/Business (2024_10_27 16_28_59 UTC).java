package com.QMe2.bean;


/*
 * 
 * */

import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.QMe2.exceptions.NoScheduleAvailable;
import com.QMe2.helpers.TimeHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Business {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(unique=true, nullable = false)
	private String email;
	
	@Column(nullable = false)
	private String address;
	
	@Column(nullable = false)
	private String country;
	
	@Column(nullable = false)
	private String city;
	
	
	@Column(nullable = false)
	private String phoneNumber;
	@Column(nullable = false)
	private String nameOfStore;
	@Column(nullable = false)
	private String nameOfOwner;
	@Column(unique=true, nullable = false)
	private String code;
	@Column(unique=true ,nullable = false)
	private String referenceID;
	private Calendar timeInEffectOfTmp;
	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY, optional= true, orphanRemoval=true, cascade=CascadeType.PERSIST)
	private SimpleSchedule tmpSchedule;
	
	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY, optional= true, orphanRemoval=true, cascade=CascadeType.PERSIST)
	private ScheduleException tmpScheduleException;
	
	
	@OneToMany(mappedBy= "business", orphanRemoval=true, cascade=CascadeType.PERSIST)
	private List<BusinessPhoneNumber> businessPhoneNumber;
	

	@JsonIgnore
	@OneToMany(mappedBy= "business", orphanRemoval=true, cascade=CascadeType.PERSIST)
	private List<SMSTracker> SMSTracker;	

	


	@OneToMany(mappedBy= "business", orphanRemoval=true, cascade=CascadeType.MERGE)
	private List<SimpleSchedule> simpleSchedule;
	
	@JsonIgnore
	@OneToMany(mappedBy= "business", orphanRemoval=true, cascade=CascadeType.PERSIST)
	private List<ScheduleException> scheduleException;
	
	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY, optional= true)
	private LineManager lineManager;
	
	public Queue retriveQueue() throws NoScheduleAvailable {
		Calendar now = TimeHelper.nowAt0();
		if(timeInEffectOfTmp == null || compareTimes(now, timeInEffectOfTmp) == false) {
			if(!searchForSchedule()) {
				throw new NoScheduleAvailable();
			}
		}
		
		if(tmpSchedule != null) {
			
			//System.out.println("tmpSchedule");
			return tmpSchedule.getQueue();
			
		} else if(tmpScheduleException != null) {
			
			//System.out.println("tmpScheduleException");
			return tmpScheduleException.getQueue();

		}
		
		throw new NoScheduleAvailable("Possible Logic Error");

	}
	
	private void cleanQueue() {
		if(tmpScheduleException != null) {
			tmpScheduleException.getQueue().reset();
		}
		if(tmpSchedule != null) {
			tmpSchedule.getQueue().reset();
		}
	}
	
	private boolean searchForSchedule() {
		cleanQueue();
		Calendar now = TimeHelper.nowAt0();
		for (ScheduleException se : scheduleException) {
			if(se.getDayInEffect().equals(now)) {
				tmpSchedule = null;
				tmpScheduleException = se;
				timeInEffectOfTmp = now;
				return true;
			}
		}
		
		int dayOfWeekTrans = now.get(Calendar.DAY_OF_WEEK)-1;
		if(dayOfWeekTrans == 0) {
			dayOfWeekTrans = 7;
		}
		for (SimpleSchedule sch : simpleSchedule) {
			if(sch.getDayofWeek().name().equals(DayOfWeek.of(dayOfWeekTrans).name())) {
				tmpSchedule = sch;
				tmpScheduleException = null;
				timeInEffectOfTmp = now;
				return true;
			}
		}
		return false;
	}
	private boolean compareTimes(Calendar cal1, Calendar cal2) {
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
	              cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
	}
	
	//TODO: Set up a recip patment system
}
