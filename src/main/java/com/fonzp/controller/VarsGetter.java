package com.fonzp.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * This class acts as a REST Api to provide different types of services for the
 * front-end user.
 */
@RestController
public final class VarsGetter
{
	@Autowired
	private DataSource ds;
	
	@PostMapping("/api/get_vars")
	public final Object getVars()
	{
		final Response response = new Response(false, "Unknown error.", null);
		
		ArrayList<String> variables = null;
		Connection connection = null;
		
		// Get table metadata
		try
		{
			connection = ds.getConnection();
			
			final Statement stmt = connection.createStatement();
			ResultSet r = stmt.executeQuery("SELECT * FROM dataset LIMIT 1");
			r.next();
			
			// Get columns metadata
			final ResultSetMetaData md = r.getMetaData();
			for (int i = 1; i <= md.getColumnCount(); ++i)
			{
				final String name = md.getColumnName(i);
				if (variables == null)
				{
					variables = new ArrayList<>();
				}
				variables.add(name);
			}
			
			r.close();
			stmt.close();

			response.setSuccess(variables != null);
			response.setMessage(variables == null ? "No variables are present in the current session dataset." : null);
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

		response.setVariables(variables);
		return response;
	}
	
	@AllArgsConstructor
	@Data
	protected static final class Response
	{
		protected Boolean success;
		protected String message;
		protected ArrayList<String> variables;
	}
}