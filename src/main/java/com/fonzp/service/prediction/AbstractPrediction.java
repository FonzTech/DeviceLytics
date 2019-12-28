package com.fonzp.service.prediction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.fonzp.model.ColumnToPredict;
import com.fonzp.service.DatabaseTask;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.SerializationHelper;

public abstract class AbstractPrediction extends DatabaseTask
{
	protected static final File MODEL_FILE = new File("devicelytics.model");
	
	protected Classifier classifier;
	
	protected abstract void doTaskInBackground();
	
	@Override
	protected final void doInBackground()
	{
		// Check for connection
		if (connection == null)
		{
			return;
		}

		// Do tasks in background
		doBeforeInBackground();
		doTaskInBackground();
		doAfterInBackground();
	}

	/**
	 * Write supplied CSV data into a file on file system.
	 *
	 * @param inputStream the input stream coming from the view -> controller.
	 *
	 * @return the output CSV file on file system.
	 *
	 * @throws IOException error while creating and outputting file contents.
	 */
	protected final File writeDataToCsv(final InputStream inputStream) throws IOException
	{
		final byte[] buffer = new byte[inputStream.available()];
		inputStream.read(buffer);

		final File output = File.createTempFile("csv-", ".csv");
		output.deleteOnExit();
		
		final OutputStream stream = new FileOutputStream(output);
		stream.write(buffer);
		stream.close();
		
		return output;
	}
	
	/**
	 * Check for data consistency between supplied CSV file and database variables.
	 *
	 * @param file the CSV file on disk.
	 *
	 * @throws IOException exception while opening the input file for reading.
	 * @throws SQLException general SQL error.
	 */
	protected final void checkDataConsistency(final File file) throws IOException, SQLException
	{
		// Open stream for reading
		final Scanner scanner = new Scanner(new FileInputStream(file));
		
		// Check for all fields presence
		if (scanner.hasNextLine())
		{
			final List<String> vars = checkForMissingVariables(scanner);
			if (vars.size() > 0)
			{
				scanner.close();
				throw new RuntimeException("Differences were found between database variables and CSV header: " + String.join(", ", vars));
			}
		}
		else
		{
			scanner.close();
			throw new RuntimeException("Could not get CSV header or uploaded file was invalid.");
		}
		
		scanner.close();
	}
	
	/**
	 * Get the column name contained in the CSV file to predict.
	 *
	 * @param csvFile the CSV file where to get the column index to be predicted from.
	 *
	 * @return the CSV label for the column containing the value to predict.
	 *
	 * @throws IOException generic exception while reading the CSV file.
	 * @throws SQLException generic SQL exception while performing query.
	 */
	protected final ColumnToPredict getColumnToPredict(final File csvFile) throws IOException, SQLException
	{		
		final Statement stmt = connection.createStatement();
		final ResultSet rs = stmt.executeQuery("SELECT csv_label FROM opvar WHERE show_for_chart = 2");
		
		if (rs.next())
		{
			final String name = rs.getString(1);
			
			final Scanner scanner = new Scanner(csvFile);
			while (scanner.hasNextLine())
			{
				final String[] array = scanner.nextLine().split(",");
				for (int i = 0; i < array.length; ++i)
				{
					if (array[i].equals(name))
					{
						scanner.close();
						return new ColumnToPredict(name, i);
					}
				}
			}
			scanner.close();
		}
		
		throw new RuntimeException("No variable marked as dependent was found in the database.");
	}
	
	/**
	 * Check for discrepancy between variables in the CSV header and variables stored
	 * in the application database.
	 *
	 * @param scanner the scanner iterator where to get the CSV data from.
	 *
	 * @return array of strings representing the variables which don't match the database.
	 *
	 * @throws SQLException exception when performing the query.
	 */
	protected final List<String> checkForMissingVariables(final Scanner scanner) throws SQLException
	{
		// Create list for variables
		final ArrayList<String> difference = new ArrayList<>();

		// List of CSV variables
		final ArrayList<String> csvVars = new ArrayList<>(Arrays.asList(scanner.nextLine().toLowerCase().split(",")));
		
		// Get available variables from the application database
		final Statement stmt = connection.createStatement();
		final ResultSet rs = stmt.executeQuery("SELECT csv_label FROM opvar");
		
		// Check for differences
		while (rs.next())
		{
			final String name = rs.getString(1).toLowerCase();
			if (csvVars.contains(name))
			{
				csvVars.remove(name);
			}
			else
			{
				difference.add(name);
			}
		}
		
		// Any variables which does not appear in the CSV
		difference.addAll(csvVars);
		
		// Return difference
		return difference;
	}
	
	/**
	 * Load or create the model from file.
	 */
	private final void doBeforeInBackground()
	{
		if (MODEL_FILE.exists())
		{
			try
			{
				classifier = (Classifier) SerializationHelper.read(MODEL_FILE.getAbsolutePath());
				System.out.println(getClass().getSimpleName() + " - Classifier was loaded: " + classifier);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			classifier = new LinearRegression();
			System.out.println(getClass().getSimpleName() + " - Classifier was created.");
		}
	}

	/**
	 * Store the model on file.
	 */
	private final void doAfterInBackground()
	{
		if (classifier != null)
		{
			try
			{
				SerializationHelper.write(MODEL_FILE.getAbsolutePath(), classifier);
				System.out.println(getClass().getSimpleName() + " - Classifier was saved: " + classifier);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}