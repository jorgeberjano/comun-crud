package es.jbp.comun.crud.servicio;

import com.google.gson.JsonObject;
import es.jbp.comun.crud.Crud;
import es.jbp.comun.crud.elemento.DatosSesionTabla;
import es.jbp.comun.crud.elemento.ElementoUI;
import es.jbp.comun.crud.elemento.ValorUI;
import es.jbp.comun.crud.exportacion.Exportador;
import es.jbp.comun.crud.filtro.FiltroUI;
import es.jbp.comun.crud.filtro.OperadoresFiltro;
import es.jbp.comun.crud.exportacion.FactoriaExportadores;
import es.jbp.comun.ges.dao.AccesoEntidadesGes;
import es.jbp.comun.ges.entidad.ClavePrimaria;
import es.jbp.comun.ges.entidad.EntidadGes;
import es.jbp.comun.ges.entidad.Filtro;
import es.jbp.comun.ges.entidad.CampoGes;
import static es.jbp.comun.ges.entidad.CampoGes.*;
import es.jbp.comun.ges.entidad.ConsultaGes;
import es.jbp.comun.ges.entidad.TipoRolGes;
import es.jbp.comun.utiles.conversion.Conversion;
import es.jbp.comun.utiles.depuracion.GestorLog;
import es.jbp.comun.utiles.internacionalizacion.I;
import es.jbp.comun.utiles.sql.PaginaEntidades;
import es.jbp.comun.utiles.sql.SecuenciaMaximoMasUno;
import es.jbp.comun.utiles.sql.TipoDato;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio generico para proporcionar información de un elemento (entidad en la
 * capa de vista). Se encarga proporcionar las páginas de elementos de forma
 * paginada y almacenar provisionalmente los resultados (cache) si es necesario.
 * También se encarga del filtrado, la insercción, la modificación y el borrado.
 *
 * @author jberjano
 */
public class ServicioElemento {

    protected final Crud crud;
    protected final AccesoEntidadesGes repositorio;
    protected final boolean usarCache = false;
    protected List<ElementoUI> cacheElementos;
    protected ConsultaGes consultaOriginal;
    protected ConsultaGes consultaModificada;
    protected ConsultaGes consultaVisualizada;
    protected DatosSesionTabla datosSesionTabla;
    protected String mensajeError;
    protected Modo modo = Modo.TABLA;

    public enum Modo {
        TABLA,
        SELECCIONAR,
        MOSTRAR,
        MODIFICAR,
        CREAR,
        CONFIGURAR,
        FILTRAR,
        TOTALIZAR,
    };

    public ServicioElemento(String idConsulta, Crud crud) {

        this.consultaOriginal = crud.getConsultaPorId(idConsulta);
        this.consultaVisualizada = crud.getConsultaPersonalizadaPorId(idConsulta);

        if (consultaOriginal == null) {
            consultaOriginal = copiarConsulta(consultaVisualizada);
        }
        if (consultaVisualizada == null) {
            consultaVisualizada = copiarConsulta(consultaOriginal);
        }

        this.crud = crud;
        repositorio = new AccesoEntidadesGes(consultaOriginal, crud.getGestorConexiones(), crud.getGestorSimbolos());
    }

    private ConsultaGes copiarConsulta(ConsultaGes consulta) {
        if (consulta == null) {
            return new ConsultaGes();
        }
        try {
            return (ConsultaGes) consultaOriginal.clone();
        } catch (CloneNotSupportedException ex) {
            return consulta;
        }
    }

    public AccesoEntidadesGes getRepositorio() {
        return repositorio;
    }

    public String getUuid() {
        return datosSesionTabla.getUuid();
    }

    public String getIdConsulta() {
        return consultaOriginal.getIdConsulta();
    }

    public synchronized void inicializarTabla() {
        setModo(Modo.TABLA);
        limpiarCache();
        filtrar(null);
        ordenarPorDefecto();
    }

