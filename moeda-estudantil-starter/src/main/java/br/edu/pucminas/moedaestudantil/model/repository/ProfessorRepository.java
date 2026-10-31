
package br.edu.pucminas.moedaestudantil.model.repository;

import br.edu.pucminas.moedaestudantil.model.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository
        extends JpaRepository<Professor, Long> {

    boolean existsByCpf(String cpf);
}
