
package br.edu.pucminas.moedaestudantil.model.service;

import br.edu.pucminas.moedaestudantil.model.entity.*;
import br.edu.pucminas.moedaestudantil.model.repository.ContaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContaService {

    private final ContaRepository contas;

    public ContaService(ContaRepository contas) {
        this.contas = contas;
    }

    @Transactional
    public Conta obterOuCriar(Usuario usuario) {

        if (!(usuario instanceof Aluno)
                && !(usuario instanceof Professor)) {
            throw new IllegalArgumentException(
                "Este perfil nao possui conta de moedas."
            );
        }

        return contas.findByUsuario_Id(usuario.getId())
            .orElseGet(() -> {

                Conta conta = new Conta();
                conta.setUsuario(usuario);
                conta.setSaldo(0);

                return contas.saveAndFlush(conta);
            });
    }
}
