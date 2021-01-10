package es.jbp.comun.crud.nosql;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repositorio para los ojetos de consultas personalizadas
 * @author jorge
 */
public interface RepositorioConsultaPersonalizada extends MongoRepository<ConsultaPersonalizada, String> {

    ConsultaPersonalizada findByIdConsultaAndNombreUsuario(String idConsulta, String nombreUsuario);

    @Override
    ConsultaPersonalizada save(ConsultaPersonalizada consultaPersonalizada);    
}
