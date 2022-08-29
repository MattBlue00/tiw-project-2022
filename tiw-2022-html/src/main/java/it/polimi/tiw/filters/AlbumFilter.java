package it.polimi.tiw.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Filtro che controlla che l'utente non abbia accesso a pagine relative agli album senza
 * averlo selezionato regolarmente. Questo filtro è divenuto necessario a seguito della
 * decisione di salvare in sessione l'album selezionato da un utente.
 */

@WebFilter("/AlbumFilter")
public class AlbumFilter extends HttpFilter  implements Filter{
	private static final long serialVersionUID = 1L;
	
    public AlbumFilter() {
        super();
    }

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String homePagePath = req.getServletContext().getContextPath() + "/HomePage";

		HttpSession session = req.getSession();
		if (session.getAttribute("album") == null) {
			res.sendRedirect(homePagePath);
			return;
		}
		// se l'utente ha regolarmente selezionato un album, la filter chain può proseguire
		chain.doFilter(request, response);
	}

	public void init(FilterConfig fConfig) throws ServletException {
	}

}
