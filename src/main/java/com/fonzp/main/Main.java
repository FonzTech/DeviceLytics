package com.fonzp.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.fonzp"})
public class Main
{
	public static final void main(final String[] args) throws ClassNotFoundException
	{
		// Initialize Spring Boot
	    SpringApplication.run(Main.class);
	}
}