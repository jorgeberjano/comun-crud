package es.jbp.comun.crud;

import es.jbp.comun.crud.nosql.ConsultaPersonalizada;
import es.jbp.comun.crud.servicio.FactoriaServicio;
import es.jbp.comun.crud.servicio.ServicioElemento;
import es.jbp.comun.ges.entidad.ConsultaGes;
import es.jbp.comun.ges.entidad.Ges;
import es.jbp.comun.ges.utilidades.GestorSimbolos;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import es.jbp.comun.utiles.sql.GestorConexiones;
import es.jbp.comun.utiles.sql.PoolConexiones;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import es.jbp.comun.crud.nosql.RepositorioConsultaPersonalizada;
import es.jbp.comun.ges.serializacion.SerializadorGes;
import es.jbp.comun.ges.serializacion.SerializadorGesJson;
import es.jbp.comun.ges.serializacion.SerializadorGesXml;
import es.jbp.comun.utiles.conversion.Conversion;
import es.jbp.comun.utiles.depuracion.GestorLog;
import es.jbp.comun.utiles.sql.GestorConexionesEfimeras;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase para el control del CRUD
 *
 * @author jberjano
 */
@Component
@Scope("session")
public class Crud {

    @Autowired(required=false)
    private RepositorioConsultaPersonalizada repositorioConsultaPersonalizada;

    private GestorConexiones gestorConexiones;

    private Ges gestor;

    private Map<String, FactoriaServicio> mapaFactoriasServicios = new HashMap<>();

    private IUsuario usuario;
    
    public static String bdDriver;
    public static String bdConexion;
    public static String bdUsuario;
    public static String bdClave;
    public static String gesArchivo;    
    public static Map<String, Object> mapaSimbolosEstaticos = new  HashMap<>();

    public Crud() {
        GestorLog.traza("Constructor de crud");        
        
        if ("jstels.jdbc.xml.XMLDriver2".equals(bdDriver)) {
            gestorConexiones = new GestorConexionesEfimeras(bdDriver, bdConexion, bdUsuario, bdClave, true);
        } else {
            gestorConexiones = new PoolConexiones(bdDriver, bdConexion, bdUsuario, bdClave, true);
        }

        try {
            gestorConexiones.inicializar();
        } catch (Throwable ex) {
            GestorLog.error("No se ha podido inicializar el gestor de conexiones", ex);
        }

        gestor = new Ges();
        gestor.definirSimbolos(mapaSimbolosEstaticos);
                
        if (Conversion.isBlank(gesArchivo)) {
            GestorLog.error("No se ha definido el archivo ges", null);
            return;
        } 
        SerializadorGes serializador;
        if (gesArchivo.endsWith(".xml") || gesArchivo.endsWith(".xml.ges")) {
            serializador = new SerializadorGesXml(mapaSimbolosEstaticos);
        } else {
            serializador = new SerializadorGesJson(mapaSimbolosEstaticos);
        }        
        try {
            serializador.deserializarArchivo(gesArchivo);
        } catch (Exception ex) {
            GestorLog.error("No se ha podido deserializar el archivo " + gesArchivo, ex);
        }
    }

    public IUsuario getUsuario() {
        return usuario;
    }

    public void setUsuario(IUsuario usuario) {
        this.usuario = usuario;
    }

    public List<ConsultaGes> getConsultas() {
        return gestor.getConsultasPantalla();
    }

    public List<ConsultaGes> getConsultasMenu() {
        return gestor.getConsultasPantalla().stream().filter(consulta -> !consulta.isOcultaEnMenu()).collect(Collectors.toList());
    }

    public ConsultaGes getConsultaPorId(String idConsulta) {
        return gestor.getConsultaPorId(idConsulta);
    }

    public GestorConexiones getGestorConexiones() {
        return gestorConexiones;
    }

    public GestorSimbolos getGestorSimbolos() {
        return gestor.getGestorSimbolos();
    }

    public ServicioElemento getServicio(String idConsulta) {

        FactoriaServicio factoria = mapaFactoriasServicios.get(idConsulta);

        if (factoria != null) {
            return factoria.crearServicio(idConsulta, this);
        } else {
            return new ServicioElemento(idConsulta, this);
        }
    }

    public final void registrarServicio(String idConsulta, FactoriaServicio factoriaServicio) {
        if (factoriaServicio != null) {
            mapaFactoriasServicios.put(idConsulta, factoriaServicio);
        }
    }

    public ConsultaGes getConsulta(String idConsulta) {
        return gestor.getConsultaPorId(idConsulta);
    }

    public void definirSimbolo(String nombre, String valor) {
        gestor.definirSimbolo(nombre, valor);
    }

    public void guardarConsultaPersonalizada(ConsultaGes consulta) {
        if (usuario == null || repositorioConsultaPersonalizada == null) {
            return;
        }
        ConsultaPersonalizada consultaPersonalizada
                = repositorioConsultaPersonalizada.findByIdConsultaAndNombreUsuario(
                        consulta.getIdConsulta(), usuario.getNombre());
        if (consultaPersonalizada == null) {
            consultaPersonalizada = new ConsultaPersonalizada();
        }
        consultaPersonalizada.setIdConsulta(consulta.getIdConsulta());
        consultaPersonalizada.setNombreUsuario(usuario.getNombre());
        consultaPersonalizada.setConsulta(consulta);
        repositorioConsultaPersonalizada.save(consultaPersonalizada);
    }

    public ConsultaGes getConsultaPersonalizadaPorId(String idConsulta) {
        if (usuario == null || repositorioConsultaPersonalizada == null) {
            return null;
        }
        ConsultaPersonalizada consultaPersonalizada
                = repositorioConsultaPersonalizada.findByIdConsultaAndNombreUsuario(idConsulta, usuario.getNombre());
        if (consultaPersonalizada == null) {
            return null;
        }
        return consultaPersonalizada.getConsulta();
    }
}
