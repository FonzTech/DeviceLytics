package com.fonzp.controller;

import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * This class acts as a REST Api to provide different types of services for the
 * front-end user.
 */
@RestController
public class LoadDataset
{
	@Autowired
	private DataSource ds;
	
	@Autowired
	private File modelFile;
	
	@PostMapping(value = "/api/load_dataset", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public final Object loadDataset(@RequestParam("dataset_file") MultipartFile[] files, @RequestParam("separator") String fieldSeparator)
	{
		int rowcount = -1;
		
		try
		{
			// Check for supplied data validity
			if (files.length != 1 || !files[0].getOriginalFilename().toLowerCase().endsWith(".csv"))
			{
				return new Response(false, "You must upload exactly one CSV file.");
			}
			
			// Write CSV to file system
			final File file = File.createTempFile("test-", ".csv");
			file.deleteOnExit();
			files[0].transferTo(file);
			
			// Delete old model
			if (modelFile.exists())
			{
				modelFile.delete();
			}
			
			// Drop any previous dataset table
			{
				final String query = "DROP TABLE IF EXISTS dataset";
				ds.getConnection().createStatement().execute(query);
			}
			
			// Create table from CSV
			{
				final String query = "CREATE TABLE dataset AS SELECT * FROM CSVREAD('" + file.getAbsolutePath() + "', NULL, 'fieldSeparator=" + (fieldSeparator.length() < 1 ? "," : fieldSeparator) + "')";
				ds.getConnection().createStatement().execute(query);
				// file.delete();
			}
			
			// Get inserted rows
			{
				final Statement stmt = ds.getConnection().createStatement();
				ResultSet r = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM dataset");
				r.next();
				rowcount = r.getInt("rowcount");
				r.close();
				stmt.close();
			}

			// Return result
			return new Response(rowcount > 0, rowcount + " rows have been inserted.");
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return new Response(false, e.getMessage());
		}
	}
	
	@AllArgsConstructor
	@Data
	protected static final class Response
	{
		protected Boolean success;
		protected String message;
	}
}