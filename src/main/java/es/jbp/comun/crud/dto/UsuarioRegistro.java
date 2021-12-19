package es.jbp.comun.crud.dto;

/**
 *
 * @author jorge
 */
public class UsuarioRegistro {
    private String nombre;
    private String contrasena;
    private String contrasenaVerificacion;
    private String empresa;
    private String contrasenaEmpresa;

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

    public String getContrasenaVerificacion() {
        return contrasenaVerificacion;
    }

    public void setContrasenaVerificacion(String contrasenaVerificacion) {
        this.contrasenaVerificacion = contrasenaVerificacion;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getContrasenaEmpresa() {
        return contrasenaEmpresa;
    }

    public void setContrasenaEmpresa(String contrasenaEmpresa) {
        this.contrasenaEmpresa = contrasenaEmpresa;
    }

    public boolean verificarContrasena() {
        return contrasena != null && contrasena.equals(contrasenaVerificacion);
    }
}
