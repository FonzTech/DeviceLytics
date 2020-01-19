package com.fonzp.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * This class acts as a REST Api to provide different types of services for the
 * front-end user.
 */
@RestController
public final class LogGetter
{
	@Autowired
	private DataSource ds;
	
	@PostMapping("/api/get_logs")
	public final Object getLogs(@RequestParam final String[] variables)
	{
		final Response response = new Response(false, "Unknown error.", null);
		
		Connection connection = null;
		
		try
		{
			final ArrayList<HashMap<String, String>> values = new ArrayList<>();
			
			connection = ds.getConnection();
			
			final ResultSet rs = connection.createStatement().executeQuery("SELECT " + String.join(",", variables) + " FROM dataset");
			while (rs.next())
			{
				final HashMap<String, String> map = new HashMap<>();
				for (int i = 0; i < variables.length; ++i)
				{
					map.put(rs.getMetaData().getColumnName(i + 1), rs.getString(i + 1));
				}
				values.add(map);
			}
			
			response.setSuccess(true);
			response.setMessage(null);
			response.setData(values);
		}
		catch (final Exception e)
		{
			e.printStackTrace();

			response.setSuccess(false);
			response.setMessage(e.getMessage());
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
		
		return response;
	}
	
	@AllArgsConstructor
	@Data
	protected static final class Response
	{
		protected Boolean success;
		protected String message;
		protected Object data;
	}
}