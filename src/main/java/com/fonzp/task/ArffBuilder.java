package com.fonzp.task;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fonzp.service.Task;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public final class ArffBuilder extends Task
{
	@Autowired
	private DataSource ds;
	
	private boolean result;
	private File output;
	private String error;

	@Override
	protected final void onStart()
	{
	}

	@Override
	protected void doInBackground()
	{
		Connection connection = null;
		
		try
		{
			// Export loaded file to CSV file
			final File file = File.createTempFile("csv-", ".csv");
			{
				connection = ds.getConnection();
				connection.createStatement().execute("CALL CSVWRITE('" + file.getAbsolutePath() + "', 'SELECT * FROM dataset')");
			}
			
			// Create temporary file for destination ARFF
			output = File.createTempFile("arff-", ".arff");
			output.deleteOnExit();

			// Call Weka to convert CSV to ARFF
			final CSVLoader loader = new CSVLoader();
			loader.setSource(file);

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
		finally
		{
			if (connection != null)
			{
				try
				{
					connection.close();
				}
				catch (final SQLException e)
				{
					e.printStackTrace();
				}
				connection = null;
			}
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