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

import it.polimi.tiw.DAO.CommentDAO;
import it.polimi.tiw.DAO.ImageDAO;
import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.Comment;
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
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		
		int pageNumber = -1;
		
		// entriamo in questo if ogni volta che selezioniamo (clicchiamo) un album
		if(request.getAttribute("titoloAlbum") != null && request.getAttribute("proprietarioAlbum") != null) {
			String albumTitle, albumOwner;
			Album album = new Album();

			albumTitle = (String) request.getAttribute("titoloAlbum");
			albumOwner = (String) request.getAttribute("proprietarioAlbum");
			album.setOwner(albumOwner);
			album.setTitle(albumTitle);
			session.setAttribute("album", album);
			ctx.setVariable("pageNumber", 1);
			pageNumber = 1;
		}
		
		// prendiamo il valore di pageNumber
		else {
			if(request.getParameter("pageNumber") != null) {
				pageNumber = Integer.valueOf(request.getParameter("pageNumber"));
			}
			else {
				pageNumber = (int) request.getAttribute("pageNumber");
			}
		}
		
		Album album = (Album) session.getAttribute("album");
		
		ctx.setVariable("albumTitle", album.getTitle());
		ctx.setVariable("albumOwner", album.getOwner());
		
		// carichiamo le immagini dell'album
		ImageDAO imageDAO = new ImageDAO(connection);
		List<Image> albumImages = new ArrayList<>();
		try {
			albumImages.addAll(imageDAO.getAlbumImages(album.getOwner(), album.getTitle()));
		} 
		catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile caricare le immagini.");
			return;
		}
		
		// se l'album è vuoto non serve pageNumber
		if(albumImages.isEmpty()) {
			ctx.removeVariable("pageNumber");
		}
		
		if(((User) session.getAttribute("utente")).getUsername().equals(album.getOwner())) {
			ctx.setVariable("addImageAllowed", Boolean.valueOf(true));
		}
		
		// gestione dei button
		if(request.getParameter("buttonValue") != null) {
			if(((String) request.getParameter("buttonValue")).equalsIgnoreCase("previous")) {
				pageNumber--;
				ctx.setVariable("pageNumber", pageNumber);
				}
			else {
				pageNumber++;
				ctx.setVariable("pageNumber", pageNumber);
			}
				
		}
		
		// mostriamo 5 immagini alla volta
		int listLength = albumImages.size();
		int firstImageIndex = (pageNumber - 1)* MAX_NUM_IMAGES;
		int lastImageIndex = pageNumber * MAX_NUM_IMAGES;
		if(listLength > MAX_NUM_IMAGES) {
			if(lastImageIndex > listLength)
				lastImageIndex = listLength;
			albumImages = albumImages.subList(firstImageIndex, lastImageIndex);
		}
		
		if(firstImageIndex >= MAX_NUM_IMAGES) {
			ctx.setVariable("previousButtonNeeded", Boolean.valueOf(true));
			}
		else {
			ctx.setVariable("previousButtonNeeded", Boolean.valueOf(false));
			}
		if(listLength > lastImageIndex) {
			ctx.setVariable("nextButtonNeeded", Boolean.valueOf(true));
			}
		else {
			ctx.setVariable("nextButtonNeeded", Boolean.valueOf(false));
			}
		
		Image image = new Image();
		
		// se un'immagine viene selezionata aggiorniamo la pagina con i dettagli dell'immagine 
		// e con gli eventuali commenti
		
		Integer imageId = null;
		if(request.getAttribute("imageID") != null) {
			imageId = (int) request.getAttribute("imageID");
			ctx.setVariable("successMsg", "Il commento è stato aggiunto correttamente.");
		}
		
		if(request.getParameter("idImmagine") != null) {
			imageId = Integer.valueOf(request.getParameter("idImmagine"));
		}
		
		if(imageId != null ) {
			ctx.setVariable("imageClicked", Boolean.valueOf(true));
			try {
				image = imageDAO.getImageFromId(imageId);
				
			} catch (NumberFormatException | SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile caricare l'immagine.");
				e.printStackTrace();
				return;
			}
			
			// dettagli dell'immagine
			ctx.setVariable("image", image);
			ctx.setVariable("imageId", image.getID());
			ctx.setVariable("imageTitle", image.getImageTitle());
			ctx.setVariable("albumTitle", image.getAlbumTitle());
			ctx.setVariable("albumOwner", image.getOwner());
			ctx.setVariable("date", image.getDate());
			ctx.setVariable("description", image.getDescription());
			ctx.setVariable("path", image.getPath());
			User user = (User) session.getAttribute("utente");
			ctx.setVariable("user", user.getUsername());
			ctx.setVariable("pageNumber", pageNumber);
			
			// estraiamo i commenti dal database
			List<Comment> comments = new ArrayList<Comment>();
			CommentDAO commentDAO = new CommentDAO(connection);
			
			try {
				comments = commentDAO.getImageComments(image.getID());
				ctx.setVariable("commenti", comments);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile caricare i commenti.");
				return;
			}
			
		}
		
		String path = "/WEB-INF/templates/AlbumPage";
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