    public synchronized boolean inicializarSeleccion(String nombreCampo, ServicioElemento servicioElemento, ElementoUI elemento) {
        setModo(Modo.SELECCIONAR);
        limpiarCache();
        filtrar(null);
        ordenarPorDefecto();
        return true;
    }

    public synchronized void ordenarPorDefecto() {
        boolean ordenAscendente = consultaOriginal.tieneEstilo(ConsultaGes.CONSULTA_ORDEN_AUTO_ASC);
        boolean ordenDescendente = consultaOriginal.tieneEstilo(ConsultaGes.CONSULTA_ORDEN_AUTO_DESC);
        if (ordenAscendente || ordenDescendente) {
            for (CampoGes campo : consultaOriginal.getListaCampos()) {
                if (campo.tieneEstilo(CampoGes.CAMPO_ORDEN_INICIAL)) {
                    ordenar(campo.getIdCampo(), !ordenAscendente);
                    break;
                }
            }
        }
    }

    public CampoGes getCampo(String idCampo) {
        return consultaOriginal.getCampoPorId(idCampo);
    }

    public Map<String, String> getMapaCampos() {
        Map<String, String> mapa = new LinkedHashMap<>();
        consultaVisualizada.getListaCampos().stream().forEach((e) -> {
            mapa.put(e.getIdCampo(), e.getTitulo());
        });
        return mapa;
    }

    public Map<String, String> getMapaOperadores(String idCampo) {
        CampoGes campo = consultaOriginal.getCampoPorId(idCampo);
        return OperadoresFiltro.getOperadores(campo.getTipoDato());
    }

    /**
     * Devuelve un mapa con los valores que tiene este campo en la base de datos
     * y el nombre que lo representa, si lo tiene.
     *
     * @param idCampoClave
     * @param idCampoNombre
     * @return mapa de valores
     */
    public Map<String, String> getMapaValoresActuales(String idCampoClave, String idCampoNombre) {

        Map<String, String> mapa = new LinkedHashMap<>();
        mapa.put("", "");

        CampoGes campoClave = getCampo(idCampoClave);
        CampoGes campoNombre = getCampo(idCampoNombre);

        AccesoEntidadesGes repositorioSeleccion = new AccesoEntidadesGes(consultaOriginal, crud.getGestorConexiones(), crud.getGestorSimbolos());
        List<EntidadGes> lista = repositorioSeleccion.getLista();
        if (lista == null) {
            return mapa;
        }

        lista.stream().forEach((entidad) -> {
            String valorClave = Conversion.toString(entidad.get(campoClave.getIdCampo()));
            String valorNombre = Conversion.toString(entidad.get(campoNombre.getIdCampo()));
            mapa.put(valorClave, valorClave + " - " + valorNombre);
        });

        return mapa;
    }

    /**
     * Este metodo es similar a getConsultaVisualizada() pero se usa en las paginas jsp
     * @return 
     */
    public ConsultaGes getConsulta() {
        return consultaVisualizada;
    }

    public ConsultaGes getConsultaVisualizada() {
        return consultaVisualizada;
    }

    public ConsultaGes getConsultaModificada() {
        return consultaModificada;
    }

    public ConsultaGes getConsultaOriginal() {
        return consultaOriginal;
    }

    public List<CampoGes> getCamposVisibles() {
        return consultaVisualizada.getListaCampos();
    }

    public List<CampoGes> getCamposDisponibles() {
        return consultaOriginal.getListaCampos().stream().filter((campo) -> {
            return consultaModificada.getCampoPorId(campo.getIdCampo()) == null;
        }).collect(Collectors.toList());
    }

    public List<CampoGes> getCamposConfigurados() {
        return consultaModificada.getListaCampos();
    }

    public Modo getModo() {
        return modo;
    }

    public synchronized void setModo(Modo modo) {
        this.modo = modo;
    }

    public synchronized DatosSesionTabla getDatosSesionTabla() {
        return datosSesionTabla;
    }

    public synchronized void setDatosSesionTabla(DatosSesionTabla datosSesionTabla) {
        this.datosSesionTabla = datosSesionTabla;
    }

