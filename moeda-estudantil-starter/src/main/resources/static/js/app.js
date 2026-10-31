
"use strict";

const $ = id => document.getElementById(id);

const mostrar = (id, visivel) =>
    $(id).classList.toggle("hidden", !visivel);

let sessao = null;
let modoEdicao = false;

// Mensagens
function aviso(mensagem, erro = false) {
    $("feedback").textContent = mensagem;
    $("feedback").classList.toggle("error", erro);
    mostrar("feedback", true);
}

// Protecao CSRF
function tokenCsrf() {
    const item = document.cookie.split("; ")
        .find(c => c.startsWith("XSRF-TOKEN="));

    if (!item) {
        throw new Error(
            "Token de seguranca ausente. Atualize a pagina."
        );
    }

    return decodeURIComponent(
        item.substring("XSRF-TOKEN=".length)
    );
}

async function prepararCsrf() {
    const resposta = await fetch("/api/auth/csrf", {
        credentials: "same-origin",
        cache: "no-store"
    });

    if (!resposta.ok) {
        throw new Error("Falha ao preparar a seguranca.");
    }

    tokenCsrf();
}

// Requisicoes HTTP
async function api(caminho, opcoes = {}) {
    const metodo = (opcoes.method || "GET").toUpperCase();

    const headers = {
        Accept: "application/json",
        ...opcoes.headers
    };

    if (opcoes.body && typeof opcoes.body === "string") {
        headers["Content-Type"] = "application/json";
    }

    if (!["GET", "HEAD", "OPTIONS"].includes(metodo)) {
        headers["X-XSRF-TOKEN"] = tokenCsrf();
    }

    const resposta = await fetch(caminho, {
        ...opcoes,
        method: metodo,
        headers,
        credentials: "same-origin",
        cache: "no-store"
    });

    if (resposta.status === 204) return null;

    const texto = await resposta.text();
    let dados = null;

    try {
        dados = texto ? JSON.parse(texto) : null;
    } catch {
        // Resposta sem JSON
    }

    if (!resposta.ok) {
        const detalhes = dados?.erros
            ? Object.entries(dados.erros)
                .map(([c, v]) => `${c}: ${v}`)
                .join(" | ")
            : dados?.mensagem;

        throw new Error(
            detalhes || `Erro HTTP ${resposta.status}`
        );
    }

    return dados;
}

// Painel do usuario
function mostrarPainel() {
    mostrar("acesso", false);
    mostrar("cadastro", false);
    mostrar("painel", true);

    $("boas-vindas").textContent =
        `Bem-vindo(a), ${sessao.nome}!`;

    $("resumo-perfil").textContent =
        `Login: ${sessao.login} | Perfil: ${sessao.perfil}`;

    mostrar(
        "editar-conta",
        sessao.perfil !== "PROFESSOR"
    );

    mostrar(
        "excluir-conta",
        sessao.perfil !== "PROFESSOR"
    );
}



async function carregarPerfil() {
    sessao = await api("/api/auth/me");

    mostrarPainel();

    await carregarCarteira();

    // Carrega as vantagens sem bloquear o login
    // caso o catalogo apresente algum erro.
    try {
        await carregarVantagens();
    } catch (erro) {
        aviso(
            "Nao foi possivel carregar vantagens: " +
            erro.message,
            true
        );
    }
}



// Login real
async function entrar(login, senha) {
    await prepararCsrf();

    const resposta = await fetch("/api/auth/login", {
        method: "POST",
        credentials: "same-origin",
        headers: {
            "Content-Type":
                "application/x-www-form-urlencoded",
            "X-XSRF-TOKEN": tokenCsrf()
        },
        body: new URLSearchParams({ login, senha })
    });

    if (!resposta.ok) {
        throw new Error("Login ou senha incorretos.");
    }

    // Renova o CSRF apos autenticar
    await prepararCsrf();
    await carregarPerfil();
}

// Logout
async function sair() {
    try {
        await api("/api/auth/logout", {
            method: "POST"
        });

        sessao = null;

        $("form-login").reset();

        mostrar("painel", false);
        mostrar("cadastro", false);
        mostrar("acesso", true);

        await prepararCsrf();

        aviso("Voce saiu da conta.");
    } catch (erro) {
        aviso(erro.message, true);
    }
}

// Instituicoes
async function preencherInstituicoes() {
    const instituicoes = await api("/api/instituicoes");

    const select = $("aluno-instituicao");

    select.replaceChildren(
        new Option("Selecione...", "")
    );

    for (const instituicao of instituicoes) {
        select.add(
            new Option(
                instituicao.nome,
                String(instituicao.id)
            )
        );
    }
}

