package devicelytics.task.prediction;

import java.io.File;
import java.io.InputStream;

import devicelytics.model.ColumnToPredict;
import devicelytics.task.ArffBuilder;
import devicelytics.task.ModelEnrichment;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class TrainModel extends AbstractPrediction
{
	private final InputStream inputStream;
	
	private boolean success;
	private String message;
	
	@Override
	public final void doTaskInBackground()
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
			final ModelEnrichment pmt = new ModelEnrichment(arffFile, columnToPredict, classifier);
			pmt.start();
			pmt.join();
			
			{
				final ModelEnrichment.Result result = (ModelEnrichment.Result) pmt.getResult();
				if (result.getResult())
				{
					success = true;
				}
				else
				{
					throw new RuntimeException("Could not apply the learning algorithm. Error is: " + result.getError());
				}
			}
			
			message = "The Model was trained correctly.";
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
		return new Result(success, message);
	}
	
	// Getter result
	@RequiredArgsConstructor
	public static final class Result
	{
		protected final Boolean success;
		protected final String message;
	}
}