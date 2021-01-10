package es.jbp.comun.crud.nosql;

import es.jbp.comun.ges.entidad.ConsultaGes;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Respresenta el estado que tiene una consulta configurada por un usuario
 * @author Jorge Berjano
 */
@Document(collection = "consultas")
public class ConsultaPersonalizada {
    @Id
    private String _id;
    
    private String nombreUsuario;
    private String idConsulta;
    private ConsultaGes consulta;

    public String get_id() {
        return _id;
    }

    public void set_id(String id) {
        this._id = id;
    }
    
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }    

    public void setIdConsulta(String idConsulta) {
        this.idConsulta = idConsulta;
    }

    public String getIdConsulta() {
        return idConsulta;
    }
        
    public ConsultaGes getConsulta() {
        return consulta;
    }

    public void setConsulta(ConsultaGes consulta) {
        this.consulta = consulta;
    }
    
    
    
}
