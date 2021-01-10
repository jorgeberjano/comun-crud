package es.jbp.comun.crud.exportacion;

import es.jbp.comun.crud.elemento.ElementoUI;
import es.jbp.comun.crud.elemento.ValorUI;
import es.jbp.comun.crud.servicio.ServicioElemento;
import es.jbp.comun.ges.entidad.CampoGes;
import es.jbp.comun.ges.entidad.ConsultaGes;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Genera exportacion en CSV a partir de los datos de sesión de una tabla.
 *
 * @author jberjano
 */
public class ExportadorCsv implements Exportador {

    /**
     * Crea un informe a partir de los datos de sesión de una tabla
     */
    public ExportadorCsv() {
    }

    /**
     * Genera la exportación
     */
    public void generar(OutputStream outputStream, ServicioElemento servicio) throws Exception {
        ConsultaGes consulta = servicio.getConsultaVisualizada();
                
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        
        for (ElementoUI elementoUI : servicio.getListaElementos()) {
            boolean primero = true;
            for (CampoGes campo : consulta.getListaCampos()) {
                if (campo.isOculto()) {
                    continue;
                }
                if (primero) {
                    primero = false;                    
                } else {
                    writer.append(";");
                }
                ValorUI valor = elementoUI.get(campo.getIdCampo());
                String str = valor.getValor();
                writer.append(str);                
            }
            writer.append("\n");
        }
        writer.flush();
    }
}
