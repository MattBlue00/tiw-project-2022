package it.polimi.tiw.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

/**
 * Servlet implementation class HomePageHandler
 */

@WebServlet("/HomePage")
public class HomePage extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
	
    public HomePage() {
        super();
        // TODO Auto-generated constructor stub
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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("utente");
		AlbumDAO albumDAO = new AlbumDAO(connection);
		List<Album> userAlbums = new ArrayList<>();
		List<Album> otherAlbums = new ArrayList<>();
		try {
			userAlbums.addAll(albumDAO.getUserAlbums(user.getUsername()));
			otherAlbums.addAll(albumDAO.getOtherAlbums(user.getUsername()));
		} 
		catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile caricare gli album.");
			return;
		}
		
		String path = "/WEB-INF/templates/HomePage.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("userAlbums", userAlbums);
		ctx.setVariable("otherAlbums", otherAlbums);
		if(session.getAttribute("titleWarning") != null) {
			ctx.setVariable("errorMsg", "Il nome scelto è già presente.");
			session.removeAttribute("titleWarning");
		}
		if(session.getAttribute("firstImageIndex") != null)
			session.removeAttribute("firstImageIndex");
		if(session.getAttribute("albumTitle") != null)
			session.removeAttribute("albumTitle");
		if(session.getAttribute("albumOwner") != null)
			session.removeAttribute("albumOwner");
		templateEngine.process(path, ctx, response.getWriter());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
