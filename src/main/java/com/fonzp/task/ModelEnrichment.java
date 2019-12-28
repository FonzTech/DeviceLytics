package com.fonzp.task;

import java.io.File;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.fonzp.model.ColumnToPredict;
import com.fonzp.service.DatabaseTask;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

@RequiredArgsConstructor
public final class ModelEnrichment extends DatabaseTask
{
	@PersistenceContext
	private EntityManager em;
	
	private final File arffFile;
	private final ColumnToPredict columnToPredict;
	private final Classifier classifier;
	
	private boolean result;
	private String error;
	
	@Override
	protected final EntityManager getEntityManager()
	{
		return em;
	}

	@Override
	protected final void doInBackground()
	{
		try
		{
			// Load dataset
			final DataSource source = new DataSource(arffFile.getAbsolutePath());
			final Instances dataset = source.getDataSet();
			
			// Set class index to the supplied one
			dataset.setClassIndex(columnToPredict.getIndex());
			
			// Build model
			final LinearRegression model = (LinearRegression) classifier;
			model.buildClassifier(dataset);
			
			result = true;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			error = e.getMessage();
		}
	}

	@Override
	public final Object getResult()
	{
		return new Result(result, error);
	}
	
	@RequiredArgsConstructor
	@Getter
	public static final class Result
	{
		protected final Boolean result;
		protected final String error;
	}
}