package com.fonzp.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "opvar")
@Data
@EqualsAndHashCode(of={"id"})
public final class OpVar
{
	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "column_name")
	private String columnName;

	@Column(name = "column_label")
	private String columnLabel;

	@Column(name = "csv_label")
	private String csvLabel;

	@Column(name = "show_for_chart")
	private boolean showForChart;
}