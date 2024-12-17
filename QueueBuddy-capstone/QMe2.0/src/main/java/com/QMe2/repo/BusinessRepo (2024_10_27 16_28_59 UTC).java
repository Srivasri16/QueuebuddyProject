package com.QMe2.repo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.QMe2.bean.Business;

@Repository
public interface BusinessRepo extends JpaRepository<Business,Long>{
	@Autowired
	Business findByCode(String Code);
	Business findByPhoneNumber(String num);
	List<Business> findByCodeContaining(String title);
	Business findByReferenceID(String num);
}
