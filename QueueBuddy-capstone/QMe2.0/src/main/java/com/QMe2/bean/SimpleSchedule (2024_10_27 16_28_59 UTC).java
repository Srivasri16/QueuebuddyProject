package com.QMe2.bean;

import java.time.DayOfWeek;
import java.time.LocalTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

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
public class SimpleSchedule {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private DayOfWeek dayofWeek;
	private LocalTime open;
	private LocalTime close;
	
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY, optional= false)
	@JoinColumn(name = "business_id", nullable = false)
	private Business business;
	
	//If the store is closed no people will be added to the queue
	private boolean isClosed;
	
	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY, optional= false, cascade = CascadeType.ALL)
	@JoinColumn(name = "queue_id", nullable = false)
	private Queue queue;
	
	
}
