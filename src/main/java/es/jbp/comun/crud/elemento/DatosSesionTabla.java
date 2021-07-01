package es.jbp.comun.crud.elemento;

import es.jbp.comun.ges.entidad.Filtro;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Información de sesión de una consulta.
 * Guarda la información de pagina actual, filtro, orden y lista de elementos actual.
 * @author jberjano
 */
public class DatosSesionTabla implements Serializable {

    public static int NUMERO_ELEMENTOS_PAGINA_TABLA = 10;
    
    private int paginaActual;
    private int numeroPaginas;
    private Filtro filtro;
    private String campoOrden;
    private boolean ordenDescendente;

    private List listaElementosPagina;
    private int numeroTotalElementos;
    private String mensajeError;
    
    private String uuid;

    public DatosSesionTabla() {
        paginaActual = 1;
        uuid = UUID.randomUUID().toString();
    }

    public String getUuid() {
        return uuid;
    }

    public int getPaginaActual() {
        return paginaActual;
    }

    public void setPaginaActual(int paginaActual) {
        this.paginaActual = paginaActual;
    }

    public int getNumeroPaginas() {
        return numeroPaginas;
    }

    public Filtro getFiltro() {
        return filtro;
    }

    public void setFiltro(Filtro filtro) {
        this.filtro = filtro;
    }

    public void setCampoOrden(String campoOrden) {
        this.campoOrden = campoOrden;
    }
    
    public void conmutarOrden(String campoOrden) {
        if (this.campoOrden != null && this.campoOrden.equals(campoOrden)) {
            ordenDescendente = !ordenDescendente;
        } else {
            ordenDescendente = false;
        }
        setCampoOrden(campoOrden);
    }
       
    public String getCampoOrden() {
        return campoOrden;
    }

    public void setOrdenDescendente(boolean b) {
        ordenDescendente = b;
    }
    
    public boolean isOrdenDescendente() {
        return ordenDescendente;
    }

    public int getElementosPorPagina() {
        return NUMERO_ELEMENTOS_PAGINA_TABLA;
    }

    public List getListaElementosPagina() {
        return listaElementosPagina;
    }
    
    public void setListaElementosPagina(List listaElementos) {
        listaElementosPagina = listaElementos;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    public void setMensajeError(String mensajeError) {
        this.mensajeError = mensajeError;
    }

    public void paginar(String accion) {

        actualizarNumeroPaginas();

        switch (accion) {            
            case "primero":
                paginaActual = 1;
                break;
            case "anterior":
                paginaActual--;
                break;
            case "siguiente":
                paginaActual++;
                break;
            case "ultimo":
                paginaActual = numeroPaginas;
                break;
        }
        if (paginaActual <= 0) {
            paginaActual = 1;
        } else if (paginaActual > numeroPaginas) {
            paginaActual = numeroPaginas;
        }
    }

    public void setNumeroTotalElementos(int numeroTotalElementos) {
        this.numeroTotalElementos = numeroTotalElementos;
        actualizarNumeroPaginas();
    }
    
    
    private void actualizarNumeroPaginas() {
        numeroPaginas = numeroTotalElementos == 0 ? 1 : (int) Math.ceil((double) numeroTotalElementos / (double) getElementosPorPagina());
        if (paginaActual > numeroPaginas) {
            paginaActual = numeroPaginas;
        }
    }     
}
