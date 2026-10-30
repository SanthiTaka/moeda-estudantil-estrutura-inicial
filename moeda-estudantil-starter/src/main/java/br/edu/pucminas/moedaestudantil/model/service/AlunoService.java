
package br.edu.pucminas.moedaestudantil.model.service;

import java.util.List;
import java.util.NoSuchElementException;

import br.edu.pucminas.moedaestudantil.dto.request.AlunoRequest;
import br.edu.pucminas.moedaestudantil.dto.response.AlunoResponse;
import br.edu.pucminas.moedaestudantil.exception.ConflitoCadastroException;
import br.edu.pucminas.moedaestudantil.model.entity.Aluno;
import br.edu.pucminas.moedaestudantil.model.entity.Instituicao;
import br.edu.pucminas.moedaestudantil.model.repository.AlunoRepository;
import br.edu.pucminas.moedaestudantil.model.repository.InstituicaoRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlunoService {

    private final AlunoRepository alunos;
    private final InstituicaoRepository instituicoes;
    private final ValidacaoCadastro validacao;

    public AlunoService(
            AlunoRepository alunos,
            InstituicaoRepository instituicoes,
            ValidacaoCadastro validacao) {

        this.alunos = alunos;
        this.instituicoes = instituicoes;
        this.validacao = validacao;
    }

    @Transactional(readOnly = true)
    public List<AlunoResponse> listar() {
        return alunos.findAll(Sort.by("nome").ascending())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlunoResponse buscar(Long id) {
        return toResponse(achar(id));
    }

    @Transactional
    public AlunoResponse criar(AlunoRequest dados) {
        Aluno aluno = new Aluno();
        aplicar(aluno, dados, true);
        return toResponse(alunos.save(aluno));
    }

    @Transactional
    public AlunoResponse atualizar(Long id, AlunoRequest dados) {
        Aluno aluno = achar(id);
        aplicar(aluno, dados, false);
        return toResponse(alunos.save(aluno));
    }

    @Transactional
    public void excluir(Long id) {
        alunos.delete(achar(id));
    }

    private Aluno achar(Long id) {
        return alunos.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Aluno nao encontrado."));
    }

    private void aplicar(
            Aluno aluno, AlunoRequest d, boolean novo) {

        String email = d.email().trim().toLowerCase();
        String login = d.login().trim().toLowerCase();
        String cpf = d.cpf().trim();
        String rg = d.rg().trim();

        validacao.conferirUnicidade(
                email, login, novo ? null : aluno);

        if ((novo || !aluno.getCpf().equals(cpf))
                && alunos.existsByCpf(cpf)) {
            throw new ConflitoCadastroException(
                    "Este CPF ja esta cadastrado.");
        }

        if ((novo || !aluno.getRg().equalsIgnoreCase(rg))
                && alunos.existsByRg(rg)) {
            throw new ConflitoCadastroException(
                    "Este RG ja esta cadastrado.");
        }

        Instituicao instituicao = instituicoes
                .findById(d.instituicaoId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Instituicao nao encontrada."));

        aluno.setNome(d.nome().trim());
        aluno.setEmail(email);
        aluno.setLogin(login);
        aluno.setCpf(cpf);
        aluno.setRg(rg);
        aluno.setEndereco(d.endereco().trim());
        aluno.setCurso(d.curso().trim());
        aluno.setInstituicao(instituicao);

        validacao.atualizarSenha(
                aluno, d.senha(), novo);
    }

    private AlunoResponse toResponse(Aluno a) {
        return new AlunoResponse(
                a.getId(),
                a.getNome(),
                a.getEmail(),
                a.getLogin(),
                a.getCpf(),
                a.getRg(),
                a.getEndereco(),
                a.getCurso(),
                a.getInstituicao().getId(),
                a.getInstituicao().getNome()
        );
    }
}
