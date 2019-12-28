package com.fonzp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public final class Index
{
	@GetMapping({"/", "/index"})
	public final ModelAndView index()
	{
		return new ModelAndView("index");
	}
}