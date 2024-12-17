package com.QMe2.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.QMe2.repo.BusinessPhoneNumberRepo;
import com.QMe2.repo.BusinessRepo;
import com.QMe2.repo.LineManagerRepo;
import com.QMe2.repo.LogRepo;
import com.QMe2.repo.LogEntryRepo;
import com.QMe2.repo.QueueRepo;
import com.QMe2.repo.QueuerRepo;
import com.QMe2.repo.SMSTrackerRepo;
import com.QMe2.repo.ScheduleExceptionRepo;
import com.QMe2.repo.ScheduleRepo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Data
@NoArgsConstructor
public class RepoController {
	
	
	@Autowired
	private BusinessRepo businessRepo;
	@Autowired
	private QueueRepo queueRepo;
	@Autowired
	private QueuerRepo queuerRepo;
	@Autowired
	private ScheduleExceptionRepo schedulExRepo;
	@Autowired
	private ScheduleRepo scheduleRepo;
	@Autowired
	private BusinessPhoneNumberRepo businessPhoneNumberRepo;
	@Autowired
	private SMSTrackerRepo smsTrackerRepo;
	
	@Autowired
	private LineManagerRepo lineManagerRepo;
	
	@Autowired
	private LogEntryRepo logEntryRepo;
	
	@Autowired
	private LogRepo logClassRepo;
	
	
	
}
