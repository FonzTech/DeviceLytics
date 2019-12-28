package com.fonzp.service;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

import org.springframework.orm.jpa.EntityManagerFactoryInfo;

public abstract class DatabaseTask extends Task
{
	protected static Connection connection;
	
	public DatabaseTask()
	{
		super();
	}
	
	/**
	 * Get entity manager in implementation.
	 *
	 * @return entity manager instance.
	 */
	protected abstract EntityManager getEntityManager();
	
	@Override
	protected final void onStart()
	{
		try
		{
			connection = getDatabaseConnection();
		}
		catch (final SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onFinish()
	{
		if (connection != null)
		{
			try
			{
				connection.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			connection = null;
		}
	}
	
	/**
	 * Get database connection using the credentials defined in the
	 * context configuration file.
	 *
	 * @return Connection to database on success, null on failure.
	 *
	 * @throws NamingException creation of context or data source lookup fails.
	 * @throws SQLException failure while getting database connection.
	 */
	protected final Connection getDatabaseConnection() throws SQLException
	{
		if (connection != null)
		{
			return connection;
		}
		
		final EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) getEntityManager().getEntityManagerFactory();
		return info.getDataSource().getConnection();
	}
}