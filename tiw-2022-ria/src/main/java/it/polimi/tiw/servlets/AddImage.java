package it.polimi.tiw.servlets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import it.polimi.tiw.DAO.ImageDAO;
import it.polimi.tiw.beans.Image;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.Constants;

/**
 * Servlet implementation class CreateAlbum.
 * La servlet gestisce l'aggiunta di un'immagine.
 */

@WebServlet("/AddImage")
@MultipartConfig
public class AddImage extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
    private String imagePath = null;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddImage() {
        super();
    }
    
    public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		connection = ConnectionHandler.getConnection(servletContext);
		// starting path for saving images' files
    	imagePath = servletContext.getInitParameter("folderPath");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doPost(request, response);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
    
  	  	request.setCharacterEncoding("UTF-8");
		String imageTitle = (String) request.getParameter("newImageTitle");
		String imageDescription = (String) request.getParameter("newImageDescription");
		
		// controlliamo che i parametri non siano nulli o vuoti
		if(imageTitle == null || imageDescription == null || imageTitle.isEmpty() || imageDescription.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Valori mancanti o inesistenti.");
			return;
		}
		
		String albumTitle = request.getParameter("albumToModify");
		if(albumTitle == null || albumTitle.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Parametri non corretti.");
			return;
		}
		
		// controlliamo che imageTitle e imageDescription non superino il numero di caratteri permessi
		if(imageTitle.length() > Constants.IMAGE_TITLE_MAX_DIM || imageDescription.length() > Constants.IMAGE_DESCRIPTION_MAX_DIM) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Valori inseriti troppo lunghi.");
			return;
		}
		
		// controlliamo che il parametro necessario sia presente
		Part filePart = request.getPart("newImagePart");
		if (filePart == null || filePart.getSize() <= 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("File mancante o inesistente.");
			return;
		}
		
		// il file deve essere un'immagine
		String contentType = filePart.getContentType();
		if (!contentType.startsWith("image")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println("Il file non è un'immagine.");
			return;
		}
		
		// l'immagine deve essere leggibile
		try(InputStream fileContent = filePart.getInputStream()){
			ImageIO.read(fileContent);
		}
		catch(IOException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println("il file non è un'immagine leggibile.");
			return;
		}
		
		ImageDAO imageDao = new ImageDAO(this.connection);
		int imageID;
		try {
			// calcoliamo l'imageID della nuova immagine
			imageID = imageDao.getImageID();
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Il database non ha risposto correttamente.");
			return;
		}
		String imageOutputPath = imagePath + String.valueOf(imageID);
		
		// salviamo l'immagine nel file system
		File imageFile = new File(imageOutputPath);
		try(InputStream fileContent = filePart.getInputStream()) {
			Files.copy(fileContent, imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch(Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println("Il salvataggio del file caricato non si è concluso correttamente.");
			return;
		}
		
		BasicFileAttributes attributes = 
				Files.readAttributes(Paths.get(imageOutputPath), BasicFileAttributes.class);
		
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("utente");
		Image image = new Image();
		
		// settiamo tutti gli attributi dell'immagine da aggiungere
		image.setID(imageID);
		image.setOwner(user.getUsername());
		image.setAlbumTitle(albumTitle);
		image.setImageTitle(imageTitle);
		image.setDate(new Timestamp(attributes.creationTime().toMillis()));
		image.setDescription(imageDescription);
		image.setPath(imageOutputPath);
		try {
			// aggiungiamo la nuova immagine
			imageDao.createImage(image);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Il database non ha potuto salvare le informazioni relative all'immagine.");
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println("L'immagine \"" + image.getImageTitle() + "\" è stata aggiunta correttamente all'album.");

	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
