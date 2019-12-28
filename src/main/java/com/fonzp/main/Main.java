package com.fonzp.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.fonzp"})
@EnableJpaRepositories("com.fonzp.repository")
@EntityScan("com.fonzp.model.entity")
public class Main
{
	public static final void main(final String[] args) throws ClassNotFoundException
	{
		// Initialize Spring Boot
	    SpringApplication.run(Main.class);
	}
}