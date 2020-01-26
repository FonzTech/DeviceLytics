package com.fonzp.task;

import java.io.File;
import java.io.InputStream;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fonzp.service.DatabaseTask;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public final class ArffBuilder extends DatabaseTask
{
	@Setter
	private InputStream inputStream;
	
	private boolean result;
	private File output;
	private String error;

	@Override
	protected void doInBackground()
	{
		try
		{
			// Create temporary file for destination ARFF
			output = File.createTempFile("arff-", ".arff");
			output.deleteOnExit();

			// Create CSV Loader object
			final CSVLoader loader = new CSVLoader();
			
			// Check where to put CSV data
			if (inputStream != null)
			{
				loader.setSource(inputStream);
			}
			else
			{
				// Export loaded file to CSV file
				final File file = File.createTempFile("csv-", ".csv");
				connection.createStatement().execute("CALL CSVWRITE('" + file.getAbsolutePath() + "', 'SELECT * FROM dataset')");

				loader.setSource(file);
			}

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
		catch (final Exception e)
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