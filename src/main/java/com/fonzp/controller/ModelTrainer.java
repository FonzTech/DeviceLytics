package com.fonzp.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fonzp.service.prediction.TrainClassifier;

/**
 * This class acts as a REST Api to provide different types of services for the
 * front-end user.
 */
@RestController
public final class ModelTrainer
{
	@Autowired
	private ApplicationContext context;
			
	@PostMapping(value = "/api/train_model")
	public final Object trainModel(@RequestParam("target") final String target, @RequestParam("folds") final String folds) throws IOException, InterruptedException
	{
		final TrainClassifier task = context.getBean(TrainClassifier.class);
		task.setClassIndex(Integer.parseInt(target));
		task.setFolds(Integer.parseInt(folds));
		task.start();
		task.join();
		return task.getResult();
	}
}