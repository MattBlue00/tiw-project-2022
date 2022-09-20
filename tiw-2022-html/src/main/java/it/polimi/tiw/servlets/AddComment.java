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

import it.polimi.tiw.DAO.CommentDAO;
import it.polimi.tiw.DAO.ImageDAO;
import it.polimi.tiw.beans.Comment;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.Constants;

/**
 * Servlet che si occupa dell'aggiunta di un commento.
 */

@WebServlet("/AddComment")
public class AddComment extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
    
    public AddComment() {
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		ImageDAO imageDAO = new ImageDAO(connection);
		int idLimit;
		
		/* 
		 * Qualsiasi ID immagine eventualmente fornito uguale o superiore a idLimit 
		 * corrisponderà a un'immagine non esistente.
		 */
		try {
			idLimit = imageDAO.getImageID();
		} catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Il database non ha risposto correttamente.");
			return;
		}
		
		// controllo di integrità sul parametro "commentText"
		String commentText = request.getParameter("commentText");
		if(commentText == null || commentText.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Valore inserito mancante o inesistente.");
			return;
		}
		if(commentText.length() > Constants.COMMENT_MAX_LENGTH) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Valore inserito troppo lungo.");
			return;
		}
		
		// controllo di integrità sul parametro "id_immagine"
		try {
			if(request.getParameter("id_immagine") == null || Integer.valueOf(request.getParameter("id_immagine")) < 1 ||
					Integer.valueOf(request.getParameter("id_immagine")) >= idLimit) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri errati.");
				return;
			}
		} catch(NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri errati.");
			return;
		}
		
		// salvataggio del commento nel database
		Comment comment = new Comment();
		User user = (User) session.getAttribute("utente");
		comment.setImageId(Integer.valueOf(request.getParameter("id_immagine")));
		comment.setAuthor(user.getUsername());
		comment.setText(commentText);
		CommentDAO commentDAO = new CommentDAO(connection);
		try {
			commentDAO.createComment(comment);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Il database non ha potuto salvare le informazioni relative al commento.");
			return;
		}
		
		// redirezione alla Servlet "AlbumPage" per l'aggiornamento dei dati della pagina
		String path = "/AlbumPage";
		request.setAttribute("imageID", Integer.valueOf(request.getParameter("id_immagine")));
		// controllo di integrità sul parametro "pageNuber"
		try {
			request.setAttribute("pageNumber", Integer.valueOf(request.getParameter("pageNumber")));
		} catch(NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri errati.");
			return;
		}
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
		dispatcher.forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
