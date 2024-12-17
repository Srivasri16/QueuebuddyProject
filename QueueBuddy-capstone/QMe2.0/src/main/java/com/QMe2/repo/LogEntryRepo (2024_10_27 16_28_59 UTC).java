package com.QMe2.repo;

import com.QMe2.bean.Log;
import com.QMe2.bean.Queue;
import org.springframework.data.jpa.repository.JpaRepository;

import com.QMe2.bean.LogEntry;

import java.util.Collection;
import java.util.List;

public interface LogEntryRepo extends JpaRepository<LogEntry,Long> {
    //List<LogEntry> findByQueue(Queue queue);

    List<LogEntry> findAllByLog(Log log);
}
