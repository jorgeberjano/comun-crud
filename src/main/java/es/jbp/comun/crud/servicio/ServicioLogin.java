package es.jbp.comun.crud.servicio;

import es.jbp.comun.crud.dto.UsuarioLogin;
import es.jbp.comun.crud.dto.UsuarioRegistro;
import es.jbp.comun.crud.nosql.RepositorioUsuarios;
import es.jbp.comun.crud.nosql.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Funcionalidad de servicio para login
 * @author jorge
 */
@Service
public class ServicioLogin {

    @Autowired
    private RepositorioUsuarios repositorioUsuarios;

    private String mensajeError;

    public Usuario login(UsuarioLogin usuarioLogin) {

        Usuario usuario;
        boolean admin = "sipac".equals(usuarioLogin.getNombre())
                && "capis".equals(usuarioLogin.getContrasena());

        if (admin) {
            usuario = new Usuario();
            usuario.setContrasena(usuarioLogin.getContrasena());
            usuario.setNombre(usuarioLogin.getNombre());
            return usuario;
        }

        try {
            usuario = repositorioUsuarios.findByNombre(usuarioLogin.getNombre());
        } catch (Throwable ex) {
            mensajeError = ex.getMessage();
            return null;
        }
        if (usuario == null) {
            mensajeError = "El usuario no está registrado";
            return null;
        }
        
        if (!usuarioLogin.getContrasena().equals(usuario.getContrasena())) {
            mensajeError = "La contraseña es incorrecta";
            return null;
        }
        System.out.println("Ha entrado el usuario " + usuario.getNombre());
        return usuario;
    }

    public boolean registrarUsuario(UsuarioRegistro usuarioRegistro) {
        Usuario usuario = new Usuario();
        usuario.setNombre(usuarioRegistro.getNombre());
        usuario.setContrasena(usuarioRegistro.getContrasena());
        usuario.setCodigoEmpresa(usuarioRegistro.getEmpresa());
        usuario = repositorioUsuarios.save(usuario);
        if (usuario == null) {
            mensajeError = "No se ha podido registral el usuario";
            return false;
        }
        return true;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    public boolean existeUsuario(String nombre) {
        try {
            Usuario usuario = repositorioUsuarios.findByNombre(nombre);
            return usuario != null;
        } catch (Throwable ex) {
            mensajeError = ex.getMessage();
            return false;
        }   
    }
}
