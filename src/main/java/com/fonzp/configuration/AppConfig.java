package com.fonzp.configuration;

import java.io.File;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig
{
	@Bean
	public File getModelFile()
	{
		return new File("devicelytics.model");
	}
}