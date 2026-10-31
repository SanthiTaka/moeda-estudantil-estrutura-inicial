
package br.edu.pucminas.moedaestudantil.model.service;

import java.util.List;
import java.util.NoSuchElementException;

import br.edu.pucminas.moedaestudantil.dto.request.VantagemRequest;
import br.edu.pucminas.moedaestudantil.dto.response.VantagemResponse;

import br.edu.pucminas.moedaestudantil.exception.ConflitoCadastroException;

import br.edu.pucminas.moedaestudantil.model.entity.*;
import br.edu.pucminas.moedaestudantil.model.repository.*;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VantagemService {

    private final VantagemRepository vantagens;
    private final UsuarioRepository usuarios;
    private final ResgateRepository resgates;

    public VantagemService(
            VantagemRepository vantagens,
            UsuarioRepository usuarios,
            ResgateRepository resgates) {

        this.vantagens = vantagens;
        this.usuarios = usuarios;
        this.resgates = resgates;
    }

    @Transactional(readOnly = true)
    public List<VantagemResponse> listar() {

        return vantagens.findAllByOrderByDescricaoAsc()
                .stream()
                .map(this::converter)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VantagemResponse> minhas(String login) {

        EmpresaParceira empresa = localizarEmpresa(login);

        return vantagens
                .findByEmpresa_IdOrderByDescricaoAsc(
                    empresa.getId()
                )
                .stream()
                .map(this::converter)
                .toList();
    }

    @Transactional(readOnly = true)
    public VantagemResponse buscar(Long id) {
        return converter(localizar(id));
    }

    @Transactional
    public VantagemResponse criar(
            String login,
            VantagemRequest dados) {

        EmpresaParceira empresa = localizarEmpresa(login);

        Vantagem vantagem = new Vantagem();

        vantagem.setEmpresa(empresa);
        preencher(vantagem, dados);

        return converter(vantagens.save(vantagem));
    }

    @Transactional
    public VantagemResponse atualizar(
            String login,
            Long id,
            VantagemRequest dados) {

        Vantagem vantagem = localizar(id);

        verificarProprietario(vantagem, login);
        verificarSemResgates(id);

        preencher(vantagem, dados);

        return converter(vantagens.save(vantagem));
    }

    @Transactional
    public void excluir(String login, Long id) {

        Vantagem vantagem = localizar(id);

        verificarProprietario(vantagem, login);
        verificarSemResgates(id);

        vantagens.delete(vantagem);
    }

    private Vantagem localizar(Long id) {

        return vantagens.findById(id)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Vantagem nao encontrada."
                    )
                );
    }

    private EmpresaParceira localizarEmpresa(String login) {

        Usuario usuario = usuarios
                .findByLoginIgnoreCase(login)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Usuario nao encontrado."
                    )
                );

        if (!(usuario instanceof EmpresaParceira empresa)) {
            throw new AccessDeniedException(
                "Acesso permitido somente a empresas."
            );
        }

        return empresa;
    }

    private void verificarProprietario(
            Vantagem vantagem,
            String login) {

        EmpresaParceira empresa = localizarEmpresa(login);

        if (!vantagem.getEmpresa().getId()
                .equals(empresa.getId())) {

            throw new AccessDeniedException(
                "Esta vantagem pertence a outra empresa."
            );
        }
    }

    private void verificarSemResgates(Long id) {

        if (resgates.existsByVantagem_Id(id)) {
            throw new ConflitoCadastroException(
                "Esta vantagem ja possui resgates " +
                "e nao pode ser alterada ou excluida."
            );
        }
    }

    private void preencher(
            Vantagem vantagem,
            VantagemRequest dados) {

        vantagem.setDescricao(dados.descricao().trim());
        vantagem.setFoto(dados.foto().trim());
        vantagem.setCustoMoedas(dados.custoMoedas());
    }

    private VantagemResponse converter(Vantagem vantagem) {

        return new VantagemResponse(
            vantagem.getId(),
            vantagem.getDescricao(),
            vantagem.getFoto(),
            vantagem.getCustoMoedas(),
            vantagem.getEmpresa().getId(),
            vantagem.getEmpresa().getNome()
        );
    }
}
