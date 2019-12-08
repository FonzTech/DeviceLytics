package devicelytics.task;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public final class OpValuesGetter extends DatabaseTask
{
	private final ArrayList<HashMap<String, String>> values;
	
	public OpValuesGetter()
	{
		super();
		values = new ArrayList<>();
	}
	
	@Override
	public final void doInBackground()
	{
		// Check for connection
		if (connection == null)
		{
			return;
		}

		// Get values for variables
		try
		{
			final String program = parameterMap.get("program")[0];
			final String position = parameterMap.get("position")[0];
			final String vars = parameterMap.get("vars")[0].replaceAll("(?!(\\w|\\d|\\,))", "");
			
			final PreparedStatement stmt = connection.prepareStatement("SELECT id, " + vars + " FROM oplog WHERE program = ? AND position = ? ORDER BY id ASC");
			stmt.setString(1, program);
			stmt.setString(2, position);
			
			final ResultSet rs = stmt.executeQuery();
			final ResultSetMetaData rsmd = rs.getMetaData();
			
			while (rs.next())
			{
				final HashMap<String, String> var = new HashMap<>();
				
				for (int i = 0; i < rsmd.getColumnCount(); ++i)
				{
					final String name = rsmd.getColumnName(i + 1);
					final String value = rs.getString(i + 1);
					var.put(name, value);
				}
				
				values.add(var);
			}
			stmt.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public final Object getResult()
	{
		return values;
	}
}