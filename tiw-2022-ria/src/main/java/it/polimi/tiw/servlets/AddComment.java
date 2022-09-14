package it.polimi.tiw.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.DAO.CommentDAO;
import it.polimi.tiw.beans.Comment;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class AddComment.
 * La servlet gestisce l'aggiunta di un commento.
 */
@WebServlet("/AddComment")
@MultipartConfig
public class AddComment extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddComment() {
        super();
    }
    
    public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		connection = ConnectionHandler.getConnection(servletContext);
	}

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    		throws ServletException, IOException {
    	 
    	  HttpSession session = request.getSession();
    	  request.setCharacterEncoding("UTF-8");
    	  int imageID;
    	  
    	  //estraiamo i parametri dalla request, se sono nulli vengono lanciate eccezioni
    	  try {
    		  imageID = Integer.valueOf(request.getParameter("imageID")); // immagine a cui dobbiamo aggiungere il commento
    	  } catch(NullPointerException | NumberFormatException e) {
    		  response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    		  response.getWriter().println("Parametri non corretti.");
    		  return;
    	  }
    	  
    	  String commentText = request.getParameter("newCommentText"); // testo del nuovo commento
    	  if (commentText == null || commentText.isEmpty()) {
    		  response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    		  response.getWriter().println("Testo del commento mancante o inesistente.");
    		  return;
    	  }
    	  
    	  User user = (User) session.getAttribute("utente");
    	  String username = user.getUsername();
    	  CommentDAO commentDAO = new CommentDAO(connection);
    	  Comment comment = new Comment();
    	  
    	  // settiamo gli attributi del commento da aggiungere
    	  try {
	    	  comment.setImageId(imageID);
	    	  comment.setCommentNumber(commentDAO.getCommentNumber(imageID));
	    	  comment.setAuthor(username);
	    	  comment.setText(commentText);
	    	  commentDAO.createComment(comment); // creaiamo il commento
    	  } catch (SQLException e) {
    		  response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    		  response.getWriter().println("Impossibile aggiungere il commento.");
    		  return;
    	  }
    	  
    	  response.setStatus(HttpServletResponse.SC_OK);
    	  response.getWriter().println("Commento aggiunto correttamente."); 

    	 }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
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
