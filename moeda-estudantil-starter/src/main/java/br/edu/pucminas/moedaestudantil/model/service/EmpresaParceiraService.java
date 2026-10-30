
package br.edu.pucminas.moedaestudantil.model.service;

import java.util.List;
import java.util.NoSuchElementException;

import br.edu.pucminas.moedaestudantil.dto.request.EmpresaParceiraRequest;
import br.edu.pucminas.moedaestudantil.dto.response.EmpresaParceiraResponse;
import br.edu.pucminas.moedaestudantil.exception.ConflitoCadastroException;
import br.edu.pucminas.moedaestudantil.model.entity.EmpresaParceira;
import br.edu.pucminas.moedaestudantil.model.repository.EmpresaParceiraRepository;
import br.edu.pucminas.moedaestudantil.model.validation.DocumentoValidator;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaParceiraService {

    private final EmpresaParceiraRepository empresas;
    private final ValidacaoCadastro validacao;

    public EmpresaParceiraService(
            EmpresaParceiraRepository empresas,
            ValidacaoCadastro validacao) {

        this.empresas = empresas;
        this.validacao = validacao;
    }

    @Transactional(readOnly = true)
    public List<EmpresaParceiraResponse> listar() {
        return empresas.findAll(Sort.by("nome").ascending())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmpresaParceiraResponse buscar(Long id) {
        return toResponse(achar(id));
    }

    @Transactional
    public EmpresaParceiraResponse criar(
            EmpresaParceiraRequest dados) {

        EmpresaParceira empresa = new EmpresaParceira();
        aplicar(empresa, dados, true);

        return toResponse(empresas.save(empresa));
    }

    @Transactional
    public EmpresaParceiraResponse atualizar(
            Long id, EmpresaParceiraRequest dados) {

        EmpresaParceira empresa = achar(id);
        aplicar(empresa, dados, false);

        return toResponse(empresas.save(empresa));
    }

    @Transactional
    public void excluir(Long id) {
        empresas.delete(achar(id));
    }

    private EmpresaParceira achar(Long id) {
        return empresas.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Empresa nao encontrada."));
    }

    private void aplicar(
            EmpresaParceira empresa,
            EmpresaParceiraRequest d,
            boolean novo) {

        String email = d.email().trim().toLowerCase();
        String login = d.login().trim().toLowerCase();
        String cnpj = d.cnpj().trim().toUpperCase();

if (!DocumentoValidator.cnpjValido(cnpj)) {
    throw new IllegalArgumentException(
        "CNPJ invalido. Verifique os caracteres informados."
    );
}

        validacao.conferirUnicidade(
                email, login, novo ? null : empresa);

        if ((novo || !empresa.getCnpj().equals(cnpj))
                && empresas.existsByCnpj(cnpj)) {
            throw new ConflitoCadastroException(
                    "Este CNPJ ja esta cadastrado.");
        }

        empresa.setNome(d.nome().trim());
        empresa.setEmail(email);
        empresa.setLogin(login);
        empresa.setCnpj(cnpj);

        validacao.atualizarSenha(
                empresa, d.senha(), novo);
    }

    private EmpresaParceiraResponse toResponse(
            EmpresaParceira e) {

        return new EmpresaParceiraResponse(
                e.getId(),
                e.getNome(),
                e.getEmail(),
                e.getLogin(),
                e.getCnpj()
        );
    }
}
