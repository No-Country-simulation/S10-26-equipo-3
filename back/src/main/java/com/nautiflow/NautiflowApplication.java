package com.nautiflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NautiflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(NautiflowApplication.class, args);
	}

}
