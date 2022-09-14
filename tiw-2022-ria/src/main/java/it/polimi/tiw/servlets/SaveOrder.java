package it.polimi.tiw.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.DAO.AlbumDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class SaveOrder.
 * Servlet che gestisce l'ordinamento degli album dell'utente.
 */
@MultipartConfig
@WebServlet("/SaveOrder")
public class SaveOrder extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

    
    public SaveOrder() {
        super();
    }
    
  //crea la servlet
  	public void init() throws ServletException {
  		connection = ConnectionHandler.getConnection(getServletContext());  		
  	}
  	
  	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
  		doPost(request, response);
  	}
   
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		request.setCharacterEncoding("UTF-8");
		
		// controlliamo che i parametri non siano nulli o vuoti
		String albumList = request.getParameter("lista");
		if(albumList == null || albumList.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Parametri non corretti.");
			return;
		}
		// albums contiene la lista degli albums nel nuovo ordine e di cui bisogna aggiornare l'indice nel database
		String[] albums = albumList.split(",");
		
		User user = (User) session.getAttribute("utente");
		String albumOwner = user.getUsername();
		AlbumDAO albumDAO = new AlbumDAO(connection);
		
		try {
			// si cicla su ogni album contenuto in albums
			for(int i=0; i<albums.length; i++)
				// la variabile i sarà il nuovo indice dell'album
				// aggiorniamo il database
				albumDAO.modifyAlbumOrder(i, albumOwner, albums[i]);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Impossibile modificare l'ordine degli album.");
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("Modifica avvenuta correttamente.");
		
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}