    public EntidadGes buscarEntidad(String pk) {
        ClavePrimaria clavePrimaria = parsearClavePrimaria(pk);
        EntidadGes entidad = repositorio.getEntidad(clavePrimaria);
        if (entidad == null) {
            mensajeError = repositorio.getMensajeError();
        }
        return entidad;
    }

    public synchronized ElementoUI getElemento(String pk) {
        EntidadGes entidad = buscarEntidad(pk);
        return entidad != null ? crearElemento(entidad, false) : null;
    }

    public ElementoUI getElementoEdicion(String pk) {
        EntidadGes entidad = buscarEntidad(pk);
        return entidad != null ? crearElemento(entidad, true) : null;
    }

    public List<ElementoUI> getListaElementos() {
        if (datosSesionTabla == null) {
            return new ArrayList();
        }

        List<ElementoUI> lista = new ArrayList();
        List<EntidadGes> listaEntidades = getListaEntidades();
        if (listaEntidades == null) {
            return null;
        }
        for (EntidadGes entidad : listaEntidades) {
            lista.add(crearElemento(entidad, false));
        }
        return lista;
    }

    /**
     * Crea un elemento inicial.
     */
    public synchronized ElementoUI crearElemento() {
        ElementoUI elemento = new ElementoUI();
        elemento.set("uuid", getUuid());

        for (CampoGes campo : consultaOriginal.getListaCampos()) {
            String valorPorDefecto = campo.getValorPorDefecto();
            elemento.set(campo.getIdCampo(), valorPorDefecto);
        }
        return elemento;
    }

    /**
     * Crea un elemento a partir de un objeto Json
     */
    public synchronized ElementoUI crearElemento(JsonObject objetoJson) {
        ElementoUI elemento = new ElementoUI();
        elemento.set("uuid", getUuid());

        for (CampoGes campo : consultaOriginal.getListaCampos()) {
            String valor = objetoJson.get(campo.getIdCampo()).getAsString();
            elemento.set(campo.getIdCampo(), valor);
        }
        return elemento;
    }

    public ElementoUI crearElemento(EntidadGes entidad, boolean paraEdicion) {
        ElementoUI elemento = new ElementoUI();
        elemento.asignarValores(entidad, consultaOriginal, paraEdicion);
        elemento.set("uuid", getUuid());
        return elemento;
    }

    /**
     * Crea una entidad a partir de un elemento.
     */
    public synchronized EntidadGes crearEntidad(ElementoUI elemento) {
        mensajeError = null;

        if (!validar(elemento)) {
            return null;
        }

        if (elemento == null || consultaOriginal == null) {
            mensajeError = "No se ha definido el elemento o la consulta";
            return null;
        }
        EntidadGes entidad = new EntidadGes();
        elemento.obtenerValores(entidad, consultaOriginal);
        asignarSecuenciaPk(entidad, consultaOriginal);

        boolean ok = insertarEntidad(entidad);
        if (!ok) {
            return null;
        }
        limpiarCache();
        return entidad;
    }

    protected boolean insertarEntidad(EntidadGes entidad) {
        boolean ok = repositorio.insertar(entidad);
        if (!ok) {
            mensajeError = repositorio.getMensajeError();
        }
        return ok;
    }

    /**
     * Modifica una entidad a partir de un elemento.
     */
    public synchronized EntidadGes modificarEntidad(String pk, ElementoUI elemento) {
        mensajeError = null;

        if (!validar(elemento)) {
            return null;
        }

        if (elemento == null || consultaOriginal == null) {
            mensajeError = "No se ha definido el elemento o la consulta";
            return null;
        }
        ClavePrimaria clavePrimaria = parsearClavePrimaria(pk);
        EntidadGes entidad = repositorio.getEntidad(clavePrimaria);
        if (entidad == null) {
            return null;
        }
        elemento.obtenerValores(entidad, consultaOriginal);

        boolean ok = modificarEntidad(entidad);
        if (!ok) {
            return null;
        }
        limpiarCache();
        return entidad;
    }

