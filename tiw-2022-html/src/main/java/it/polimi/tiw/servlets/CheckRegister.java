package it.polimi.tiw.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.DAO.UserDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/CheckRegister")
public class CheckRegister extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	public CheckRegister() {
		super();
	}
	
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		connection = ConnectionHandler.getConnection(servletContext);
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String email = request.getParameter("email");
        String username = request.getParameter("username");
		String password = request.getParameter("password");
        String passwordRepeated = request.getParameter("passwordRepeated");
		if (email == null || username == null || password == null || passwordRepeated == null || email.isEmpty() || 
            username.isEmpty() || password.isEmpty() || passwordRepeated.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Credenziali mancanti o inesistenti");
			return;
		}
		
		UserDAO userDao = new UserDAO(connection);
		boolean checkCredentialsDone = false;
		try {
			checkCredentialsDone = userDao.checkCredentialsRegistration(email, username);
			if(checkCredentialsDone && password.equals(passwordRepeated)) {
				userDao.registerUser(username, email, password);
			}
			else {
				if(!checkCredentialsDone && password.equals(passwordRepeated)) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Registrazione fallita.\nUsername o email già in uso.");
				}
				else {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Registrazione fallita.\nLe password inserite non sono uguali.");
				}
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile controllare le credenziali "
					+ e.getMessage());
			return;
		}
		
		String path;
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("emailInserita", (email != null || !email.isEmpty()) ? email : "");
		ctx.setVariable("usernameInserito", (username != null || !username.isEmpty()) ? username : "");
		ctx.setVariable("errorMsg", "Email o password errata");
		path = "/index.html";
		templateEngine.process(path, ctx, response.getWriter());
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
