package com.fonzp.model;

import lombok.Data;

@Data
public final class LogRequest
{
	private String program;
	private String position;
	private String[] vars;
	private boolean boxPlot;
}