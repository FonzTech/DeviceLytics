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
		final File file = new File("models/devicelytics.model");
		if (!file.exists())
		{
			file.getParentFile().mkdirs();
		}
		return file;
	}
}