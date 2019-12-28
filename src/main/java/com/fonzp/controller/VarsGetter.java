package com.fonzp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fonzp.model.entity.OpCategory;
import com.fonzp.model.entity.OpVar;
import com.fonzp.repository.OpLogRepository;
import com.fonzp.repository.OpVarRepository;

import lombok.Data;

/**
 * This class acts as a REST Api to provide different types of services for the
 * front-end user.
 */
@RestController
public final class VarsGetter
{
	@Autowired
	private OpVarRepository opVarRepository;

	@Autowired
	private OpLogRepository opCategoryRepository;
	
	@PostMapping("/api/get_vars")
	public final Response getOpVars()
	{
		return new Response(opCategoryRepository.findOpCategories(), opVarRepository.findAllForChart());
	}
	
	@Data
	protected static final class Response
	{
		protected final List<OpCategory> categories;
		protected final List<OpVar> variables;
	}
}