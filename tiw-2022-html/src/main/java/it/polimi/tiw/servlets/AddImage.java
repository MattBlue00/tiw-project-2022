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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.DAO.ImageDAO;
import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.Image;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class CreateAlbum
 */

@WebServlet("/AddImage")
@MultipartConfig
public class AddImage extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
    private String imagePath = null;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddImage() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		connection = ConnectionHandler.getConnection(servletContext);
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		// starting path for saving images' files
    	imagePath = getServletContext().getInitParameter("imagePath");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        
		Part filePart = request.getPart("image");
		// the parameter needed must be present
		if (filePart == null || filePart.getSize() <= 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File mancante o inesistente.");
			return;
		}
		
		// the file must be an image
		String contentType = filePart.getContentType();
		if (!contentType.startsWith("image")) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Il file non è un'immagine.");
			return;
		}
		
		// the image must be readable
		try(InputStream fileContent = filePart.getInputStream()){
			ImageIO.read(fileContent);
		}
		catch(IOException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Il file non è un'immagine leggibile.");
			return;
		}
		
		ImageDAO imageDao = new ImageDAO(this.connection);
		int imageID;
		try {
			imageID = imageDao.getImageID();
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Il database non ha risposto correttamente.\n");
			return;
		}
		String imageOutputPath = imagePath + String.valueOf(imageID);
		
		// save image in the file system
		File imageFile = new File(imageOutputPath);
		try(InputStream fileContent = filePart.getInputStream()) {
			Files.copy(fileContent, imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch(Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Il salvataggio del file caricato non si è concluso corretamente");
			return;
		}
		
		BasicFileAttributes attributes = 
				Files.readAttributes(Paths.get(imageOutputPath), BasicFileAttributes.class);
		
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("utente");
		Album album = (Album) session.getAttribute("album");
		Image image = new Image();
		image.setID(imageID);
		image.setOwner(user.getUsername());
		image.setAlbumTitle(album.getTitle());
		image.setImageTitle(((String) request.getParameter("imageTitle")));
		image.setDate(new Timestamp(attributes.creationTime().toMillis()));
		image.setDescription(((String) request.getParameter("imageDescription")));
		image.setPath(imageOutputPath);
		try {
			imageDao.createImage(image);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Il database non ha potuto salvare le informazioni relative all'immagine.\n");
			return;
		}
		
		ctx.setVariable("successMsg", "L'immagine \"" + image.getImageTitle() + "\" è stata aggiunta correttamente.");
		String path = "/WEB-INF/templates/AddImage.html";
		templateEngine.process(path, ctx, response.getWriter());

	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
