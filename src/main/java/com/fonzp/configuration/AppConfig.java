	package com.fonzp.configuration;

import java.io.File;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AppConfig
{
	@Bean("modelFile")
	@Primary
	public File getModelFile()
	{
		final File file = new File("models/devicelytics.model");
		if (!file.exists())
		{
			file.getParentFile().mkdirs();
		}
		return file;
	}

	@Bean("configFile")
	public File getConfigFile()
	{
		final File file = new File("models/devicelytics.json");
		if (!file.exists())
		{
			file.getParentFile().mkdirs();
		}
		return file;
	}
}