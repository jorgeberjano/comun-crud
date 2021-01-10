package es.jbp.comun.crud.exportacion;

import es.jbp.comun.utiles.conversion.Conversion;
import java.util.HashMap;
import java.util.Map;

/**
 * Acumulador de valores de campos.
 * @author jorge
 */
public class Acumulador {
    
    private final Map<String, Double> mapaAcumulados = new HashMap<>();

    public void limpiar() {
        mapaAcumulados.clear();
    }

    public void acumularValor(String idCampo, Object valor) {
        Double incremento = Conversion.toDouble(valor);
        Double valorAcumulado = mapaAcumulados.get(idCampo);
        Double nuevoAcumulado = 0.0;
        if (valorAcumulado != null) {
            nuevoAcumulado += valorAcumulado;
        }
        if (incremento != null) {
            nuevoAcumulado += incremento;
        }
        mapaAcumulados.put(idCampo, nuevoAcumulado);
    }
    
    public Double getAcumulado(String idCampo) {
        return mapaAcumulados.get(idCampo);
    }
    
    public boolean hayAcumulados() {
        return !mapaAcumulados.isEmpty();
    }
}
