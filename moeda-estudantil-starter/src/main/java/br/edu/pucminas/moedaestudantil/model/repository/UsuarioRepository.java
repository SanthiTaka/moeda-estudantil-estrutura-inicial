package br.edu.pucminas.moedaestudantil.model.repository;

import br.edu.pucminas.moedaestudantil.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