// Cadastro e edicao
function abrirCadastro(tipo, editar = false) {
    modoEdicao = editar;

    $("form-aluno").reset();
    $("form-empresa").reset();

    mostrar("acesso", false);
    mostrar("painel", false);
    mostrar("cadastro", true);

    mostrar("form-aluno", tipo === "alunos");
    mostrar("form-empresa", tipo === "empresas");

    $("cadastro-titulo").textContent = editar
        ? "Editar meus dados"
        : tipo === "alunos"
            ? "Cadastro de aluno"
            : "Cadastro de empresa";

    const prefixo = tipo === "alunos"
        ? "aluno" : "empresa";

    $(`${prefixo}-senha`).required = !editar;
    $(`${prefixo}-login`).readOnly = editar;

    $("dica-senha").textContent = editar
        ? "Deixe a senha em branco para mante-la."
        : "A senha precisa ter ao menos 8 caracteres.";
}

// Buscar meus dados para edicao
async function editarConta() {
    const tipo = sessao.perfil === "ALUNO"
        ? "alunos" : "empresas";

    try {
        const dados = await api(
            `/api/${tipo}/${sessao.id}`
        );

        abrirCadastro(tipo, true);

        const prefixo = tipo === "alunos"
            ? "aluno" : "empresa";

        const campos = [
            "nome", "email", "login",
            ...(tipo === "alunos"
                ? ["cpf", "rg", "endereco", "curso"]
                : ["cnpj"])
        ];

        for (const campo of campos) {
            $(`${prefixo}-${campo}`).value =
                dados[campo] ?? "";
        }

        if (tipo === "alunos") {
            $("aluno-instituicao").value =
                String(dados.instituicaoId);
        }
    } catch (erro) {
        aviso(erro.message, true);
    }
}

// Cadastrar ou atualizar
async function salvarCadastro(evento, tipo) {
    evento.preventDefault();

    const prefixo = tipo === "alunos"
        ? "aluno" : "empresa";

    const form = $(`form-${prefixo}`);

    const dados = Object.fromEntries(
        new FormData(form).entries()
    );

    const senhaDigitada = dados.senha;

    dados.senha = dados.senha || null;

    if (tipo === "alunos") {
        dados.cpf = dados.cpf.replace(/\D/g, "");
        dados.instituicaoId = Number(dados.instituicaoId);
    } else {
        dados.cnpj = dados.cnpj
            .replace(/[^A-Za-z0-9]/g, "")
            .toUpperCase();
    }

    const caminho = `/api/${tipo}${
        modoEdicao ? `/${sessao.id}` : ""
    }`;

    try {
        await api(caminho, {
            method: modoEdicao ? "PUT" : "POST",
            body: JSON.stringify(dados)
        });

        if (modoEdicao) {
            await carregarPerfil();
            aviso("Cadastro atualizado.");
        } else {
            await entrar(dados.login, senhaDigitada);
            aviso("Conta criada e login realizado!");
        }

        form.reset();
    } catch (erro) {
        aviso(erro.message, true);
    }
}

// Excluir minha conta
async function excluirConta() {
    if (!confirm(
        "Deseja excluir seu cadastro permanentemente?"
    )) return;

    const tipo = sessao.perfil === "ALUNO"
        ? "alunos" : "empresas";

    try {
        await api(`/api/${tipo}/${sessao.id}`, {
            method: "DELETE"
        });

        await sair();
        aviso("Cadastro excluido.");
    } catch (erro) {
        aviso(erro.message, true);
    }
}

// Eventos
$("form-login").addEventListener(
    "submit", async evento => {
        evento.preventDefault();

        try {
            await entrar(
                $("login-usuario").value.trim(),
                $("login-senha").value
            );

            $("login-senha").value = "";

            aviso("Login realizado!");
        } catch (erro) {
            aviso(erro.message, true);
        }
    }
);

$("abrir-aluno").addEventListener(
    "click", () => abrirCadastro("alunos")
);

$("abrir-empresa").addEventListener(
    "click", () => abrirCadastro("empresas")
);

$("voltar").addEventListener("click", () => {
    if (sessao) {
        mostrarPainel();
    } else {
        mostrar("cadastro", false);
        mostrar("acesso", true);
    }
});

$("form-aluno").addEventListener(
    "submit", e => salvarCadastro(e, "alunos")
);

