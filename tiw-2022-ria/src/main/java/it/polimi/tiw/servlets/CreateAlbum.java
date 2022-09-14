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
import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class CreateAlbum
 */
@WebServlet("/CreateAlbum")
@MultipartConfig
/**
 * La servlet gestisce la creazione di un album
 *
 */
public class CreateAlbum extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    
    public CreateAlbum() {
        super();
    }
    
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
		
		String title = request.getParameter("newAlbumTitle");
		Album album = new Album();
		
		// Controlliamo che il parametro non sia vuoto o nullo
		if(title == null || title.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Titolo dell'album mancante.");
			return; 
		}

		try { 
			/*Controlliamo che nel DB non esista un album con lo stesso nome per quell'utente*/
			AlbumDAO albumDAO = new AlbumDAO(connection);
			User utente = (User) session.getAttribute("utente");
			String username = utente.getUsername();
			album.setOwner(username);
			album.setTitle(title);
			
			// esiste già un album con lo stesso titolo per quell'utente
			if(false == albumDAO.checkAlbumTitle(album)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.setCharacterEncoding("UTF-8");
				response.getWriter().println("Album già esistente.");
				return;
			}
			// non esiste nel DB un album con lo stesso titolo per quell'utente
			else {
				try {
					// creiamo il nuovo album
					albumDAO.createAlbum(album);
				} catch (SQLException e) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					response.getWriter().println("Impossibile creare l'album, riprova.");
					return;
				}
				
				// settiamo in sessione l'album appena creato
				session.setAttribute("album", album);
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().println("Album creato correttamente.");
			}
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println("Impossbile controllare se l'album esiste già.");
			return;
		}
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}