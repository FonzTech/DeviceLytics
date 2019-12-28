package com.fonzp.task;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.fonzp.service.DatabaseTask;

import lombok.AllArgsConstructor;

public final class CSVExporter extends DatabaseTask
{
	private static final String PRIMARY_KEY_COLUMN_NAME = "id";
	
	@PersistenceContext
	private EntityManager em;
	
	private final ArrayList<String> exportData;
	private boolean result;
	private File output;
	
	public CSVExporter()
	{
		exportData = new ArrayList<>();
	}
	
	@Override
	protected final EntityManager getEntityManager()
	{
		return em;
	}
	
	@Override
	public final void doInBackground()
	{
		// Check for connection
		if (connection == null)
		{
			return;
		}

		// Build CSV data
		try
		{
			final Statement stmt = connection.createStatement();
			final ResultSet rs = stmt.executeQuery("SELECT * FROM oplog");
			final ResultSetMetaData rsmd = rs.getMetaData();
			
			// Avoid ID column to be in the dataset
			int columnToAvoid = -1;
			
			// Export column names
			{
				final String[] data = new String[rsmd.getColumnCount()];

				for (int i = 0; i < rsmd.getColumnCount(); ++i)
				{
					final String name = rsmd.getColumnName(i + 1);
					if (name.equals(PRIMARY_KEY_COLUMN_NAME))
					{
						columnToAvoid = i;
						continue;
					}
					data[i] = name;
				}

				exportData.add(String.join(",", data));
			}
			
			// Export actual data
			while (rs.next())
			{
				final String[] data = new String[rsmd.getColumnCount()];
				
				for (int i = 0; i < rsmd.getColumnCount(); ++i)
				{
					if (i == columnToAvoid)
					{
						continue;
					}
					data[i] = rs.getString(i + 1);
				}

				exportData.add(String.join(",", data));
			}
			
			// Write to file
			try
			{
				output = File.createTempFile("csv-", ".csv");
				Files.write(output.toPath(), exportData, StandardOpenOption.TRUNCATE_EXISTING);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return;
			}
			
			result = true;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Object getResult()
	{
		return new Result(result, exportData, output);
	}
	
	// Export result
	@AllArgsConstructor
	public static final class Result
	{
		protected final boolean result;
		protected final ArrayList<String> exportData;
		protected final File output;
	}
}