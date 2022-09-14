package it.polimi.tiw.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

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

@WebServlet("/CheckRegister")
@MultipartConfig
/**
 * La servlet gestisce la registrazione di un utente
 *
 */
public class CheckRegister extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public CheckRegister() {
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
		
		String email = request.getParameter("email");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String passwordRepeated = request.getParameter("passwordRepeated");
        
        // controlliamo che i parametri della request non siano nulli o vuoti
        if (email == null || username == null || password == null || passwordRepeated == null || email.isEmpty() || 
	            username.isEmpty() || password.isEmpty() || passwordRepeated.isEmpty()) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	response.getWriter().println("Credenziali mancanti o inesistenti.");
        }
    
        // controlliamo che i parametri siano della dimensione giusta (ovvero che non superino 
        // il numero massimo di caratteri consentiti)
        else if (email.length() > Constants.EMAIL_MAX_DIM || username.length() > Constants.USERNAME_MAX_DIM ||
			password.length() > Constants.PASSWORD_MAX_DIM || passwordRepeated.length() > Constants.PASSWORD_MAX_DIM){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Valori inseriti troppo lunghi.");
        }
        
        // controlliamo che non ci siano spazi 
        else if(username.contains(" ") || email.contains(" ") || password.contains(" ") || 
        		passwordRepeated.contains(" ")) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Non è possibile inserire spazi vuoti.");
        }
        
        // controlliamo che l'email sia valida
        else if(!CheckRegister.isEmailValid(email)) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("L'email inserita non è valida.");
        }
        
    	else {	
    		
    		/*
    		 * Controlliamo se esistono altri utenti con lo stesso username e/o email
    		 */
    		
			UserDAO userDao = new UserDAO(connection);
			User user = new User();
			user.setUsername(username);
			user.setPassword(password);
			user.setEmail(email);
			boolean checkCredentialsDone = false;
			
			try {
				// Controlliamo se esistono altri utenti con lo stesso username e/o email
				checkCredentialsDone = userDao.checkCredentialsRegistration(email, username);
				
				// username e email sono unici e non ancora presenti nel database
				// password e passwordRepeated sono uguali
				if(checkCredentialsDone && password.equals(passwordRepeated)) {
					// registriamo il nuovo utente 
					userDao.registerUser(user);
					response.setStatus(HttpServletResponse.SC_OK);
					response.setCharacterEncoding("UTF-8");
					response.getWriter().println("La registrazione è andata a buon fine!");			
					}
				
				// username e email non sono unici e sono presenti nel database
				// password e passwordRepeated sono uguali
				else if(!checkCredentialsDone && password.equals(passwordRepeated)) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.setCharacterEncoding("UTF-8");
					response.getWriter().println("Username o email già in uso.");
				}
				
				// password e passwordRepeated non sono uguali
				else {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println("Le password inserite non sono uguali.");
				}
				
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Impossibile controllare le credenziali.");
				return;
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
	
	/**
	 * Il metodo controlla la validità dell'email
	 * @param email
	 * @return
	 */
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
