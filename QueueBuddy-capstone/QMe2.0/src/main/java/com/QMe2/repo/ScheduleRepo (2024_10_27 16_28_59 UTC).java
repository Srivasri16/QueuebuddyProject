package com.QMe2.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.QMe2.bean.SimpleSchedule;


public interface ScheduleRepo extends JpaRepository<SimpleSchedule,Long>{

}
