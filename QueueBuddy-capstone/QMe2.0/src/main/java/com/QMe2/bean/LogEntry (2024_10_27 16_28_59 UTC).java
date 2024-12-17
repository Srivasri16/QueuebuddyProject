package com.QMe2.bean;

import java.time.Instant;

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
public class LogEntry {

	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY, optional= false)
	@JoinColumn(name = "log_id", nullable = false)
	private Log log;
	
	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY, optional= true, cascade = CascadeType.MERGE)
	@JoinColumn(name = "queuer_id", nullable = true)
	private Queuer queuer;
	
	

	

	
	
	
	
	
}