    protected boolean modificarEntidad(EntidadGes entidad) {
        boolean ok = repositorio.modificar(entidad);
        if (!ok) {
            mensajeError = repositorio.getMensajeError();
        }
        return ok;
    }

    /**
     * Asigna el valor del maximo mas uno si hay un unico campo clave y este
     * tieneEstilo de tipo entero.
     */
    private synchronized void asignarSecuenciaPk(EntidadGes entidad, ConsultaGes consulta) {
        CampoGes campo = consulta.getCampoClave();
        if (campo == null || campo.getTipoDato() != TipoDato.ENTERO) {
            return;
        }

        entidad.setValor(campo.getIdCampo(), new SecuenciaMaximoMasUno());
    }

    public synchronized boolean borrarEntidad(String pk, String uuid) {

        if (!verificarUuid(uuid)) {
            return false;
        }

        ClavePrimaria clavePrimaria = parsearClavePrimaria(pk);
        boolean ok = repositorio.borrar(clavePrimaria);
        if (ok) {
            limpiarCache();
        } else {
            mensajeError = repositorio.getMensajeError();
        }
        return ok;
    }

    public synchronized ClavePrimaria parsearClavePrimaria(String texto) {
        if (Conversion.isBlank(texto)) {
            return new ClavePrimaria();
        }
        ClavePrimaria clave = ClavePrimaria.crearDeCadena(texto, consultaOriginal);
        return clave;
    }

    public synchronized boolean actualizarPaginaConCache(DatosSesionTabla datosPagina) {

        if (cacheElementos == null) {
            List<EntidadGes> listaEntidades = getListaEntidades();
            if (listaEntidades == null) {
                return false;
            }
            cacheElementos = new ArrayList();
            for (EntidadGes entidad : listaEntidades) {
                cacheElementos.add(crearElemento(entidad, false));
            }
        }
        datosPagina.setNumeroTotalElementos(cacheElementos.size());

        List listaElementos = getPaginaResultados(cacheElementos, datosPagina.getElementosPorPagina(), datosPagina.getPaginaActual());
        datosPagina.setListaElementosPagina(listaElementos);
        return true;
    }

    public synchronized List<EntidadGes> getListaEntidades() {
        List<EntidadGes> lista = repositorio.getListaFiltrada(getFiltro(), getNombreSqlCampoOrden(), isOrdenDescendente());
        if (lista == null) {
            mensajeError = repositorio.getMensajeError();
        }
        return lista;
    }

    public synchronized void limpiarCache() {
        cacheElementos = null;
    }

    public synchronized boolean actualizarPagina() {

        try {
            if (usarCache) {
                return actualizarPaginaConCache(datosSesionTabla);
            }

            int primerElemento = (datosSesionTabla.getPaginaActual() - 1) * datosSesionTabla.getElementosPorPagina();

            PaginaEntidades<EntidadGes> entidades = repositorio.getPagina(
                    getFiltro(), getNombreSqlCampoOrden(), isOrdenDescendente(),
                    primerElemento, datosSesionTabla.getElementosPorPagina());

            if (entidades == null) {
                mensajeError = repositorio.getMensajeError();
                return false;
            }
            
            datosSesionTabla.setNumeroTotalElementos(entidades.getNumeroTotalEntidades());
            
            List listaElementos = new ArrayList();
            for (EntidadGes entidad : entidades.getListaEntidades()) {
                listaElementos.add(crearElemento(entidad, false));
            }
            datosSesionTabla.setListaElementosPagina(listaElementos);
            return true;
        } catch (Exception ex) {
            mensajeError = ex.getMessage();
            return false;
        }
    }

    public synchronized static List<?> getPaginaResultados(List<?> resultados, int elementosPagina, int pagina) {
        if (resultados == null || resultados.isEmpty()) {
            return null;
        }
        int posicionInicial = elementosPagina * (pagina - 1);
        int posicionFinal = elementosPagina * pagina;
        if (posicionInicial < 0) {
            posicionInicial = 0;
        }
        int total = resultados.size();
        if (posicionFinal > total) {
            posicionFinal = total;
        }

        List<Object> list = new ArrayList<>();
        for (int i = posicionInicial; i < posicionFinal; i++) {
            list.add(resultados.get(i));
        }

        return list;
    }

