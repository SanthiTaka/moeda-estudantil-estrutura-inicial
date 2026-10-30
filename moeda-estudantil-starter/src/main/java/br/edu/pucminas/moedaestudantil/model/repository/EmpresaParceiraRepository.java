
package br.edu.pucminas.moedaestudantil.model.repository;

import br.edu.pucminas.moedaestudantil.model.entity.EmpresaParceira;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaParceiraRepository
        extends JpaRepository<EmpresaParceira, Long> {

    boolean existsByCnpj(String cnpj);
}
