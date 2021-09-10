package io.gab.challenge.logreader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.gab.challenge.logreader.controller.EventController;


@SpringBootApplication
public class LogReaderApplication implements CommandLineRunner {
	
	@Autowired
	private EventController eventController;
	
	public static void main(String[] args) {
		SpringApplication.run(LogReaderApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if(args != null && args.length > 0) {
			eventController.processLogFile(args[0]);
		} else {
			System.out.println("[ERROR] :: No log file path argument provided");
		}
	}	
}
