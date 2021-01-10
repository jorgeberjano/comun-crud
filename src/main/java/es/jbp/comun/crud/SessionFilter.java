package es.jbp.comun.crud;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * El filtro de sesión Spring
 * @author jberjano
 */
public class SessionFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession();
        if (session == null) {
            //session.setAttribute("iniciado", true);
            String contextPath = req.getContextPath();
            res.sendRedirect(contextPath + "/");
        } else if (session.getAttribute("usuario") == null && session.getAttribute("iniciado") == null) {
            session.setAttribute("iniciado", true);
            String contextPath = req.getContextPath();
            res.sendRedirect(contextPath + "/");
            return;
        }
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {

    }

}
