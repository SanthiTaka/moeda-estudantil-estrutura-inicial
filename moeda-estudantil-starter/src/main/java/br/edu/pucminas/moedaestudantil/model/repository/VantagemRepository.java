
package br.edu.pucminas.moedaestudantil.model.repository;

import java.util.List;

import br.edu.pucminas.moedaestudantil.model.entity.Vantagem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VantagemRepository
        extends JpaRepository<Vantagem, Long> {

    List<Vantagem> findAllByOrderByDescricaoAsc();

    List<Vantagem> findByEmpresa_IdOrderByDescricaoAsc(
        Long empresaId
    );
}
