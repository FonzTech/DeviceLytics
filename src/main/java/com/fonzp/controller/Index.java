package com.fonzp.controller;

import java.io.File;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public final class Index
{
	@Autowired
	@Qualifier("configFile")
	private File configFile;
	
	@GetMapping({"/", "/index"})
	public final ModelAndView index()
	{
		final ModelAndView mav = new ModelAndView("index");
		
		try
		{
			if (configFile.exists())
			{
				final HashMap<String, String> params = new ObjectMapper().readValue(configFile, new TypeReference<HashMap<String, String>>() {});
				mav.getModel().put("classIndex", params.get("classIndex"));
				mav.getModel().put("target", params.get("target"));
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		return mav;
	}
}