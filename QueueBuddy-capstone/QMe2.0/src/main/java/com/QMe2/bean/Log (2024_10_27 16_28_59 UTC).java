package com.QMe2.bean;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.persistence.*;

import com.QMe2.bean.Queue.QueueBuilder;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity

public class Log {

	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	
	@OneToMany(mappedBy= "log", orphanRemoval=true, cascade=CascadeType.MERGE)
	private List<LogEntry> logEntry;
	
	
	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY, optional= false, cascade = CascadeType.MERGE)
	@JoinColumn(name = "queue_id", nullable = false)
	private Queue queue;

	


}
