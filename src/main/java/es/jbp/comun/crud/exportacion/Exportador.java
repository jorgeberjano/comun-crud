package es.jbp.comun.crud.exportacion;

import es.jbp.comun.crud.servicio.ServicioElemento;
import java.io.OutputStream;

/**
 * Exportador
 * @author jorge
 */
public interface Exportador {
    void generar(OutputStream outputStream, ServicioElemento servicio) throws Exception;
}
