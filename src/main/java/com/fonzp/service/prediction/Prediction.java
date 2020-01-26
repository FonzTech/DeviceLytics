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
					double label = -1;
					
					try
					{
						label = classifier.classifyInstance(instance);
						instance.setClassValue(label);
					}
					catch (final NullPointerException ex)
					{
						throw new RuntimeException("The model has been not trained yet.");
					}
					
					final HashMap<String, String> attributes = new HashMap<>();
					for (int j = 0; j < instance.numAttributes(); ++j)
					{
						String value;
						
						try
						{
							value = instance.stringValue(j);
						}
						catch (Exception e)
						{
							value = Double.toString(label);
						}

						attributes.put(instance.attribute(j).name().toUpperCase(), value);
					}

					// features.add(new Feature(attributes, (int) label));
					features.add(new Feature(attributes));
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
		// protected final double label;
	}
}