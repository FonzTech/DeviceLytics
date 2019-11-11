package devicelytics.model;

public final class OpVar
{
	private int id;
	private String columnName;
	private String columnLabel;
	
	public OpVar()
	{
	}
	
	public OpVar(final int id, final String columnName, final String columnLabel)
	{
		this.id = id;
		this.columnName = columnName;
		this.columnLabel = columnLabel;
	}

	public final int getId()
	{
		return id;
	}

	public final void setId(final int id)
	{
		this.id = id;
	}

	public final String getColumnName()
	{
		return columnName;
	}

	public final void setColumnName(final String columnName)
	{
		this.columnName = columnName;
	}

	public final String getColumnLabel()
	{
		return columnLabel;
	}

	public final void setColumnLabel(final String columnLabel)
	{
		this.columnLabel = columnLabel;
	}
}