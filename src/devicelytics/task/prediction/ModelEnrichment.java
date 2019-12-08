package devicelytics.task.prediction;

import java.io.File;

import devicelytics.model.ColumnToPredict;
import lombok.RequiredArgsConstructor;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

@RequiredArgsConstructor
public final class ModelEnrichment extends AbstractPrediction
{
	private final File arffFile;
	private final ColumnToPredict columnToPredict;

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
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public final Object getResult()
	{
		return null;
	}
}