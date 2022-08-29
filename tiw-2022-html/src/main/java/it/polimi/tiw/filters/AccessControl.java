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
 * Filtro che controlla che l'utente non abbia accesso a pagine dell'applicazione diverse dalla
 * pagina di login se non è correttamente loggato.
 */

@WebFilter("/AccessControl")
public class AccessControl extends HttpFilter implements Filter {
	private static final long serialVersionUID = 1L;

    public AccessControl() {
        super();
    }

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String loginPath = req.getServletContext().getContextPath() + "/login.html";

		HttpSession session = req.getSession();
		if (session.isNew() || session.getAttribute("utente") == null) {
			res.sendRedirect(loginPath);
			return;
		}
		// se l'utente è regolarmente loggato, la filter chain può proseguire
		chain.doFilter(request, response);
	}

	public void init(FilterConfig fConfig) throws ServletException {
	}

}
