package com.fonzp.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fonzp.service.prediction.Prediction;

/**
 * This class acts as a REST Api to provide different types of services for the
 * front-end user.
 */
@RestController
public final class ModelPrediction
{
	@Autowired
	private ApplicationContext context;
			
	@PostMapping(value = "/api/test_model", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public final Object predictModel(@RequestParam("test_file") MultipartFile[] files) throws IOException, InterruptedException
	{
		final Prediction task = context.getBean(Prediction.class);
		task.setInputStream(files[0].getInputStream());
		task.start();
		task.join();
		return task.getResult();
	}
}
