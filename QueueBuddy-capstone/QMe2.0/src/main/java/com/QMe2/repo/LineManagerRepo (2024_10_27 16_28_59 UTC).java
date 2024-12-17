package com.QMe2.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.QMe2.bean.LineManager;

public interface LineManagerRepo extends JpaRepository<LineManager,Long> {
	LineManager findByUsername(String username);
	Optional<LineManager> findBySessionID(String id);
	List<LineManager> findBySessionIDNotNull();
}
