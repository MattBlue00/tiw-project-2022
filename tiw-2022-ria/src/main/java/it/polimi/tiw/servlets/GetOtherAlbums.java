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
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.DAO.AlbumDAO;
import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class GetOtherAlbums.
 * La servlet gestisce tutti gli album che non sono dell'utente in sessione
 */
@WebServlet("/GetOtherAlbums")
public class GetOtherAlbums extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    public GetOtherAlbums() {
        super();
    }

  //crea la servlet
  	public void init() throws ServletException {
  		connection = ConnectionHandler.getConnection(getServletContext());
  	}
  	
  	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
  		
  		HttpSession session = request.getSession(); 	
  		request.setCharacterEncoding("UTF-8");
  		
  		User utente = (User) session.getAttribute("utente");
  		AlbumDAO albumDAO = new AlbumDAO(connection);
  		List<Album> albumsNonMiei = new ArrayList<Album>();
  		
  		try {
  			// il metodo getOtherAlbums ritorna tutti gli album che non sono dell'utente in sessione
			albumsNonMiei = albumDAO.getOtherAlbums(utente.getUsername());
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println("Non è stato possibile recuperare gli album.");
			return;
		}
		
  		// Configures Gson to serialize Date objects according to the pattern provided 
  		response.setStatus(HttpServletResponse.SC_OK);
  		Gson gson = new GsonBuilder().setDateFormat("dd MMM yyyy").create();
  		String json = gson.toJson(albumsNonMiei);
		
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