    public boolean huboError() {
        return mensajeError != null;
    }

    public synchronized String getMensajeError() {
        return mensajeError;
    }

    public synchronized List<String> getListaValoresCampo(CampoGes campoGes) {
        if (!Conversion.isBlank(campoGes.getConsultaSeleccion())
                && !campoGes.tieneEstilo(CampoGes.CAMPO_MOSTRAR_BOTON_LUPA)) {
            ConsultaGes consultaSeleccion = crud.getConsulta(campoGes.getConsultaSeleccion());
            if (consultaSeleccion == null) {
                return null;
            }
            AccesoEntidadesGes repositorioSeleccion = new AccesoEntidadesGes(consultaSeleccion, crud.getGestorConexiones(), crud.getGestorSimbolos());
            return repositorioSeleccion.getListaValores(campoGes.getIdCampo());
        } else {
            return null;
        }
    }

    public synchronized String getTitulo() {
        switch (modo) {
            case TABLA:
                return getNombreEnPlural();
            case SELECCIONAR:
                return "Selección de " + getNombreEnSingular();
            case MOSTRAR:
                return "Visualización de " + getNombreEnSingular();
            case MODIFICAR:
                return "Modificación de " + getNombreEnSingular();
            case CREAR:
                return "Creación de " + getNombreEnSingular();
            case CONFIGURAR:
                return "Configuración de " + getNombreEnPlural();
            case FILTRAR:
                return "Filtrado de " + getNombreEnPlural();
            case TOTALIZAR:
                return "Totalización de " + getNombreEnPlural();
        }
        return null;
    }

    public synchronized String getSubtitulo() {
        if (datosSesionTabla == null || datosSesionTabla.getFiltro() == null) {
            return "";
        }
        return getDescripcionFiltro();
    }

    private String getDescripcionFiltro() {
        return datosSesionTabla.getFiltro().getDescripcion();
    }

    public synchronized boolean estaEnModoEdicion() {
        return modo == Modo.MODIFICAR || modo == Modo.CREAR;
    }

    public String getNombreEnSingularConArticulo(String articuloFemenino, String articuloMasculino) {
        boolean femenino = consultaOriginal.tieneEstilo(ConsultaGes.CONSULTA_NOMBRE_FEMENINO);
        return new StringBuilder().append(femenino ? articuloFemenino : articuloMasculino).append(" ").append(getNombreEnSingular()).toString();
    }

    public synchronized String getNombreEnPlural() {
        return consultaOriginal.getNombreEnPlural();
    }

    public synchronized String getNombreEnSingular() {
        return consultaOriginal.getNombreEnSingular();
    }

    public Filtro getFiltroPrevio() {
        List<CampoGes> camposFiltro = consultaOriginal.construirListaCamposFiltroPrevio();
        if (camposFiltro == null || camposFiltro.isEmpty()) {
            return null;
        }

        FiltroUI filtro = new FiltroUI();
        camposFiltro.forEach((campo) -> {
            filtro.agregarCondicion(campo);
        });
        return filtro;
    }

    public synchronized boolean filtrar(Filtro filtro) {
        setFiltro(filtro);
        limpiarCache();
        return actualizarPagina();
    }

    public void setFiltro(Filtro filtro) {
        datosSesionTabla.setFiltro(filtro);
    }

    public Filtro getFiltro() {
        return datosSesionTabla.getFiltro();
    }

    public synchronized boolean ordenar(String campoOrden, Boolean descendente) {
        datosSesionTabla.setCampoOrden(campoOrden);
        if (descendente == null) {
            datosSesionTabla.conmutarOrden(campoOrden);
        } else {
            datosSesionTabla.setOrdenDescendente(descendente);
        }
        limpiarCache();
        return actualizarPagina();
    }

