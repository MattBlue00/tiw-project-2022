package it.polimi.tiw.servlets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet che si occupa dell'ottenimento di un'immagine e di tutti i suoi dettagli.
 */

@WebServlet("/GetImage")
public class GetImage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
    
    public GetImage() {
        super();
    }

    public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		String imagePath = request.getParameter("imagePath");
		// controllo di integrità sul parametro "imagePath"
		if(imagePath == null || imagePath.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri errati.");
			return;
		}
		
		File file = new File(imagePath);
		// se esiste, copia il file nella risposta, in modo tale che sia visualizzabile nell'HTML
		try {
			Files.copy(file.toPath(), response.getOutputStream());
		} catch(IOException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Il path fornito non esiste.");
			return;
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doGet(request, response);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
