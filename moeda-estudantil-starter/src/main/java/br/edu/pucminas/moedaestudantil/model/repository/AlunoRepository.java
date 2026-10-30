
package br.edu.pucminas.moedaestudantil.model.repository;

import br.edu.pucminas.moedaestudantil.model.entity.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository
        extends JpaRepository<Aluno, Long> {

    boolean existsByCpf(String cpf);
    boolean existsByRg(String rg);
}
