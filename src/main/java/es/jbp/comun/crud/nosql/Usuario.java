package es.jbp.comun.crud.nosql;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento con los datos del usuario de la aplicación.
 * @author Jorge Berjano
 */
@Document(collection = "usuarios")
public class Usuario {
    @Id
    private String _id;
    
    private String nombre;
    private String contrasena;
    private String empresa;
    private String email;

    public String get_id() {
        return _id;
    }

    public void set_id(String id) {
        this._id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getCodigoEmpresa() {
        return empresa;
    }

    public void setCodigoEmpresa(String codigoEmpresa) {
        this.empresa = codigoEmpresa;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
