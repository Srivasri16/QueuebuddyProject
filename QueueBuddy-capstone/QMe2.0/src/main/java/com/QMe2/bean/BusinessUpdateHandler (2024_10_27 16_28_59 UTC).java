package com.QMe2.bean;

import java.util.Calendar;
import java.util.List;

import com.QMe2.bean.Business.BusinessBuilder;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusinessUpdateHandler {
	private List<BusinessPhoneNumber> businessPhoneNumber;
	private String referenceID;
	private List<SimpleSchedule> simpleSchedule;
	private List<ScheduleException> scheduleException;
	private Business business;
	private LineManager lineManager;
}
