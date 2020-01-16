package com.fonzp.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {"com.fonzp"})
@EntityScan("com.fonzp.model.entity")
public class Main
{
	public static final void main(final String[] args) throws ClassNotFoundException
	{
		// Initialize Spring Boot
	    SpringApplication.run(Main.class);
	}
}