    public synchronized boolean paginar(String accion) {
        datosSesionTabla.paginar(accion);
        limpiarCache();
        return actualizarPagina();
    }

    public synchronized boolean esEditable(CampoGes campoGes) {
        boolean mismaTabla = campoGes.getTabla().equals(consultaOriginal.getTabla());
        return !campoGes.tieneEstilo(CampoGes.CAMPO_NO_EDITABLE)
                && (mismaTabla || !Conversion.isBlank(campoGes.getConsultaSeleccion()));
    }

    public synchronized int getPaginaActual() {
        return datosSesionTabla.getPaginaActual();
    }

    public synchronized int getNumeroPaginas() {
        return datosSesionTabla.getNumeroPaginas();
    }

    public synchronized List getListaElementosPagina() {
        return datosSesionTabla.getListaElementosPagina();
    }

    public String getTextoBoton(String nombreCampo) {
        return null;
    }

    public boolean botonEditorPulsado(String nombreCampo, ElementoUI elemento) {
        return true;
    }

    public synchronized boolean validar(ElementoUI elemento) {

        for (CampoGes campo : consultaOriginal.getListaCampos()) {

            if (campo.tieneEstilo(CAMPO_NO_EDITABLE)) {
                continue;
            }
            String valor = elemento.get(campo.getIdCampo()).getValor();
            if (campo.isSiempreMayusculas()) {
                valor = valor.toUpperCase();
                elemento.set(campo.getIdCampo(), valor);
            }
            boolean requerido
                    = campo.tieneEstilo(CAMPO_REQUERIDO)
                    || campo.tieneEstilo(CAMPO_NO_NULO)
                    || campo.tieneEstilo(CAMPO_CLAVE);
            if (requerido && Conversion.isBlank(valor)) {
                mensajeError = "Debe especificar un valor para el campo " + campo.getTitulo();
                return false;
            }
        }

        return true;
    }

    private boolean verificarUuid(String uuid) {
        if (uuid == null || !uuid.equals(getUuid())) {
            mensajeError = "Error de seguridad";
            return false;
        }
        return true;
    }

    /**
     * Obtiene el nombre de la entidad definida por un pk.
     */
    public String getNombreEntidad(String pk) {

        ClavePrimaria clavePrimaria = parsearClavePrimaria(pk);
        EntidadGes entidad = repositorio.getEntidad(clavePrimaria);
        if (entidad == null) {
            return "inexistente";
        }
        String textoNombre = null;
        String textoClave = null;
        for (CampoGes campo : consultaOriginal.getListaCampos()) {
            if (campo.getTipoRol() == TipoRolGes.NOMBRE && campo.getTabla().equals(consultaOriginal.getTabla())) {
                textoNombre = Conversion.concatenar(textoNombre, " ", Conversion.toString(entidad.get(campo.getIdCampo())));
            }
            if (campo.isClave()) {
                textoClave = Conversion.concatenar(textoClave, " ", Conversion.toString(entidad.get(campo.getIdCampo())));
            }
        }
        if (!Conversion.isBlank(textoNombre)) {
            return textoNombre;
        }
        if (!Conversion.isBlank(textoClave)) {
            return textoClave;
        }
        return "sin nombre";
    }

    public String getNombreCampoFocoInicial() {
        for (CampoGes campo : consultaOriginal.getListaCampos()) {
            boolean requiereFoco = !campo.tieneEstilo(CAMPO_OCULTO | CAMPO_NO_EDITABLE);
            if (requiereFoco) {
                return campo.getIdCampo();
            }
        }
        return "";
    }

    public boolean permitirSeleccion() {

        if (modo == Modo.SELECCIONAR) {
            return true;
        }

        if (consultaOriginal.tieneEstilo(ConsultaGes.CONSULTA_SIN_ALTA)
                && consultaOriginal.tieneEstilo(ConsultaGes.CONSULTA_SIN_BAJA)
                && consultaOriginal.tieneEstilo(ConsultaGes.CONSULTA_SIN_MODIFICACION)) {
            return false;
        }
        return true;
    }

