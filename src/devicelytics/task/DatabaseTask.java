package devicelytics.task;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public abstract class DatabaseTask extends Task
{
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
			connection = getDatabaseConnection();
		}
		catch (NamingException | SQLException e)
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
	protected final Connection getDatabaseConnection() throws NamingException, SQLException
	{
		final Context context = (Context) new InitialContext().lookup("java:comp/env");
		final DataSource ds = (DataSource) context.lookup("jdbc/DLDataSource");
		return ds.getConnection();
	}
}