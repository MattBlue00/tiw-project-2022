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
 * Servlet che si occupa di caricare correttamente la pagina AlbumPage.
 */

@WebServlet("/AlbumPage")
public class AlbumPage extends HttpServlet {
		
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	private static final int MAX_NUM_IMAGES = 5;
    
    public AlbumPage() {
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
		HttpSession session = request.getSession();
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		
		int pageNumber = -1;
		
		// entriamo in questo if ogni volta che selezioniamo (clicchiamo su) un album
		if(request.getAttribute("titoloAlbum") != null && request.getAttribute("proprietarioAlbum") != null) {
			String albumTitle, albumOwner;
			Album album = new Album();
			albumTitle = (String) request.getAttribute("titoloAlbum");
			albumOwner = (String) request.getAttribute("proprietarioAlbum");
			album.setOwner(albumOwner);
			album.setTitle(albumTitle);
			ctx.setVariable("pageNumber", 1);
			pageNumber = 1;
		}
		// altrimenti prendiamo il valore di pageNumber
		else {
			if(request.getParameter("pageNumber") != null) {
				// controllo di integrità sul parametro "pageNumber"
				try {
					pageNumber = Integer.valueOf(request.getParameter("pageNumber"));
				} catch(NumberFormatException e){
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Impossibile caricare le immagini.");
					return;
				}
			}
			else if(request.getAttribute("pageNumber") != null){
				pageNumber = (int) request.getAttribute("pageNumber");
			}
			else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri scorretti.");
				return;
			}
		}
		
		// se il valore di pageNumber fornito non è valido, la servlet risponde con un errore
		if(pageNumber < 1) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Impossibile caricare le immagini.");
			return;
		}
		
		Album album = (Album) session.getAttribute("album");
		
		ctx.setVariable("albumTitle", album.getTitle());
		ctx.setVariable("albumOwner", album.getOwner());
		
		// se l'album visualizzato è di proprietà dell'utente, può aggiungervi immagini
		if(((User) session.getAttribute("utente")).getUsername().equals(album.getOwner())) {
			ctx.setVariable("addImageAllowed", Boolean.valueOf(true));
		}
		
		// aggiornamento della variabile pageNumber in base ai bottoni eventualmente premuti
		if(request.getParameter("buttonValue") != null) {
			if(((String) request.getParameter("buttonValue")).equalsIgnoreCase("previous")) {
				pageNumber--;
				ctx.setVariable("pageNumber", pageNumber);
				}
			else if(((String) request.getParameter("buttonValue")).equalsIgnoreCase("next")){
				pageNumber++;
				ctx.setVariable("pageNumber", pageNumber);
			}
			else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Impossibile caricare le immagini.");
				return;
			}	
		}
		
		// carichiamo le immagini dell'album
		ImageDAO imageDAO = new ImageDAO(connection);
		List<Image> albumImages = new ArrayList<>();
		try {
			albumImages.addAll(imageDAO.getAlbumImages(album.getOwner(), album.getTitle(), ((pageNumber-1)*MAX_NUM_IMAGES)));
		} 
		catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile caricare le immagini.");
			return;
		}
		
		// se l'album è vuoto, non serve pageNumber
		if(albumImages.isEmpty()) {
			ctx.removeVariable("pageNumber");
		}
		
		if(pageNumber > 1) {
			ctx.setVariable("previousButtonNeeded", Boolean.valueOf(true));
			}
		else {
			ctx.setVariable("previousButtonNeeded", Boolean.valueOf(false));
			}
		try {
			if(imageDAO.hasNextSetOfFive(album.getOwner(), album.getTitle(), pageNumber*MAX_NUM_IMAGES)) {
				ctx.setVariable("nextButtonNeeded", Boolean.valueOf(true));
				}
			else {
				ctx.setVariable("nextButtonNeeded", Boolean.valueOf(false));
				}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		/*
		int listLength = albumImages.size();
		// se il valore di pageNumber fornito non è valido, la servlet risponde con un errore
		if(Math.ceil((double) listLength / MAX_NUM_IMAGES) < pageNumber && listLength != 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Impossibile caricare le immagini.");
			return;
		}
		// mostriamo 5 immagini alla volta
		int firstImageIndex = (pageNumber - 1)* MAX_NUM_IMAGES;
		int lastImageIndex = pageNumber * MAX_NUM_IMAGES;
		if(listLength > MAX_NUM_IMAGES) {
			if(lastImageIndex > listLength)
				lastImageIndex = listLength;
			// ulteriore controllo di integrità a supporto di possibili manipolazioni di pageNumber
			try {
				albumImages = albumImages.subList(firstImageIndex, lastImageIndex);
			} catch(IndexOutOfBoundsException e){
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Impossibile caricare le immagini.");
				return;
			}
		}
		
		// gestione della visualizzazione dei pulsanti "precedenti" e "successive"
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
		*/
		
		Image image = new Image();
		
		// se un'immagine viene selezionata, aggiorniamo la pagina con i dettagli
		// dell'immagine e con gli eventuali commenti
		
		Integer imageId = null;
		if(request.getAttribute("imageID") != null) {
			imageId = (int) request.getAttribute("imageID");
			ctx.setVariable("successMsg", "Il commento è stato aggiunto correttamente.");
		}
		
		if(request.getParameter("idImmagine") != null) {
			// controllo di integrità sul parametro "idImmagine"
			try {
				imageId = Integer.valueOf(request.getParameter("idImmagine"));
			}catch(NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Impossibile caricare le immagini.");
				return;
			}
		}
		
		// se l'espressione booleana contenuta nell'if viene valutata vera, siamo
		// sicuramente di fronte a un caso di manipolazione della query string
		if(request.getParameter("titoloImmagine") == null && request.getAttribute("titoloAlbum") == null &&
				request.getParameter("buttonValue") == null && request.getAttribute("imageID") == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri scorretti.");
			return;
		}
		
		List<Integer> checkImageID = null;
		// si entra nell'if se un'immagine è stata selezionata
		if(imageId != null ) {
			ctx.setVariable("imageClicked", Boolean.valueOf(true));
			// controllo di integrità dei parametri della richiesta
			try {
				image = imageDAO.getImageFromId(imageId);
				checkImageID = imageDAO.getImageIDsFromTitle(album.getOwner(), album.getTitle(), request.getParameter("titoloImmagine"));
			} catch (NumberFormatException | SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile caricare l'immagine.");
				return;
			}
			// ulteriore controllo di integrità dei parametri
			if(image == null || (!image.getImageTitle().equals(request.getParameter("titoloImmagine")) && 
					request.getAttribute("imageID") == null) ||
					(!checkImageID.contains(image.getID()) && request.getAttribute("imageID") == null)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Impossibile caricare l'immagine.");
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
			
			// aggiorniamo la pagina con i commenti appena estratti
			try {
				comments = commentDAO.getImageComments(image.getID());
				ctx.setVariable("commenti", comments);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile caricare i commenti.");
				return;
			}
			
		}
		// se l'espressione booleana contenuta nell'if viene valutata vera, siamo
		// sicuramente di fronte a un caso di manipolazione della query string
		else if(request.getAttribute("titoloAlbum") == null && request.getParameter("buttonValue") == null &&
				request.getAttribute("imageID") == null){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri scorretti.");
			return;
		}
		
		String path = "/WEB-INF/templates/AlbumPage";
		ctx.setVariable("albumImages", albumImages);
		
		templateEngine.process(path, ctx, response.getWriter());
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
