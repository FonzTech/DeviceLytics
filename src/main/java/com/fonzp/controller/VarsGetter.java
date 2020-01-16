package com.fonzp.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class acts as a REST Api to provide different types of services for the
 * front-end user.
 */
@RestController
public final class VarsGetter
{
	@PostMapping("/api/get_vars")
	public final Object getOpVars()
	{
		return "mock";
	}
}