package es.jbp.comun.crud.filtro;

import es.jbp.comun.ges.entidad.CampoGes;
import es.jbp.comun.ges.entidad.ConsultaGes;
import es.jbp.comun.ges.utilidades.ConversionValores;
import es.jbp.comun.utiles.conversion.Conversion;
import es.jbp.comun.utiles.sql.PlantillaSql;
import es.jbp.comun.utiles.sql.compatibilidad.FormateadorSql;
import java.io.Serializable;

/**
 * Representa una condición del filtro en la capa de vista.
 * @author jorge
 */
public class CondicionFiltroUI implements Serializable {
    private String indice;
    private String tituloCampo;
    private String idCampo;
    private String operador;
    private String valor;

    public CondicionFiltroUI() {        
    }

    public Integer getPosicion() {        
        Integer posicion = Conversion.toInteger(indice);
        return posicion == null ? 0 : posicion;
    }

    public String getIndice() {
        return indice;
    }

    public void setIndice(String indice) {
        this.indice = indice;
    }
    
    public String getIdCampo() {
        return idCampo;
    }

    public void setIdCampo(String nombreCampo) {
        this.idCampo = nombreCampo;
    }
    
    public String getTituloCampo() {
        return tituloCampo;
    }

    public void setTituloCampo(String tituloCampo) {
        this.tituloCampo = tituloCampo;
    }

    public String getOperador() {
        return operador;
    }

    public void setOperador(String operador) {
        this.operador = operador;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
    
    public String getDescripcion() {
        if (Conversion.isBlank(valor)) {
            return "";
        }
        String tituloOperador = OperadoresFiltro.getDescripcion(operador);
        return new StringBuilder()
                .append(tituloCampo)
                .append(" ")
                .append(tituloOperador)
                .append(" ")
                .append(valor)
       .toString();
    }

    public String generarSql(FormateadorSql formateador, ConsultaGes consulta) {
        CampoGes campo = consulta.getCampoPorId(idCampo);
        if (campo == null) {
            return null;
        }
        PlantillaSql plantilla = new PlantillaSql(operador, formateador);
        plantilla.definirParametro("campo", campo.getNombreCompletoCampo());
        //Object valorObj = campo.convertirValor(valor);
        Object valorObj = ConversionValores.aValorBD(valor, campo);
        if (valorObj == null) {
            return null;
        }
        plantilla.definirParametro("valor", valorObj);
        plantilla.definirParametro("contieneValor", formateador.getContieneTexto(valor));
        
        return plantilla.getResultado();
    }
}
