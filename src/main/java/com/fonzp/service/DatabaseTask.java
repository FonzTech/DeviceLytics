package com.fonzp.service;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class DatabaseTask extends Task
{
	@Autowired
	protected DataSource ds;
	
	protected Connection connection;
	
	public DatabaseTask()
	{
		super();
	}
	
	@Override
	protected final void onStart()
	{
		try
		{
			connection = ds.getConnection();
		}
		catch (final SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onFinish()
	{
		/*
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
		*/
	}
}