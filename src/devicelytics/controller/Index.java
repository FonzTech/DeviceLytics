package devicelytics.controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/Index")
@MultipartConfig
public final class Index extends HttpServlet
{
	@Override
	public final void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		// Set response content type
		response.setContentType("text/html");
		
		// Dispatch JSP
		final RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp");
		dispatcher.forward(request, response);
	}
}