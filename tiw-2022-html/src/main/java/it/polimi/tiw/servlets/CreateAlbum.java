package it.polimi.tiw.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.DAO.AlbumDAO;
import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.Constants;

/**
 * Servlet che si occupa della creazione di un album.
 */

@WebServlet("/CreateAlbum")
public class CreateAlbum extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	public CreateAlbum() {
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
			throws ServletException, IOException{
		
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("utente");
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		
		String title = request.getParameter("title");
		
		// controllo di integrità del parametro "title"
		if (title == null || title.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Valori mancanti o inesistenti.");
			return;
		}
		if(title.length() > Constants.ALBUM_TITLE_MAX_DIM) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Valore inserito troppo lungo.");
			return;
		}
		
		AlbumDAO albumDao = new AlbumDAO(connection);
		Album album = new Album();
		album.setOwner(user.getUsername());
		album.setTitle(title);
		String path;
		// se il titolo dell'album è unico tra quelli dell'utente, l'album viene creato
		try {
			if(albumDao.checkAlbumTitle(album)) {
				albumDao.createAlbum(album);
				session.setAttribute("album", album);
				path = "/WEB-INF/templates/AddImage.html";
				templateEngine.process(path, ctx, response.getWriter());
			}
			else {
				path = "/HomePage";
				request.setAttribute("titleWarning", Boolean.valueOf(true));
				RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
				dispatcher.forward(request, response);
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile creare l'album.");
			return;
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