$("form-empresa").addEventListener(
    "submit", e => salvarCadastro(e, "empresas")
);

$("editar-conta").addEventListener(
    "click", editarConta
);

$("excluir-conta").addEventListener(
    "click", excluirConta
);

$("sair").addEventListener("click", sair);

// Inicializacao
(async () => {
    try {
        await prepararCsrf();
        await preencherInstituicoes();

        try {
            await carregarPerfil();
        } catch {
            mostrar("acesso", true);
        }
    } catch (erro) {
        aviso(erro.message, true);
    }
})();


// CARTEIRA E EXTRATO

async function carregarCarteira() {

    const possuiConta =
        sessao && ["ALUNO", "PROFESSOR"]
            .includes(sessao.perfil);

    mostrar("area-moedas", possuiConta);

    if (!possuiConta) return;

    mostrar(
        "area-envio",
        sessao.perfil === "PROFESSOR"
    );

    const extrato = await api("/api/extratos/me");

    $("saldo-moedas").textContent =
        `${extrato.saldo} moedas`;

    const tbody = $("lista-transacoes");
    tbody.replaceChildren();

    for (const t of extrato.transacoes) {

        const linha = document.createElement("tr");

        const data = document.createElement("td");
        data.textContent =
            new Date(t.data).toLocaleString("pt-BR");

        const tipo = document.createElement("td");
        tipo.textContent = t.tipo.replaceAll("_", " ");

        const valor = document.createElement("td");
        valor.textContent =
            `${t.valor > 0 ? "+" : ""}${t.valor}`;

        const motivo = document.createElement("td");
        motivo.textContent = t.motivo || "-";

        linha.append(data, tipo, valor, motivo);
        tbody.appendChild(linha);
    }

    if (extrato.transacoes.length === 0) {
        const linha = document.createElement("tr");
        const celula = document.createElement("td");

        celula.colSpan = 4;
        celula.textContent =
            "Nenhuma movimentação registrada.";

        linha.appendChild(celula);
        tbody.appendChild(linha);
    }

    if (sessao.perfil === "PROFESSOR") {
        await carregarAlunosDestino();
    }
}

// Alunos disponiveis para recebimento

async function carregarAlunosDestino() {

    const alunos = await api("/api/moedas/alunos");

    const select = $("destino-aluno");

    select.replaceChildren(
        new Option("Selecione um aluno...", "")
    );

    for (const aluno of alunos) {
        select.add(
            new Option(
                aluno.nome,
                String(aluno.id)
            )
        );
    }
}

// Envio de moedas

async function enviarMoedas(evento) {
    evento.preventDefault();

    const alunoId = Number(
        $("destino-aluno").value
    );

    const quantidade = Number(
        $("quantidade-moedas").value
    );

    const motivo = $("motivo-moedas").value.trim();

    if (!alunoId ||
        !Number.isSafeInteger(quantidade) ||
        quantidade <= 0 ||
        !motivo) {

        aviso("Preencha os campos corretamente.", true);
        return;
    }

    if (!confirm(
        `Confirma o envio de ${quantidade} moedas?`
    )) {
        return;
    }

    const botao = document.querySelector(
        "#form-envio button[type='submit']"
    );

    botao.disabled = true;

    try {
        await api("/api/moedas/enviar", {
            method: "POST",
            body: JSON.stringify({
                alunoId,
                quantidade,
                motivo
            })
        });

        $("form-envio").reset();

        await carregarCarteira();

        aviso("Moedas enviadas com sucesso!");
    } catch (erro) {
        aviso(erro.message, true);
    } finally {
        botao.disabled = false;
    }
}

// Eventos

$("form-envio").addEventListener(
    "submit", enviarMoedas
);

$("atualizar-extrato").addEventListener(
    "click", async () => {
        try {
            await carregarCarteira();
            aviso("Extrato atualizado.");
        } catch (erro) {
            aviso(erro.message, true);
        }
    }
);


// =====================================
// CATALOGO DE VANTAGENS
// =====================================

let vantagemEmEdicao = null;

