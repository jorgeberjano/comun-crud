package es.jbp.comun.crud.controlador;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.jbp.comun.crud.Crud;
import es.jbp.comun.crud.filtro.FiltroUI;
import es.jbp.comun.crud.elemento.DatosSesionTabla;
import es.jbp.comun.crud.elemento.ElementoUI;
import es.jbp.comun.crud.servicio.ServicioElemento;
import es.jbp.comun.crud.servicio.ServicioElemento.Modo;
import es.jbp.comun.ges.dao.EntidadGes;
import es.jbp.comun.ges.dao.Filtro;
import es.jbp.comun.ges.entidad.CampoGes;
import es.jbp.comun.ges.entidad.ConsultaGes;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import es.jbp.comun.utiles.conversion.Conversion;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

/**
 * Controlador para el mantenimiento de una tabla de entidades y su CRUD.
 *
 * @author jberjano
 */
@Controller
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ControladorCrud {

    private final Map<String, ServicioElemento> mapaServicios = new HashMap<>();

    @Autowired
    private ObjectFactory<Crud> crudFactory;

    private String mensajeError;

    private ServicioElemento obtenerServicio(String idConsulta) {
        return obtenerServicio(idConsulta, false);
    }
    
    private ServicioElemento obtenerServicio(String idConsulta, boolean seleccion) {

        if (idConsulta == null) {
            mensajeError = "No se ha especificado el nombre de la consulta";
            return null;
        }

        Crud crud = crudFactory.getObject();       
        if (crud.getUsuario() == null){
            mensajeError = "Se ha cerrado la sesión";
            return null;            
        }
        ConsultaGes consulta = crud.getConsultaPorId(idConsulta);
        if (consulta == null) {
            mensajeError = "No existe la consulta " + idConsulta;
            return null;
        }

        String nombreServicio = (seleccion ? "seleccion_" : "tabla_") + consulta.getIdConsulta();
        ServicioElemento servicio = mapaServicios.get(nombreServicio);

        if (servicio == null) {
            servicio = crud.getServicio(idConsulta);
            servicio.setDatosSesionTabla(new DatosSesionTabla());
            mapaServicios.put(nombreServicio, servicio);
        }

        return servicio;
    }

    private ModelAndView reportarError(String mensajeError, ModelMap model) {
        Crud crud = crudFactory.getObject();
        boolean sinSesion = crud.getUsuario() == null;
        
        model.addAttribute("mensaje_error", mensajeError);
        return new ModelAndView(sinSesion ? "vacio" : "crud/mensaje_error", model);
    }

//    @RequestMapping(value = "/inicio", method = RequestMethod.GET)
//    public ModelAndView inicio(HttpServletRequest request,
//            ModelMap model) {
//        return new ModelAndView("crud/inicial", model);
//    }

    @RequestMapping(value = "/iniciarTabla/{nombreConsulta}", method = RequestMethod.GET)
    public ModelAndView iniciarTabla(HttpServletRequest request,
            @PathVariable String nombreConsulta,
            ModelMap model) {
        
        ServicioElemento servicio = obtenerServicio(nombreConsulta);
        if (servicio == null) {
            return reportarError(mensajeError, model);
        }

        Filtro filtroPrevio = servicio.getFiltroPrevio();
        if (filtroPrevio != null) {
            servicio.setModo(Modo.FILTRAR);
            model.addAttribute("titulo", "Filtro previo de " + servicio.getNombreEnPlural());
            model.addAttribute("servicio", servicio);
            model.addAttribute("filtro", filtroPrevio);
            return new ModelAndView("crud/filtro", model);
        }
        servicio.inicializarTabla();
        model.addAttribute("infoSeleccion", "");
        return new ModelAndView("redirect:/tabla/" + nombreConsulta + "/", model);
    }

    /**
     * Mostrar la tabla de una consulta
     */
    @RequestMapping(value = "/tabla/{nombreConsulta}", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView tabla(HttpServletRequest request,
            @PathVariable String nombreConsulta,
            @ModelAttribute("infoSeleccion") String infoSeleccion,
            ModelMap model) {

        boolean esSeleccion = !Conversion.isBlank(infoSeleccion);
        ServicioElemento servicio = obtenerServicio(nombreConsulta, esSeleccion);
        if (servicio == null) {
            return reportarError(mensajeError, model);
        }

        boolean ok = servicio.actualizarPagina();
        if (!ok) {
            return reportarError(servicio.getMensajeError(), model);
        }

        model.addAttribute("servicio", servicio);

        return new ModelAndView("crud/tabla", model);
    }

    /**
     * Exportar el contenido de la tabla en PDF o CSV
     */
    @RequestMapping(value = "/exportarTabla/{formato}/{idConsulta}", method = RequestMethod.GET)
    public void imprimirTabla(HttpServletRequest request, HttpServletResponse response,
            @PathVariable String formato,
            @PathVariable String idConsulta,
            ModelMap model) {

        formato = formato.toLowerCase();
        
        ServicioElemento servicio = obtenerServicio(idConsulta);
        if (servicio == null) {
            return;
        }

        ServletOutputStream out;
        try {           
            response.setContentType("application/" + formato);
            response.setHeader("Content-disposition", "attachment; filename="+ idConsulta + "." + formato);
            out = response.getOutputStream();
        } catch (IOException ex) {
            return;
        }
        boolean ok = false;
        ok = servicio.exportar(formato, out);
        // TODO: ver como se puede informar del error
    }

    @RequestMapping(value = "/seleccionar/{idConsulta}", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView seleccionar(HttpServletRequest request,
            @PathVariable String idConsulta,
            @ModelAttribute("infoSeleccion") String infoSeleccion,
            @ModelAttribute("elemento") String elementoTexto,
            ModelMap model) {

        boolean esSeleccion = !Conversion.isBlank(infoSeleccion);
        ServicioElemento servicio = obtenerServicio(idConsulta, esSeleccion);
        if (servicio == null) {
            return reportarError(mensajeError, model);
        }
        JsonObject jsonSeleccion = (new JsonParser()).parse(infoSeleccion).getAsJsonObject();
        String idConsultaElemento = jsonSeleccion.get("idConsulta").getAsString();
        String idCampo = jsonSeleccion.get("idCampo").getAsString();

        ServicioElemento servicioElemento = obtenerServicio(idConsultaElemento);

        JsonObject jsonElemento = (new JsonParser()).parse(elementoTexto).getAsJsonObject();
        ElementoUI elemento = servicioElemento.crearElemento(jsonElemento);

        boolean ok = servicio.inicializarSeleccion(idCampo, servicioElemento, elemento);
        if (!ok) {
            return reportarError(servicio.getMensajeError(), model);
        }

        model.addAttribute("servicio", servicio);
        model.addAttribute("infoSeleccion", infoSeleccion);

        return new ModelAndView("crud/tabla", model);
    }

//    @RequestMapping(value = "/filtrar/{nombreConsulta}", method = RequestMethod.POST)
//    public ModelAndView filtrar(HttpServletRequest request,
//            @PathVariable String nombreConsulta,
//            @ModelAttribute("filtro") Filtro filtro,
//            @ModelAttribute("infoSeleccion") String infoSeleccion,
//            BindingResult result,
//            ModelMap model) {
//
//        boolean esSeleccion = !Conversion.isBlank(infoSeleccion);
//        ServicioElemento servicio = obtenerServicio(nombreConsulta, esSeleccion);
//        if (servicio == null) {
//            return reportarError(mensajeError, model);
//        }
//
//        boolean ok = servicio.filtrar(filtro);
//        if (!ok) {
//            model.addAttribute("mensaje_error", servicio.getMensajeError());
//            return new ModelAndView("crud/mensaje_error", model);
//        }
//
//        model.addAttribute("servicio", servicio);
//        model.addAttribute("infoSeleccion", infoSeleccion);
//
//        return new ModelAndView("crud/iniciarTabla", model);
//    }

    @RequestMapping(value = "/ordenar/{nombreConsulta}", method = RequestMethod.POST)
    public ModelAndView ordenar(HttpServletRequest request,
            @PathVariable String nombreConsulta,
            @ModelAttribute("campoOrden") String campoOrden,
            @ModelAttribute("infoSeleccion") String infoSeleccion,
            BindingResult result,
            ModelMap model) {

        boolean esSeleccion = !Conversion.isBlank(infoSeleccion);
        ServicioElemento servicio = obtenerServicio(nombreConsulta, esSeleccion);
        if (servicio == null) {
            return reportarError(mensajeError, model);
        }

        boolean ok = servicio.ordenar(campoOrden, null);
        if (!ok) {
            model.addAttribute("mensaje_error", servicio.getMensajeError());
            return new ModelAndView("crud/mensaje_error", model);
        }

        servicio.setModo(Conversion.isBlank(infoSeleccion) ? Modo.TABLA : Modo.SELECCIONAR);

        model.addAttribute("servicio", servicio);
        model.addAttribute("infoSeleccion", infoSeleccion);

        return new ModelAndView("crud/tabla", model);
    }

    /**
     * Realiza la paginacion de la tabla
     */
    @RequestMapping(value = "/paginar/{nombreConsulta}", method = RequestMethod.GET)
    public ModelAndView paginar(HttpServletRequest request,
            @PathVariable String nombreConsulta,
            @RequestParam(value = "accion", required = true) String accion,
            @ModelAttribute("infoSeleccion") String infoSeleccion,
            ModelMap model) {

        boolean esSeleccion = !Conversion.isBlank(infoSeleccion);
        ServicioElemento servicio = obtenerServicio(nombreConsulta, esSeleccion);
        if (servicio == null) {
            return reportarError(mensajeError, model);
        }

        boolean ok = servicio.paginar(accion);
        if (!ok) {
            model.addAttribute("mensaje_error", servicio.getMensajeError());
            return new ModelAndView("crud/mensaje_error", model);
        }

        servicio.setModo(Conversion.isBlank(infoSeleccion) ? Modo.TABLA : Modo.SELECCIONAR);

        model.addAttribute("servicio", servicio);
        model.addAttribute("infoSeleccion", infoSeleccion);

        return new ModelAndView("crud/tabla", model);
    }

    @RequestMapping(value = "/mostrar/{nombreConsulta}/{pk}", method = RequestMethod.GET)
    public ModelAndView mostrar(HttpServletRequest request,
            @PathVariable String nombreConsulta,
            @PathVariable String pk,
            ModelMap model) {

        ServicioElemento servicio = obtenerServicio(nombreConsulta);
        if (servicio == null) {
            return reportarError(mensajeError, model);
        }

        ElementoUI elemento = servicio.getElemento(pk);

        if (elemento == null) {
            String mensaje = "No se ha encontrado " + servicio.getNombreEnSingular() + " con " + pk;
            mensaje += ". Causa: " + servicio.getMensajeError();
            model.addAttribute("mensaje_error", mensaje);
            return new ModelAndView("crud/mensaje_error", model);
        }
        servicio.setModo(Modo.MOSTRAR);

        model.addAttribute("servicioElemento", servicio);
        model.addAttribute("pk", pk);
        model.addAttribute("elemento", elemento);

        return new ModelAndView("crud/editor", model);
    }

    @RequestMapping(value = "/modificar/{nombreConsulta}/{pk}", method = RequestMethod.GET)
    public ModelAndView modificar(HttpServletRequest request,
            @PathVariable String nombreConsulta,
            @PathVariable String pk,
            ModelMap model) {

        ServicioElemento servicio = obtenerServicio(nombreConsulta);
        if (servicio == null) {
            return reportarError(mensajeError, model);
        }

        servicio.setModo(Modo.MODIFICAR);

        ElementoUI elemento = servicio.getElementoEdicion(pk);
        model.addAttribute("servicioElemento", servicio);
        model.addAttribute("elemento", elemento);
        model.addAttribute("pk", pk);

        return new ModelAndView("crud/editor", model);
    }

    @RequestMapping(value = "/crear/{nombreConsulta}", method = RequestMethod.GET)
    public ModelAndView crear(HttpServletRequest request,
            @PathVariable String nombreConsulta,
            ModelMap model) {

        ServicioElemento servicio = obtenerServicio(nombreConsulta);
        if (servicio == null) {
            return reportarError(mensajeError, model);
        }

        servicio.setModo(Modo.CREAR);
        ElementoUI elemento = servicio.crearElemento();

        model.addAttribute("servicioElemento", servicio);
        model.addAttribute("elemento", elemento);

        return new ModelAndView("crud/editor", model);
    }

    @RequestMapping(value = "/guardar/{idConsulta}", method = RequestMethod.POST)
    public ModelAndView guardar(HttpServletRequest request,
            @PathVariable String idConsulta,
            @ModelAttribute("elemento") ElementoUI elementoFormulario,
            BindingResult result,
            ModelMap model) {

        ServicioElemento servicio = obtenerServicio(idConsulta);
        if (servicio == null) {
            return reportarError(mensajeError, model);
        }

        ElementoUI elemento = servicio.crearElemento();
        elemento.asignarValores(elementoFormulario);

        EntidadGes nuevaEntidad = servicio.crearEntidad(elemento);

        if (servicio.huboError()) {
            model.addAttribute("mensaje_error", servicio.getMensajeError());
            boolean esNuevo = nuevaEntidad == null;
            servicio.setModo(esNuevo ? Modo.CREAR : Modo.MOSTRAR);
            model.addAttribute("servicioElemento", servicio);
            model.addAttribute("elemento", elemento);
            return new ModelAndView("crud/editor", model);
        }
        String nuevaPk = nuevaEntidad.getClavePrimaria().toString();
        return new ModelAndView("redirect:/mostrar/" + idConsulta + "/" + nuevaPk + "/", model);
    }

    @RequestMapping(value = "/guardar/{idConsulta}/{pk}", method = RequestMethod.POST)
    public ModelAndView guardar(HttpServletRequest request,
            @PathVariable String idConsulta,
            @PathVariable String pk,
            @ModelAttribute("elemento") ElementoUI elementoFormulario,
            BindingResult result,
            ModelMap model) {

        ServicioElemento servicio = obtenerServicio(idConsulta);
        if (servicio == null) {
            return reportarError(mensajeError, model);
        }

        ElementoUI elemento = servicio.getElemento(pk);
        elemento.asignarValores(elementoFormulario);

        EntidadGes entidad = servicio.modificarEntidad(pk, elemento);

        if (entidad == null) {
            model.addAttribute("mensaje_error", servicio.getMensajeError());
            return new ModelAndView("crud/mensaje_error", model);
        }
        String nuevaPk = entidad.getClavePrimaria().toString();
        return new ModelAndView("redirect:/mostrar/" + idConsulta + "/" + nuevaPk + "/", model);
    }

    @RequestMapping(value = "/confirmarBorrado/{nombreConsulta}/{pk}", method = RequestMethod.GET)
    public ModelAndView confirmarBorrado(HttpServletRequest request,
            @PathVariable String nombreConsulta,
            @PathVariable String pk,
            ModelMap model) {

        ServicioElemento servicio = obtenerServicio(nombreConsulta);
        if (servicio == null) {
            return reportarError(mensajeError, model);
        }

        StringBuilder mensaje = new StringBuilder("Se va a proceder al borrado de ");
        mensaje.append(servicio.getNombreEnSingularConArticulo("la", "el"));
        mensaje.append(" ");
        mensaje.append(servicio.getNombreEntidad(pk));

        String uuid = servicio.getUuid();
        model.addAttribute("mensaje", mensaje.toString());
        model.addAttribute("url_aceptar", "/borrar/" + nombreConsulta + "/" + pk + "/" + uuid + "/");
        model.addAttribute("url_cancelar", "/mostrar/" + nombreConsulta + "/" + pk + "/");

        return new ModelAndView("crud/confirmacion", model);
    }

    @RequestMapping(value = "/borrar/{idConsulta}/{pk}/{uuid}", method = RequestMethod.GET)
    public ModelAndView borrar(HttpServletRequest request,
            @PathVariable String idConsulta,
            @PathVariable String pk,
            @PathVariable String uuid,
            ModelMap model) {

        ServicioElemento servicio = obtenerServicio(idConsulta);
        if (servicio == null) {
            return reportarError(mensajeError, model);
        }

        boolean ok = servicio.borrarEntidad(pk, uuid);
        if (!ok) {
            model.addAttribute("mensaje_error", servicio.getMensajeError());
            return new ModelAndView("crud/mensaje_error", model);
        }
        return new ModelAndView("redirect:/tabla/" + idConsulta + "/", model);
    }

    @RequestMapping(value = "/obtenerDatosRelacionados/{idConsulta}",
            method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> obtenerDatosRelacionados(HttpServletRequest request,
            @PathVariable String idConsulta) {

        String idCampoModificado = request.getParameter("campo");
        String valor = request.getParameter("valor");

        ServicioElemento servicio = obtenerServicio(idConsulta);
        if (servicio == null) {
            return null;//reportarError(mensajeError, model);
        }
        // Se obtiene el campo que provoca la selección en la tabla relacionada

        ConsultaGes consulta = servicio.getConsultaOriginal();
        CampoGes campoModificado = consulta.getCampoPorId(idCampoModificado);
        ServicioElemento servicioSeleccion = obtenerServicio(campoModificado.getConsultaSeleccion());
        if (servicioSeleccion == null) {
            return null;
        }
        // Se obtiene la entidad seleccionada
        String idCampoSeleccion = campoModificado.getIdCampoSeleccion();
        EntidadGes entidad = servicioSeleccion.buscarEntidad(idCampoSeleccion + "=" + valor);

        Map<String, String> mapa = new HashMap<>();

        // Se recorren los campos de la consulta, el que tenga relación con el campo modificado
        // se le asigna el valor que tiene en la entidad relacionada
        consulta.getListaCampos().stream()
                .filter((campo) -> (campo.getIdCampoRelacion().equals(idCampoModificado)))
                .forEach((campo) -> {
                    if (entidad != null) {
                        Object valorCampo = entidad.get(campo.getIdCampoSeleccion());
                        mapa.put(campo.getIdCampo(), campo.formatearValor(valorCampo, true));
                    } else if (!campo.getTabla().equals(consulta.getTabla())) {
                        mapa.put(campo.getIdCampo(), "");
                    }
                });
        return mapa;
    }

    @RequestMapping(value = "/botonEdicionPulsado/{nombreConsulta}",
            method = RequestMethod.POST)
    public ModelAndView botonEditorPulsado(HttpServletRequest request,
            @PathVariable String nombreConsulta,
            @ModelAttribute("elemento") ElementoUI elemento,
            ModelMap model) {

        String pk = request.getParameter("pk");
        String nombreCampo = request.getParameter("campo");
        Modo modo = Modo.valueOf(request.getParameter("modo"));

        ServicioElemento servicio = obtenerServicio(nombreConsulta);
        if (servicio == null) {
            return reportarError(mensajeError, model);
        }
        servicio.setModo(modo);
        boolean ok = servicio.botonEditorPulsado(nombreCampo, elemento);
        if (!ok) {
            model.addAttribute("mensaje_error", servicio.getMensajeError());
            return new ModelAndView("crud/mensaje_error", model);
        }

        model.addAttribute("servicioElemento", servicio);
        model.addAttribute("elemento", elemento);
        model.addAttribute("pk", pk);
        return new ModelAndView("crud/editor", model);
    }

    /**
     * Inicio de la edición de la configuración de la tabla
     */
    @RequestMapping(value = "/editarConfiguracionTabla/{idConsulta}", method = RequestMethod.GET)
    public ModelAndView editarConfiguracionTabla(HttpServletRequest request, HttpServletResponse response,
            @PathVariable String idConsulta,
            ModelMap model) {

        ServicioElemento servicio = obtenerServicio(idConsulta);
        if (servicio == null) {
            return reportarError(mensajeError, model);
        }

        servicio.inicializarConfiguracion();
        model.addAttribute("servicio", servicio);
        model.addAttribute("infoSeleccion", null);

        return new ModelAndView("crud/configuracion", model);
    }

    @RequestMapping(value = "/modificarConfiguracionCampo/{idConsulta}/{operacion}/{nombreCampo}", method = RequestMethod.GET)
    public ModelAndView modificarConfiguracionCampo(HttpServletRequest request,
            @PathVariable String idConsulta,
            @PathVariable String operacion,
            @PathVariable String nombreCampo,
            ModelMap model) {

        ServicioElemento servicio = obtenerServicio(idConsulta);
        if (servicio == null) {
            return reportarError(mensajeError, model);
        }

        boolean ok = servicio.modificarCampoConfiguracion(operacion, nombreCampo);
        if (!ok) {
            model.addAttribute("mensaje_error", servicio.getMensajeError());
            return new ModelAndView("crud/mensaje_error", model);
        }

        model.addAttribute("servicio", servicio);
        return new ModelAndView("crud/configuracion_campos", model);
    }

    /**
     * Fin de la configuración de la tabla
     */
    @RequestMapping(value = "/aceptarConfiguracionTabla/{idConsulta}", method = RequestMethod.GET)
    public ModelAndView finfConfiguracionTabla(HttpServletRequest request, HttpServletResponse response,
            @PathVariable String idConsulta,
            ModelMap model) {

        ServicioElemento servicio = obtenerServicio(idConsulta);
        if (servicio == null) {
            return reportarError(mensajeError, model);
        }

        boolean ok = servicio.aceptarModifiaciones();
        if (!ok) {
            model.addAttribute("mensaje_error", servicio.getMensajeError());
            return new ModelAndView("crud/mensaje_error", model);
        }
        model.addAttribute("servicio", servicio);
        model.addAttribute("infoSeleccion", null);

        //return new ModelAndView("crud/configuracion", model);
        return new ModelAndView("redirect:/tabla/" + idConsulta + "/", model);
    }

    /**
     * Inicio de la edición del filtro de la tabla
     */
    @RequestMapping(value = "/editarFiltroTabla/{idConsulta}", method = RequestMethod.GET)
    public ModelAndView editarFiltroTabla(HttpServletRequest request, HttpServletResponse response,
            @PathVariable String idConsulta,
            ModelMap model) {

        ServicioElemento servicio = obtenerServicio(idConsulta);
        if (servicio == null) {
            return reportarError(mensajeError, model);
        }

        servicio.inicializarFiltrado();
        model.addAttribute("servicio", servicio);
        model.addAttribute("infoSeleccion", null);
        model.addAttribute("filtro", servicio.getFiltro());

        return new ModelAndView("crud/filtro", model);
    }

    @RequestMapping(value = "/accionFiltro/{idConsulta}", method = RequestMethod.POST)
    public ModelAndView accionFiltro(HttpServletRequest request,
            @PathVariable String idConsulta,
            @RequestParam("accion") String accion,
            @RequestParam(name = "parametro", defaultValue = "") String parametro,
            @ModelAttribute("filtro") FiltroUI filtroUI,
            BindingResult result,
            ModelMap model) {

        ServicioElemento servicio = obtenerServicio(idConsulta);
        if (servicio == null) {
            return reportarError(mensajeError, model);
        }
        if (accion == null) {
            return reportarError("Acción no definida para los filtros", model);
        }

        filtroUI.regenerar();

        model.addAttribute("servicio", servicio);

        if (accion.equals("agregar")) {
            filtroUI.agregarCondicion(servicio.getCampo(parametro));
        } else if (accion.equals("borrar")) {
            Integer indice = Conversion.toInteger(parametro);
            filtroUI.borrarCondicion(indice);
        } else if (accion.equals("limpiar")) {
            filtroUI.borrarTodo();
        } else if (accion.equals("aceptar")) {
            servicio.filtrar(filtroUI);
            if (servicio.huboError()) {
                model.addAttribute("mensaje_error", servicio.getMensajeError());
            }
            servicio.setModo(Modo.TABLA);
            return new ModelAndView("redirect:/tabla/" + idConsulta + "/", model);
        } else if (accion.equals("cancelar")) {
            servicio.setModo(Modo.TABLA);
            return new ModelAndView("redirect:/tabla/" + idConsulta + "/", model);
        }
        model.addAttribute("filtro", filtroUI);
        return new ModelAndView("crud/filtro", model);
    }
    
    /**
     * Totalización de la tabla
     */
    @RequestMapping(value = "/totalizarTabla/{idConsulta}", method = RequestMethod.GET)
    public ModelAndView totalizarTabla(HttpServletRequest request, HttpServletResponse response,
            @PathVariable String idConsulta,
            ModelMap model) {

        ServicioElemento servicio = obtenerServicio(idConsulta);
        if (servicio == null) {
            return reportarError(mensajeError, model);
        }
        if (!servicio.prepararTotalizacion()) {
            return reportarError(servicio.getMensajeError(), model);
        }

        model.addAttribute("servicio", servicio);
        model.addAttribute("infoSeleccion", null);
        model.addAttribute("filtro", servicio.getFiltro());

        return new ModelAndView("crud/totalizacion", model);
    }

}
