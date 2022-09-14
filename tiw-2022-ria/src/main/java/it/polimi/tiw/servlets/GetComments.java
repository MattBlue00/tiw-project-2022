package it.polimi.tiw.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.DAO.CommentDAO;
import it.polimi.tiw.beans.Comment;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class GetComment.
 * La servlet gestisce i commenti di una certa immagine.
 */
@WebServlet("/GetComments")
public class GetComments extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetComments() {
        super();
    }
    
      //crea la servlet
      	public void init() throws ServletException {
      		connection = ConnectionHandler.getConnection(getServletContext());
      	}
      	
      	protected void doGet(HttpServletRequest request, HttpServletResponse response)
      			throws ServletException, IOException {
      		
      		request.setCharacterEncoding("UTF-8");
      		int imageID;
      		
      		// controlliamo che il parametro non sia nullo e che sia un intero
      		try {
      			imageID = Integer.valueOf(request.getParameter("imageID"));
      		} catch(NullPointerException | NumberFormatException e) {
      			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Parametri non corretti.");
				return;
      		}

    		List<Comment> comments = new ArrayList<Comment>();
    		CommentDAO commentoDAO = new CommentDAO(connection);
    		
    		try {
    			// il metodo getImageComments ritorna tutti i commenti di una certa immagine dato il suo ID
    			comments = commentoDAO.getImageComments(imageID);
    			
    		} catch (SQLException e) {
    			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			response.getWriter().println("Impossibile trovare i commenti.");
    			return;
    		}
    		
    		// Configures Gson to serialize Date objects according to the pattern provided 
    		Gson gson = new GsonBuilder().setDateFormat("dd MMM yyyy").create();
    		String json = gson.toJson(comments);
    		
    		response.setContentType("application/json");
    		response.setCharacterEncoding("UTF-8");
    		response.getWriter().write(json);		

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