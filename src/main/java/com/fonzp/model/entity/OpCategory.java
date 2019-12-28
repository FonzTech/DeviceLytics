package com.fonzp.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "oplog")
@Data
@EqualsAndHashCode(of={"id"})
public final class OpCategory
{
	@Id
	@Column(name = "id")
	private int id;
	
	@Column
	private String program;
	
	@Column
	private String position;
}