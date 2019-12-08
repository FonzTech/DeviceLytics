package devicelytics.task.prediction;

import java.io.File;

import devicelytics.task.Task;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.SerializationHelper;

public abstract class AbstractPrediction extends Task
{
	protected static final File MODEL_FILE = new File("devicelytics.model");
	
	protected Classifier classifier;
	
	@Override
	protected final void onStart()
	{
		if (MODEL_FILE.exists())
		{
			try
			{
				classifier = (Classifier) SerializationHelper.read(MODEL_FILE.getAbsolutePath());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			classifier = new LinearRegression();
		}
	}

	@Override
	protected final void onFinish()
	{
		try
		{
			SerializationHelper.write(MODEL_FILE.getAbsolutePath(), classifier);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}