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
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.DAO.AlbumDAO;
import it.polimi.tiw.beans.Album;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet che si occupa di redirigere l'utente verso la pagina AlbumPage. Questa servlet si
 * è resa necessaria a seguito della decisione di salvare in sessione le informazioni relative
 * all'album cliccato e alla conseguenti neccessità di filtraggio.
 */

@WebServlet("/GoToAlbumPage")
public class GoToAlbumPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
    public GoToAlbumPage() {
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
		
		HttpSession session = request.getSession();
		String path = "/AlbumPage";
		String albumTitle, albumOwner;
		Album album = new Album();
		
		albumTitle = request.getParameter("titoloAlbum");
		albumOwner = request.getParameter("proprietarioAlbum");
		
		// controllo di integrità sui parametri della richiesta
		if(albumTitle == null || albumOwner ==  null || albumTitle.isEmpty() || albumOwner.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri errati.");
			return;
		}
		
		// settaggio degli attributi necessari alla prossima servlet chiamata (AlbumPage)
		request.setAttribute("titoloAlbum", albumTitle);
		request.setAttribute("proprietarioAlbum", albumOwner);
		album.setOwner(albumOwner);
		album.setTitle(albumTitle);
		AlbumDAO albumDAO = new AlbumDAO(connection);
		try {
			// controllo che il titolo fornito esista nel database in caso di manomissione
			// della query string
			if(albumDAO.checkAlbumTitle(album)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Album non esistente.");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile cercare l'album.");
			return;
		}
		
		session.setAttribute("album", album);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
		dispatcher.forward(request, response);
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
