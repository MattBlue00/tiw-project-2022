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
import it.polimi.tiw.DAO.ImageDAO;
import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.Image;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class AlbumPage
 */
@WebServlet("/AlbumPage")
public class AlbumPage extends HttpServlet {
		
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	private static final int MAX_NUM_IMAGES = 5;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
	
    public AlbumPage() {
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
		
		String albumTitle, albumOwner;
		if(session.getAttribute("albumTitle") == null) {
			albumTitle = request.getParameter("titoloAlbum");
			session.setAttribute("albumTitle", albumTitle);
		}
		else
			albumTitle = (String) session.getAttribute("albumTitle");
		if(session.getAttribute("albumOwner") == null) {
			albumOwner = request.getParameter("proprietarioAlbum");
			session.setAttribute("albumOwner", albumOwner);
		}
		else
			albumOwner = (String) session.getAttribute("albumOwner");
		
		ImageDAO imageDAO = new ImageDAO(connection);
		List<Image> albumImages = new ArrayList<>();
		try {
			albumImages.addAll(imageDAO.getAlbumImages(albumOwner, albumTitle));
		} 
		catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile caricare le immagini.");
			return;
		}
		
		Integer firstImageIndex = (Integer) session.getAttribute("firstImageIndex");
		if(firstImageIndex == null)
			firstImageIndex = 0;
		else {
			if(request.getParameter("buttonValue") != null) {
				if(((String) request.getParameter("buttonValue")).equalsIgnoreCase("previous"))
					firstImageIndex = firstImageIndex - MAX_NUM_IMAGES;
				else 
					firstImageIndex = firstImageIndex + MAX_NUM_IMAGES;
			}
		}
		session.setAttribute("firstImageIndex", firstImageIndex);
		
		int listLength = albumImages.size();
		int lastImageIndex = firstImageIndex + MAX_NUM_IMAGES;
		if(listLength > MAX_NUM_IMAGES) {
			if(lastImageIndex > listLength)
				lastImageIndex = listLength;
			albumImages = albumImages.subList(firstImageIndex, lastImageIndex);
		}
		
		if(firstImageIndex >= MAX_NUM_IMAGES)
			session.setAttribute("previousButtonNeeded", Boolean.valueOf(true));
		else
			session.setAttribute("previousButtonNeeded", Boolean.valueOf(false));
		if(listLength > lastImageIndex)
			session.setAttribute("nextButtonNeeded", Boolean.valueOf(true));
		else
			session.setAttribute("nextButtonNeeded", Boolean.valueOf(false));
		
		String path = "/WEB-INF/templates/AlbumPage.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("albumImages", albumImages);
		templateEngine.process(path, ctx, response.getWriter());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	
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
