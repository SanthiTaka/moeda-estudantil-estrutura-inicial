
package br.edu.pucminas.moedaestudantil.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.pucminas.moedaestudantil.model.entity.Usuario;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLoginIgnoreCase(String login);

    boolean existsByEmail(String email);

    boolean existsByLogin(String login);
}
