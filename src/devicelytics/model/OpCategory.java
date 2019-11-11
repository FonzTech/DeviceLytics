package devicelytics.model;

public final class OpCategory
{
	private String program;
	private String position;
	
	public OpCategory()
	{
	}
	
	public OpCategory(final String program, final String position)
	{
		this.program = program;
		this.position = position;
	}

	public final String getProgram()
	{
		return program;
	}

	public final void setProgram(final String program)
	{
		this.program = program;
	}

	public final String getPosition()
	{
		return position;
	}

	public final void setPosition(final String position)
	{
		this.position = position;
	}
}