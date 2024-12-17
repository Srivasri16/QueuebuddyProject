package com.QMe2;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.QMe2.bean.LineManager;
import com.QMe2.bean.Queuer;
import com.QMe2.service.RepoController;



@SpringBootApplication
//@Component
public class Application {
	@Autowired
	private RepoController rc;
	

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		
	}
	
	@PostConstruct
    private void init() {
        System.out.println("AppInitializator setting session ID's to null");
        List<LineManager> lineManager = rc.getLineManagerRepo().findBySessionIDNotNull();
		List<Queuer> queuers =rc.getQueuerRepo().findBySessionIdNotNull();
		
		for(LineManager lm: lineManager) {
			lm.setSessionID(null);
		}
		
		for(Queuer lm: queuers) {
			lm.setSessionId(null);
		}
		
		rc.getLineManagerRepo().saveAll(lineManager);
		rc.getLineManagerRepo().flush();
		rc.getQueuerRepo().saveAll(queuers);
		rc.getQueuerRepo().flush();
		
    }

}
