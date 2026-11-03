package com.nautiflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class NautiflowApplication {

	public static void main(String[] args) {

		TimeZone.setDefault(
				TimeZone.getTimeZone("America/Bogota")
		);

		SpringApplication.run(
				NautiflowApplication.class,
				args
		);
	}
}