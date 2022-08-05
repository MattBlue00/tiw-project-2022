package it.polimi.tiw.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

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
		
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		
		String email = request.getParameter("email");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String passwordRepeated = request.getParameter("passwordRepeated");
        if(username.contains(" ") || email.contains(" ") || password.contains(" ") || 
        		passwordRepeated.contains(" ")) {
        	ctx.setVariable("errorMsgRegistration", "Non è possibile inserire spazi vuoti.");
        }
        else if(!CheckRegister.isEmailValid(email)) {
        	ctx.setVariable("errorMsgRegistration", "La mail inserita non è valida.");	
        }
    	else if (email == null || username == null || password == null || passwordRepeated == null || email.isEmpty() || 
		            username.isEmpty() || password.isEmpty() || passwordRepeated.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Credenziali mancanti o inesistenti.");
			return;
		}
				
		UserDAO userDao = new UserDAO(connection);
		User user = new User();
		user.setUsername(username);
		user.setPassword(password);
		user.setEmail(email);
		boolean checkCredentialsDone = false;
		try {
			checkCredentialsDone = userDao.checkCredentialsRegistration(email, username);
			if(checkCredentialsDone && password.equals(passwordRepeated)) {
				userDao.registerUser(user);
			}
			else if(!checkCredentialsDone && password.equals(passwordRepeated)) {
				ctx.setVariable("errorMsgRegistration", "Username o email già in uso.");
			}
			else {
				ctx.setVariable("errorMsgRegistration", "Le password inserite non sono uguali.");
				ctx.setVariable("emailInserita", email != null ? email : "");
				ctx.setVariable("usernameInserito", (username != null || !username.isEmpty()) ? username : "");
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile controllare le credenziali.\n"
					+ e.getMessage());
			return;
		}
			
		String path = "/login.html";
		templateEngine.process(path, ctx, response.getWriter());
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static boolean isEmailValid(String email) {			
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";        
		Pattern pat = Pattern.compile(emailRegex);
		if (email == null)
			return false;
		return pat.matcher(email).matches();
	}
	
}
