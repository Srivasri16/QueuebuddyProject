package com.QMe2.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.QMe2.bean.BusinessPhoneNumber;

public interface BusinessPhoneNumberRepo extends JpaRepository<BusinessPhoneNumber,Long>{
	BusinessPhoneNumber findByPhoneNumber(String num);
}
