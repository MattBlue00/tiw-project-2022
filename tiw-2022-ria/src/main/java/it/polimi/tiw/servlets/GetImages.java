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

import it.polimi.tiw.DAO.ImageDAO;
import it.polimi.tiw.beans.Image;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class GetImages.
 * La servlet gestisce le immagini contenute in un certo album
 */
@WebServlet("/GetImages")
public class GetImages extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
    
    public GetImages() {
        super();
    }
    
    public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    		throws ServletException, IOException {

    	request.setCharacterEncoding("UTF-8");
    	String albumTitle = request.getParameter("albumTitle");
    	String albumOwner = request.getParameter("albumOwner");
    	
    	// controlliamo che i parametri non siano nulli o vuoti
    	if(albumTitle == null || albumOwner == null || albumTitle.isEmpty() || albumOwner.isEmpty()) {
    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Parametri non corretti.");
			return;
    	}
    	
    	ImageDAO immagineDAO = new ImageDAO(connection);
    	List<Image> immagini = new ArrayList<Image>();    	
    	
    	try {
    		// il metodo getAlbumImages ritorna tutte le immagini di un certo album (dati titolo e proprietario)
    		immagini = immagineDAO.getAlbumImages(albumOwner, albumTitle);
    	}catch(SQLException e) {
    		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Impossibile trovare le immagini dell'album");
			return;
    	}
    	
    	// Configures Gson to serialize Date objects according to the pattern provided 
    	Gson gson = new GsonBuilder().setDateFormat("dd MMM yyyy").create();
		String json = gson.toJson(immagini);
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);

    }
    
    public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}  

}