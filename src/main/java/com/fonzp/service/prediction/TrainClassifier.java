package com.fonzp.service.prediction;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fonzp.task.ArffBuilder;
import com.fonzp.task.ModelEnrichment;

import lombok.Data;
import lombok.Setter;
import weka.filters.unsupervised.attribute.Remove;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public final class TrainClassifier extends AbstractPrediction
{
	@Autowired
	private ApplicationContext context;
	
	@Setter
	private Integer classIndex;
	
	@Setter
	private ArrayList<String> includeVarsIndexes;
	
	@Setter
	private Integer folds;
	
	private boolean success;
	private String message;
	private HashMap<String, Object> evaluation;
	
	@Override
	public final void doTaskInBackground()
	{
		// Check for parameters consistency
		if (classIndex == null || classIndex < 0)
		{
			message = "Target variable is invalid.";
			return;
		}
		
		if (folds < 0)
		{
			message = "Number of folds must be a non negative number.";
			return;
		}
		
		// Train model with supplied data
		try
		{
			final ArffBuilder arff = context.getBean(ArffBuilder.class);
			arff.start();
			arff.join();
			
			final File arffFile;
			{
				final ArffBuilder.Result result = (ArffBuilder.Result) arff.getResult();
				if (!result.getResult())
				{
					throw new RuntimeException("Could not build the ARFF file from the supplied CSV. Error is: " + result.getError());
				}
				arffFile = result.getOutput();
			}
			
			final Remove remove = new Remove();
			remove.setInvertSelection(true);
			remove.setAttributeIndices(String.join(",", includeVarsIndexes
					.stream()
					.mapToInt(s -> Integer.parseInt(s) + 1)
					.mapToObj(n -> Integer.toString(n))
					.toArray(String[]::new)
			));
			
			final ModelEnrichment pmt = context.getBean(ModelEnrichment.class);
			pmt.setArffFile(arffFile);
			pmt.setClassIndex(classIndex);
			pmt.setClassifier(classifier);
			pmt.setFilter(remove);
			pmt.setFolds(folds);
			pmt.start();
			pmt.join();
			
			{
				final ModelEnrichment.Result result = (ModelEnrichment.Result) pmt.getResult();
				if (result.getResult())
				{
					success = true;
					evaluation = result.getEvaluation();
				}
				else
				{
					throw new RuntimeException("Could not apply the learning algorithm. Error is: " + result.getError());
				}
			}
			
			message = "The Model was trained correctly.";
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			message = e.getMessage();
		}
	}

	@Override
	public final Object getResult()
	{
		return new Result(success, message, classIndex, evaluation, includeVarsIndexes);
	}
	
	// Getter result
	@Data
	public static final class Result
	{
		protected final Boolean success;
		protected final String message;
		protected final Integer classIndex;
		protected final HashMap<String, Object> evaluation;
		protected final ArrayList<String> trainVars;
	}
}