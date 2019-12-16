package devicelytics.task.prediction;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import devicelytics.model.ColumnToPredict;
import devicelytics.task.ArffBuilder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Prediction extends AbstractPrediction
{
	private final InputStream inputStream;

	private boolean success;
	private String message;
	private HashMap<Instance, Double> features;
	
	@Override
	protected final void doTaskInBackground()
	{
		// Train model with supplied data
		try
		{
			final File csvFile = writeDataToCsv(inputStream);
			checkDataConsistency(csvFile);
			
			final ArffBuilder arff = new ArffBuilder(csvFile);
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
			
			final ColumnToPredict columnToPredict = getColumnToPredict(csvFile);

			// Load dataset in ARFF format
			final DataSource source = new DataSource(arffFile.getAbsolutePath());
			final Instances dataset = source.getDataSet();
			dataset.setClassIndex(columnToPredict.getIndex());
			
			// Now predict the target value
			if (dataset.numInstances() > 0)
			{
				features = new HashMap<>();
				
				for (int i = 0; i < dataset.numInstances(); ++i)
				{
					final Instance instance = dataset.instance(i);
					double value = classifier.classifyInstance(instance);
					features.put(instance, value);
				}
			}
			
			message = "The prediction was done correctly.";
			success = true;
		}
		catch (Exception e)
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
	@RequiredArgsConstructor
	public static final class Result
	{
		protected final Boolean success;
		protected final String message;
		protected final HashMap<Instance, Double> features;
	}
}