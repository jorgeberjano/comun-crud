package es.jbp.comun.crud.filtro;

import es.jbp.comun.ges.entidad.Filtro;
import es.jbp.comun.ges.entidad.CampoGes;
import es.jbp.comun.ges.entidad.ConsultaGes;
import es.jbp.comun.utiles.conversion.Conversion;
import es.jbp.comun.utiles.sql.compatibilidad.FormateadorSql;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Representa un filtro en la capa de vista.
 * @author jorge
 */
public class FiltroUI implements Filtro, Serializable {
    
    private Map<String, CondicionFiltroUI> mapaCondiciones = new HashMap<>();
    
    private List<CondicionFiltroUI> listaCondiciones = new ArrayList<>();

    public List<CondicionFiltroUI> getListaCondiciones() {
        return listaCondiciones;
    }

    public void setListaCondiciones(List<CondicionFiltroUI> listaCondiciones) {
        this.listaCondiciones = listaCondiciones;
    }

    public Map<String, CondicionFiltroUI> getMapaCondiciones() {
        return mapaCondiciones;
    }

    public void setMapaCondiciones(Map<String, CondicionFiltroUI> mapa) {
        this.mapaCondiciones = mapa;
    }
    
    public void regenerar() {
        listaCondiciones = mapaCondiciones.values().stream().sorted((o1, o2) -> {
            return o1.getPosicion() - o2.getPosicion();
        }).collect(Collectors.toList());
        mapaCondiciones.clear();
        for (int i = 0; i < listaCondiciones.size(); i++) {
            CondicionFiltroUI condicion = listaCondiciones.get(i);
            String indice = Integer.toString(i);
            condicion.setIndice(indice);
            mapaCondiciones.put(indice, condicion);
        }        
    }
    
    @Override
    public String getDescripcion() {    
        StringBuilder descripcion = new StringBuilder();
        for (CondicionFiltroUI condicion : listaCondiciones) {
            if (condicion == null) {
                continue;
            }
            if (descripcion.length() > 0) {
                descripcion.append(", ");
            }
            descripcion.append(condicion.getDescripcion());
        }
        return descripcion.toString();
    }

    @Override
    public String generarSql(FormateadorSql formateador, ConsultaGes consulta) {
        StringBuilder sql = new StringBuilder();
        for (CondicionFiltroUI condicion : mapaCondiciones.values()) {
            String condicionSql = condicion.generarSql(formateador, consulta);
            if (!Conversion.isBlank(condicionSql)) {
                if (!sql.toString().isEmpty()) {
                      sql.append(" AND ");
                } 
                sql.append(condicionSql);
            }
        }
        return sql.toString();
    }
    
    public void agregarCondicion(CampoGes campo) {
        if (campo == null) {
            return;
        }            
        CondicionFiltroUI condicion = new CondicionFiltroUI();
        condicion.setIdCampo(campo.getIdCampo());
        condicion.setTituloCampo(campo.getTitulo());
        String indice = Integer.toString(mapaCondiciones.size());
        condicion.setIndice(indice);
        mapaCondiciones.put(indice, condicion);
        listaCondiciones.add(condicion);
        
    }

    public void borrarCondicion(Integer indice) {
        if (indice == null) {
            return;
        }
        mapaCondiciones.remove(indice.toString());
        regenerar();
    }

    public void borrarTodo() {
        mapaCondiciones.clear();
        listaCondiciones.clear();
    }

    @Override
    public String getMensajeError() {
        return null;
    }
}
