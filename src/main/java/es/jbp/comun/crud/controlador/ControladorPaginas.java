package es.jbp.comun.crud.controlador;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author jberjano
 */
@Controller

@RequestMapping({"/cargarPagina"})
public class ControladorPaginas {
    
    @RequestMapping(method = RequestMethod.GET)
    public String loadPage(HttpServletRequest request, ModelMap model) {

        String pagina = request.getParameter("pagina");
        return "redirect:" + pagina;
    }
}
