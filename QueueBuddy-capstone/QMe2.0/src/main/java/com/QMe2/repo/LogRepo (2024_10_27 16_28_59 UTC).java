package com.QMe2.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.QMe2.bean.Log;
import com.QMe2.bean.Queue;

public interface LogRepo extends JpaRepository<Log,Long>{

	Optional<Log> findByQueue(Queue queue);
}
