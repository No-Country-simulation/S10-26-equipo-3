package com.nautiflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

/**
 * Punto de entrada del backend de NautiFlow. Configura la zona horaria de negocio y arranca Spring Boot con la programación de tareas habilitada.
 */
@SpringBootApplication
@EnableScheduling
public class NautiflowApplication {

    /**
     * Punto de entrada de la aplicación. Fija la zona horaria de Bogotá y arranca Spring Boot.
     */
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