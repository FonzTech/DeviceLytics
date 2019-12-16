package devicelytics.task;

import java.io.File;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public final class ArffBuilder extends Task
{
	private final File source;
	private boolean result;
	private File output;
	private String error;

	/**
	 * Built ARFF dataset file for processing in Weka.
	 *
	 * @param source existing CSV file
	 */
	public ArffBuilder(final File source)
	{
		this.source = source;
	}

	@Override
	protected final void onStart()
	{
	}

	@Override
	protected void doInBackground()
	{
		try
		{
			// Create temporary file for destination ARFF
			output = File.createTempFile("arff-", ".arff");
			output.deleteOnExit();

			// Call Weka to convert CSV to ARFF
			final CSVLoader loader = new CSVLoader();
			loader.setSource(source);

			/*
			final String[] options = { "-H" };
			loader.setOptions(options);
			*/

			final Instances data = loader.getDataSet();

			final ArffSaver saver = new ArffSaver();
			saver.setInstances(data);
			saver.setFile(output);
			saver.writeBatch();

			// Set flag
			result = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			error = e.getMessage();
		}		
	}

	@Override
	protected final void onFinish()
	{
	}

	@Override
	public final Object getResult()
	{
		return new Result(result, output, error);
	}
	
	// Export result
	@RequiredArgsConstructor
	@Getter
	@ToString
	@EqualsAndHashCode
	public static final class Result
	{
		protected final Boolean result;
		protected final File output;
		protected final String error;
	}
}