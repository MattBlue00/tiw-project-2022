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


@WebServlet("/GetUserAlbumsByDate")
/*
 * La servlet gestisce tutti gli album dell'utente in sessione ordinandoli secondo la data di creazione 
 */
public class GetUserAlbumsByDate extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

       
    public GetUserAlbumsByDate() {
        super();
    }
    
  //crea la servlet
  	public void init() throws ServletException {
  		connection = ConnectionHandler.getConnection(getServletContext());

  	}
  	
  	protected void doGet(HttpServletRequest request, HttpServletResponse response)
  			throws ServletException, IOException {
  		
  		HttpSession session = request.getSession();
  		request.setCharacterEncoding("UTF-8");
  		
  		User utente = (User) session.getAttribute("utente");
  		AlbumDAO albumDAO = new AlbumDAO(connection);
  		List<Album> albumsMiei = new ArrayList<Album>();
  		
  		try {
  			// il metodo getUserAlbumsByDate ritorna tutti gli album dell'utente in sessione ordinandoli secondo la data di creazione
			albumsMiei = albumDAO.getUserAlbumsByDate(utente.getUsername());
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore nel caricamento degli album per data");
			return;
		}
		
  		response.setStatus(HttpServletResponse.SC_OK);
  		// Configures Gson to serialize Date objects according to the pattern provided 
  		Gson gson = new GsonBuilder().setDateFormat("dd MMM yyyy").create();
		String json = gson.toJson(albumsMiei);
		
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
