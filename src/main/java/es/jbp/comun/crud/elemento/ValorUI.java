package es.jbp.comun.crud.elemento;

import java.io.Serializable;
import java.util.List;

/**
 * Representa un valor de un campo de una entidad en la capa de vista.
 * @author jberjano
 */
public class ValorUI implements Serializable {

    private String valor = null;
    private List<String> opciones;
    
    public ValorUI() {
    }

    public ValorUI(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public List<String> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<String> opciones) {
        this.opciones = opciones;
    }

    @Override
    public String toString() {
        return valor;
    }
    
    
}