    public String getCampoOrden() {
        return datosSesionTabla.getCampoOrden();
    }
    
    public String getNombreSqlCampoOrden() {
        
        CampoGes campo = consultaOriginal.getCampoPorId(getCampoOrden());
        if (campo == null) {
            return null;
        }
        return campo.getNombre();
    }

    public boolean isOrdenDescendente() {
        return datosSesionTabla.isOrdenDescendente();
    }

    public boolean tieneBotones(CampoGes campo) {
        if (!Conversion.isBlank(getTextoBoton(campo.getIdCampo()))) {
            return true;
        }
        if (!Conversion.isBlank(campo.getConsultaSeleccion())) {
            return true;
        }

        if (campo.getTipoDato() == TipoDato.FECHA || campo.getTipoDato() == TipoDato.FECHA_HORA) {
            return true;
        }

        return false;
    }

    public synchronized void inicializarFiltrado() {
        modo = Modo.FILTRAR;
    }

    public synchronized void inicializarConfiguracion() {
        modo = Modo.CONFIGURAR;
        consultaModificada = clonarConsulta(consultaVisualizada);
    }

    public boolean modificarCampoConfiguracion(final String operacion, final String nombreCampo) {
        boolean ok = false;
        try {
            if ("adelantar".equals(operacion)) {
                ok = consultaModificada.modificarPosicionCampo(nombreCampo, -1);
            } else if ("atrasar".equals(operacion)) {
                ok = consultaModificada.modificarPosicionCampo(nombreCampo, +1);
            } else if ("eliminar".equals(operacion)) {
                ok = consultaModificada.eliminarCampo(nombreCampo);
            } else if ("agregar".equals(operacion)) {
                CampoGes campo = consultaOriginal.getCampoPorId(nombreCampo);
                if (campo != null) {
                    consultaModificada.agregarCampo(campo);
                    ok = true;
                }
            } else if ("restaurar".equals(operacion)) {
                ok = restaurarConfiguracion();
            }
            if (!ok) {
                mensajeError = I.txt("La operación " + operacion + " no es válida para el campo " + nombreCampo);
            }
        } catch (Exception ex) {
            mensajeError = ex.getMessage();
        }
        return ok;
    }

    public boolean aceptarModifiaciones() {
        consultaVisualizada = clonarConsulta(consultaModificada);
        crud.guardarConsultaPersonalizada(consultaVisualizada);
        return consultaVisualizada != null;
    }

    public boolean restaurarConfiguracion() {
        consultaModificada = clonarConsulta(consultaOriginal);
        return consultaModificada != null;
    }

    private ConsultaGes clonarConsulta(ConsultaGes consulta) {
        try {
            return (ConsultaGes) consulta.clone();
        } catch (CloneNotSupportedException ex) {
            mensajeError = ex.getMessage();
            return null;
        }
    }

    public boolean exportar(String formato, OutputStream out) {
        Exportador exportador = FactoriaExportadores.crearExportador(formato);
        if (exportador == null) {
            mensajeError = "En formato " + formato + " no está soportado";
            return false;
        }
        try {
            exportador.generar(out, this);
        } catch (Exception ex) {
            mensajeError = "No se ha podido exportar el archivo " + formato;
            GestorLog.error(mensajeError, ex);
            return false;
        }
        return true;
    }

    public String getValor(ElementoUI elemento, String idCampo) {
        ValorUI valor = elemento.get(idCampo);
        if (valor == null || valor.getValor() == null) {
            return "";
        }
        return valor.getValor();
    }
    
    public boolean prepararTotalizacion() {
        modo = Modo.TOTALIZAR;
        return true;
    }
    
    public String getTotalizacion() {
        return "No hay nada que totalizar";
    }
    
    public Object obtenerValorSimbolo(String simbolo) {
        return crud.getGestorSimbolos().obtenerValorSimbolo(simbolo);
    }
}
