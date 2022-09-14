package it.polimi.tiw.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.DAO.UserDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.Constants;

/**
 * Servlet implementation class CheckLogin.
 * La servlet controlla e gestisce il login
 */
@WebServlet("/CheckLogin")
@MultipartConfig
public class CheckLogin extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CheckLogin() {
		super();
	}

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		connection = ConnectionHandler.getConnection(servletContext);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

  	  	request.setCharacterEncoding("UTF-8");
		String username = request.getParameter("username");
		String password = request.getParameter("passwordLogin");
		
		// controlliamo che i parametri non siano nulli o vuoti
		if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Credenziali mancanti o inesistenti.");
		}
		// controlliamo che username e password non superino il numero massimo di caratteri consentiti
		else if(username.length() > Constants.USERNAME_MAX_DIM || password.length() > Constants.PASSWORD_MAX_DIM) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Valori inseriti troppo lunghi.");
		}
		else {
			UserDAO userDao = new UserDAO(connection);
			User user = null;
			try {
				// controlliamo se password e username sono corretti
				user = userDao.checkCredentials(username, password);
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Impossibile controllare le credenziali.");
				return;
			}
			
			/*
			 * Se le credenziali sono corrette l'utente è reindirizzato alla home page,
			 * altrimenti viene stampato un messaggio di errore e ritorna alla pagina di login
			 */
	
			if (user == null) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().println("Credenziali non corrette.");
				
			} else {
				// settiamo in sessione l'utente che ha appena fatto il login
				request.getSession().setAttribute("utente", user);
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().println(username);
			}
			
		}
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
