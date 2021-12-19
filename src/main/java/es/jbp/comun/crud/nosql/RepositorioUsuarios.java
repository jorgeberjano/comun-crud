package es.jbp.comun.crud.nosql;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repositorio de usuarios
 * @author Jorge Berjano
 */
public interface RepositorioUsuarios extends MongoRepository<Usuario, String> {
    Usuario findByNombre(String nombre);
}
