package com.fonzp.controller;

import java.util.ArrayList;
import java.util.HashMap;

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
    
	@PostMapping("/api/get_logs")
	public final Object getOpLogs(@RequestBody final LogRequest logRequest)
	{		
		final ArrayList<HashMap<String, Object>> rows = new ArrayList<>();

		final Query q = em.createNativeQuery("SELECT id, " + String.join(",", logRequest.getVars()) + " FROM oplog WHERE program = :program AND position = :position");
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