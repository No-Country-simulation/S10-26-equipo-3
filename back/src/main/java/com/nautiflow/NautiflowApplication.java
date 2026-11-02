package com.nautiflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NautiflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(NautiflowApplication.class, args);
	}

}
