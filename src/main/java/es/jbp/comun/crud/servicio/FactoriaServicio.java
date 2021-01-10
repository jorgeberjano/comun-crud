package es.jbp.comun.crud.servicio;

import es.jbp.comun.crud.Crud;

/**
 * Metodo factoria para los servicios.
 * @author Jorge Berjano
 */
public interface FactoriaServicio {
    ServicioElemento crearServicio(String idConsulta, Crud crud);
}
