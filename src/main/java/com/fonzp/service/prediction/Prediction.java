package com.fonzp.service.prediction;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fonzp.task.ArffBuilder;

import lombok.Data;
import lombok.Setter;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class Prediction extends AbstractPrediction
{
	@Autowired
	private ApplicationContext context;
	
	@Setter
	private InputStream inputStream;
	
	@Setter
	private Integer classIndex;

	private boolean success;
	private String message;
	private ArrayList<Feature> features;
	
	@Override
	protected final void doTaskInBackground()
	{
		// Train model with supplied data
		try
		{
			final ArffBuilder arff = context.getBean(ArffBuilder.class);
			arff.setInputStream(inputStream);
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

			// Load dataset in ARFF format
			final DataSource source = new DataSource(arffFile.getAbsolutePath());
			final Instances dataset = source.getDataSet();
			dataset.setClassIndex(classIndex);
			
			// Now predict the target value
			if (dataset.numInstances() > 0)
			{
				features = new ArrayList<>();
				
				for (int i = 0; i < dataset.numInstances(); ++i)
				{
					final Instance instance = dataset.instance(i);
					String predictedValue = null;
					double label = -1;
					
					try
					{
						// Get current class value
						final double oldValue = instance.classValue();
						
						// Get value for predicted class value
						label = classifier.classifyInstance(instance);
						instance.setClassValue(label);
						predictedValue = getStringValue(instance, classIndex);
						
						// Restore old value
						instance.setClassValue(oldValue);						
					}
					catch (final NullPointerException ex)
					{
						throw new RuntimeException("The model has been not trained yet.");
					}
					
					final HashMap<String, String> attributes = new HashMap<>();
					for (int j = 0; j < instance.numAttributes(); ++j)
					{
						final String value = getStringValue(instance, j);
						attributes.put(instance.attribute(j).name().toUpperCase(), value);
					}

					// features.add(new Feature(attributes, (int) label));
					features.add(new Feature(attributes, predictedValue));
				}
			}
			
			// Success
			message = "The prediction was done correctly.";
			success = true;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			message = e.getMessage();
		}
	}
	
	/**
	 * Get human-readable string value from the instance for the provided attribute index.
	 *
	 * @param instance the instance to get the attribute value from.
	 * @param attributeIndex the attribute index to be obtained and checked.
	 *
	 * @return human-readable string.
	 */
	protected final String getStringValue(final Instance instance, final int attributeIndex)
	{
		String value;

		try
		{
			value = instance.stringValue(attributeIndex);
		}
		catch (final Exception e)
		{
			value = Double.toString(instance.value(attributeIndex));
		}
		
		return value;
	}

	@Override
	public final Object getResult()
	{
		return new Result(success, message, features);
	}
	
	// Getter result
	@Data
	public static final class Result
	{
		protected final Boolean success;
		protected final String message;
		protected final ArrayList<Feature> features;
	}

	// Feature class
	@Data
	protected static final class Feature
	{
		protected final HashMap<String, String> instance;
		protected final String label;
	}
}