async function carregarVantagens() {

    const perfilPermitido =
        sessao && ["ALUNO", "EMPRESA"]
            .includes(sessao.perfil);

    mostrar("area-vantagens", perfilPermitido);

    if (!perfilPermitido) return;

    const empresa = sessao.perfil === "EMPRESA";

    mostrar("gerenciar-vantagens", empresa);

    $("titulo-vantagens").textContent = empresa
        ? "Minhas vantagens"
        : "Vantagens disponíveis";

    const url = empresa
        ? "/api/vantagens/minhas"
        : "/api/vantagens";

    const vantagens = await api(url);

    const lista = $("lista-vantagens");

    lista.replaceChildren();

    if (vantagens.length === 0) {
        const mensagem = document.createElement("p");

        mensagem.textContent = empresa
            ? "Você ainda não cadastrou vantagens."
            : "Nenhuma vantagem disponível no momento.";

        lista.appendChild(mensagem);
        return;
    }

    for (const vantagem of vantagens) {

        const card = document.createElement("article");
        card.className = "vantagem-card";

        const foto = document.createElement("img");

        foto.src = vantagem.foto;
        foto.alt = vantagem.descricao;
        foto.loading = "lazy";
        foto.referrerPolicy = "no-referrer";
        foto.className = "vantagem-imagem";

        const descricao = document.createElement("h3");
        descricao.textContent = vantagem.descricao;

        const nomeEmpresa = document.createElement("p");
        nomeEmpresa.textContent =
            "Empresa: " + vantagem.empresaNome;

        const custo = document.createElement("strong");
        custo.className = "vantagem-preco";
        custo.textContent =
            vantagem.custoMoedas + " moedas";

        card.append(foto, descricao, nomeEmpresa, custo);

        if (empresa) {

            const acoes = document.createElement("div");
            acoes.className = "form-actions";

            const editar = document.createElement("button");
            editar.type = "button";
            editar.className = "btn outline";
            editar.textContent = "Editar";

            editar.addEventListener("click", () => {
                editarVantagem(vantagem);
            });

            const excluir = document.createElement("button");
            excluir.type = "button";
            excluir.className = "btn danger";
            excluir.textContent = "Excluir";

            excluir.addEventListener("click", async () => {

                if (!confirm(
                    "Deseja excluir esta vantagem?"
                )) return;

                try {
                    await api(
                        `/api/vantagens/${vantagem.id}`,
                        { method: "DELETE" }
                    );

                    limparFormularioVantagem();

                    await carregarVantagens();

                    aviso("Vantagem excluída.");
                } catch (erro) {
                    aviso(erro.message, true);
                }
            });

            acoes.append(editar, excluir);
            card.appendChild(acoes);
        }

        lista.appendChild(card);
    }
}

// Preenche o formulario para edicao

function editarVantagem(vantagem) {

    vantagemEmEdicao = vantagem.id;

    $("vantagem-descricao").value = vantagem.descricao;
    $("vantagem-custo").value = vantagem.custoMoedas;
    $("vantagem-foto").value = vantagem.foto;

    $("salvar-vantagem").textContent =
        "Salvar alterações";

    mostrar("cancelar-edicao-vantagem", true);

    $("form-vantagem").scrollIntoView({
        behavior: "smooth",
        block: "center"
    });
}

// Limpa o formulario

function limparFormularioVantagem() {

    vantagemEmEdicao = null;

    $("form-vantagem").reset();

    $("salvar-vantagem").textContent =
        "Cadastrar vantagem";

    mostrar("cancelar-edicao-vantagem", false);
}

// Cadastra ou atualiza

async function salvarVantagem(evento) {

    evento.preventDefault();

    if (!sessao || sessao.perfil !== "EMPRESA") {
        aviso("Acesso não permitido.", true);
        return;
    }

    const dados = {
        descricao: $("vantagem-descricao").value.trim(),
        foto: $("vantagem-foto").value.trim(),
        custoMoedas: Number(
            $("vantagem-custo").value
        )
    };

    if (!Number.isSafeInteger(dados.custoMoedas)
            || dados.custoMoedas < 1) {

        aviso("Informe um custo válido.", true);
        return;
    }

    const editando = vantagemEmEdicao !== null;

    const url = editando
        ? `/api/vantagens/${vantagemEmEdicao}`
        : "/api/vantagens";

    const botao = $("salvar-vantagem");

    botao.disabled = true;

    try {

        await api(url, {
            method: editando ? "PUT" : "POST",
            body: JSON.stringify(dados)
        });

        limparFormularioVantagem();

        await carregarVantagens();

        aviso(
            editando
                ? "Vantagem atualizada com sucesso!"
                : "Vantagem cadastrada com sucesso!"
        );

    } catch (erro) {
        aviso(erro.message, true);
    } finally {
        botao.disabled = false;
    }
}

// Eventos do formulario

$("form-vantagem").addEventListener(
    "submit", salvarVantagem
);

$("cancelar-edicao-vantagem").addEventListener(
    "click", limparFormularioVantagem
);

