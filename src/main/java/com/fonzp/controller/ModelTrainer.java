package com.fonzp.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
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

	@Autowired
	@Qualifier("configFile")
	private File configFile;
			
	@PostMapping(value = "/api/train_model")
	public final Object trainModel(
			@RequestParam("target") final String target,
			@RequestParam("targetName") final String targetName,
			@RequestParam("folds") final String folds,
			@RequestParam("vars") final String[] vars) throws IOException, InterruptedException
	{
		// Target feature is always included
		final ArrayList<String> list = new ArrayList<>(Arrays.asList(vars));
		if (!list.contains(target))
		{
			list.add(target);
		}
		
		// Get class index
		final int classIndex = Integer.parseInt(target);
		
		// Train classifier
		final TrainClassifier task = context.getBean(TrainClassifier.class);
		task.setIncludeVarsIndexes(list);
		task.setClassIndex(classIndex);
		task.setFolds(Integer.parseInt(folds));
		task.start();
		task.join();

		// Write settings to file
		{
			final HashMap<String, Object> params = new HashMap<>();
			params.put("classIndex", classIndex);
			params.put("target", targetName);
			new ObjectMapper().writeValue(new FileOutputStream(configFile), params);
		}
		
		return task.getResult();
	}
}