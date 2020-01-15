package com.fonzp.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fonzp.model.LogRequest;

/**
 * This class acts as a REST Api to provide different types of services for the
 * front-end user.
 */
@RestController
public final class LogGetter
{
    @PersistenceContext
    private EntityManager em;

	@SuppressWarnings("unchecked")    
	@PostMapping("/api/get_logs")
	public final Object getOpLogs(@RequestBody final LogRequest logRequest)
	{
		if (logRequest.isBoxPlot())
		{
			final HashMap<String, ArrayList<Object>> map = new HashMap<>();
			
			// Get program and positions
			List<Object[]> params;
			{
				final Query q = em.createNativeQuery("SELECT program, position FROM oplog GROUP BY program, position");
				params = (List<Object[]>) q.getResultList();
			}
			
			for (final String var : logRequest.getVars())
			{
				for (final Object[] param : params)
				{
					final Query q = em.createNativeQuery("SELECT " + var.replaceAll("/\\s/", "") + " FROM oplog WHERE program = :program AND position = :position ORDER BY id ASC");
					q.setParameter("program", param[0]);
					q.setParameter("position", param[1]);
					
					final ArrayList<Object> list = map.get(var);
					if (list != null)
					{
						list.add(q.getResultList());
					}
					else
					{
						map.put(var, new ArrayList<Object>(Arrays.asList(q.getResultList())));
					}
				}
			}
			
			return map;
		}
		else
		{
			final ArrayList<HashMap<String, Object>> rows = new ArrayList<>();
			
			final Query q = em.createNativeQuery("SELECT id, " + String.join(",", logRequest.getVars()).replaceAll("/\\s/", "") + " FROM oplog WHERE program = :program AND position = :position ORDER BY id ASC");
			q.setParameter("position", logRequest.getPosition());
			q.setParameter("program", logRequest.getProgram());
			
			for (final Object result : q.getResultList())
			{
				final HashMap<String, Object> row = new HashMap<>();
				
				final Object[] array = (Object[]) result;
				for (int i = 0; i < array.length; ++i)
				{
					row.put(i == 0 ? "id" : logRequest.getVars()[i - 1], array[i]);
				}
				
				rows.add(row);
			}
			
			return rows;
		}
	}
}