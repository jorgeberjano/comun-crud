package es.jbp.comun.crud.elemento;

import java.util.HashMap;

/**
 * Representa un mapa de clave valor usado en los objetos de la capa de vista.
 * @author jberjano
 */
public class MapaValoresUI extends HashMap<String, ValorUI> {


    /**
     * Obtiene el valor de una clave. Si no existe se crea.
     */    
    
    @Override
    public ValorUI get(Object key) {
        ValorUI valor = super.get(key);
        if (valor == null) {
            valor = new ValorUI();
            //valor.setValor("autogenerado");
            put((String) key, valor);
        }
        return valor;
    }
}
