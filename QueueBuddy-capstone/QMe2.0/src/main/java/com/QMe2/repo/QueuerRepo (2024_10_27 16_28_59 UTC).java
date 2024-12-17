package com.QMe2.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.QMe2.bean.Queuer;
import com.QMe2.enums.QueuerStatus;

public interface QueuerRepo extends JpaRepository<Queuer,Long>{
	Queuer findByPhoneNum(String number);
	Optional<Queuer> findBySessionId(String id);
	List<Queuer> findBySessionIdNotNull();
	List<Queuer> findByQueue_idAndStatus(Long id, QueuerStatus queuerStatus);
	
	Long countByQueue_idAndStatus(Long id, QueuerStatus queuerStatus);
	Queuer findByPhoneNumAndStatus(String phoneNum,QueuerStatus status);
}
