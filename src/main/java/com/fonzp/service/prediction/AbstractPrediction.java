package com.fonzp.service.prediction;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;

import com.fonzp.service.DatabaseTask;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.SerializationHelper;

public abstract class AbstractPrediction extends DatabaseTask
{
	@Autowired
	protected File modelFile;
	
	protected Classifier classifier;
	
	/**
	 * This method has to be implemented with the task to be performed after successfully
	 * got the database connection. The method will not be executed if connection was not
	 * available.
	 */
	protected abstract void doTaskInBackground();
	
	@Override
	protected final void doInBackground()
	{
		// Check for connection
		if (connection == null)
		{
			return;
		}

		// Do tasks in background
		doBeforeInBackground();
		doTaskInBackground();
		doAfterInBackground();
	}
	
	/**
	 * Load or create the model from file.
	 */
	private final void doBeforeInBackground()
	{
		if (modelFile.exists())
		{
			try
			{
				classifier = (Classifier) SerializationHelper.read(modelFile.getAbsolutePath());
				System.out.println(getClass().getSimpleName() + " - Classifier was loaded: " + classifier);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			classifier = new RandomForest();
			System.out.println(getClass().getSimpleName() + " - Classifier was created.");
		}
	}

	/**
	 * Store the model on file.
	 */
	private final void doAfterInBackground()
	{
		if (classifier != null)
		{
			try
			{
				SerializationHelper.write(modelFile.getAbsolutePath(), classifier);
				System.out.println(getClass().getSimpleName() + " - Classifier was saved: " + classifier);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}