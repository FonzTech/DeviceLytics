package devicelytics.task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import lombok.AllArgsConstructor;

import devicelytics.model.OpCategory;
import devicelytics.model.OpVar;

public final class OpVarsGetter extends DatabaseTask
{
	private final ArrayList<OpCategory> categories;
	private final ArrayList<OpVar> variables;
	
	public OpVarsGetter()
	{
		super();
		categories = new ArrayList<>();
		variables = new ArrayList<>();
	}
	
	@Override
	public final void doInBackground()
	{
		// Check for connection
		if (connection == null)
		{
			return;
		}

		// Get available programs
		try
		{
			final Statement stmt = connection.createStatement();
			final ResultSet rs = stmt.executeQuery("SELECT program, position FROM oplog GROUP BY program, position");
			while (rs.next())
			{
				final OpCategory category = new OpCategory();
				category.setProgram(rs.getString(1));
				category.setPosition(rs.getString(2));
				categories.add(category);
			}
			stmt.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		// Get available variables
		try
		{
			final Statement stmt = connection.createStatement();
			final ResultSet rs = stmt.executeQuery("SELECT id, column_name, column_label FROM opvar");
			while (rs.next())
			{
				final OpVar var = new OpVar();
				var.setId(rs.getInt(1));
				var.setColumnName(rs.getString(2));
				var.setColumnLabel(rs.getString(3));
				variables.add(var);
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
		return new Result(categories, variables);
	}
	
	// Getter result
	@AllArgsConstructor
	public static final class Result
	{
		protected final ArrayList<OpCategory> categories;
		protected final ArrayList<OpVar> variables;
	}
}