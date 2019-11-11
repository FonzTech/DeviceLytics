package devicelytics.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import devicelytics.task.OpValuesGetter;
import devicelytics.task.OpVarsGetter;
import devicelytics.task.Task;

/**
 * This class acts as a REST Api to provide different types of services for the
 * front-end user.
 */
@WebServlet("/Api")
@MultipartConfig
public final class Api extends HttpServlet
{
	@Override
	public final void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		// Set response content type
		response.setContentType("application/json");

		// Get requested service
		final String service = request.getParameter("s");

		// Identify task
		final Task task;

		if (service != null)
		{
			switch (service)
			{
			case "vars":
				task = new OpVarsGetter();
				break;

			case "values":
				task = new OpValuesGetter();
				break;
	
			default:
				task = null;
			}
		}
		else
		{
			task = null;
		}
		
		// Execute task if possible
		if (task != null)
		{
			task.setParameterMap(request.getParameterMap());
			task.start();
			
			try
			{
				// Wait for task to complete
				task.join();
				
				// Write response
				final PrintWriter writer = response.getWriter();
				writer.write(new Gson().toJson(task.getResult()));
				writer.close();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
		else
		{
			response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
		}
	}

	@Override
	protected final void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}
}