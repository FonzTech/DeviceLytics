package com.fonzp.task;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Random;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fonzp.service.DatabaseTask;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public final class ModelEnrichment extends DatabaseTask
{
	@Setter
	private File arffFile;
	
	@Setter
	private Integer classIndex;
	
	@Setter
	private Classifier classifier;
	
	@Setter
	private Integer folds;
	
	private boolean result;
	private String error;
	private HashMap<String, Object> measures;

	@Override
	protected final void doInBackground()
	{
		try
		{
			// Load dataset
			final DataSource source = new DataSource(arffFile.getAbsolutePath());
			final Instances dataset = source.getDataSet();
			
			// Set class index to the supplied one
			dataset.setClassIndex(classIndex);
			
			// Build model
			classifier.buildClassifier(dataset);
			
			// Cross validation
			final Evaluation evaluation = new Evaluation(dataset);
			evaluation.crossValidateModel(classifier, dataset, folds, new Random(1L));
			
			// Get list of possible classes for target feature
			measures = new HashMap<>();
			
			final Enumeration<?> enumeration = dataset.attribute(classIndex).enumerateValues();
			for (int i = 0; enumeration.hasMoreElements(); ++i)
			{
				// Get key
				final String key = enumeration.nextElement().toString();

				// Get results
				final HashMap<String, Object> values = new HashMap<>();
				values.put("truePositives", evaluation.numTruePositives(i));
				values.put("falsePositives", evaluation.numFalsePositives(i));
				values.put("trueNegatives", evaluation.numTrueNegatives(i));
				values.put("falseNegatives", evaluation.numFalseNegatives(i));
				values.put("recall", evaluation.recall(i));
				values.put("precision", evaluation.precision(i));
				values.put("fMeasure", evaluation.fMeasure(i));
				
				// Save into measures
				measures.put(key, values);
			}
			
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
		return new Result(result, error, measures);
	}
	
	@RequiredArgsConstructor
	@Getter
	public static final class Result
	{
		protected final Boolean result;
		protected final String error;
		protected final HashMap<String, Object> evaluation;
	}
}