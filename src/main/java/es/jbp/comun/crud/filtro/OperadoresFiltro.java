package es.jbp.comun.crud.filtro;

import es.jbp.comun.utiles.sql.TipoDato;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Clase para crear un mapa con los operadores de filtro
 * @author Jorge
 */
public class OperadoresFiltro {

    public static Map<String, String> getOperadores(TipoDato tipo) {

        Map<String, String> mapa = new LinkedHashMap<>();

        if (tipo == null || tipo == TipoDato.CADENA) {
            mapa.put("{:campo} like :contieneValor", "contiene");
        }

        boolean esFecha = tipo == TipoDato.FECHA || tipo == TipoDato.FECHA_HORA;
        if (tipo == null || esFecha) {
            mapa.put("{$datevalue}({:campo}) = {$datevalue}(:valor)", "el día");
        }

        if (tipo == null || !esFecha) {
            mapa.put("{:campo} = :valor", "=");
        }

        if (tipo == null || tipo == TipoDato.FECHA || tipo == TipoDato.FECHA_HORA) {
            mapa.put("{$datevalue}({:campo}) >= {$datevalue}(:valor)", "desde el día");
            mapa.put("{$datevalue}({:campo}) <= {$datevalue}(:valor)", "hasta el día");
        }

        if (tipo == null || tipo == TipoDato.ENTERO || tipo == TipoDato.REAL) {
            mapa.put("{:campo} > :valor", ">");
            mapa.put("{:campo} >= :valor", ">=");
            mapa.put("{:campo} < :valor", "<");
            mapa.put("{:campo} <= :valor", "<=");
        }
        return mapa;
    }

    public static String getDescripcion(String operador) {
        return getOperadores(null).get(operador);
    }
}
