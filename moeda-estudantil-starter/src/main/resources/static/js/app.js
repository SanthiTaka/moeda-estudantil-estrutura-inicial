
"use strict";

let credenciais = null;
let alunos = [];
let empresas = [];
let instituicoes = [];

const $ = id => document.getElementById(id);

function mostrar(elemento, visivel) {
    elemento.classList.toggle("hidden", !visivel);
}

function aviso(mensagem, erro = false) {
    const box = $("feedback");
    box.textContent = mensagem;
    box.classList.toggle("error", erro);
    mostrar(box, true);

    setTimeout(() => mostrar(box, false), 5000);
}

// Autenticacao temporaria para desenvolvimento
function headerBasic(usuario, senha) {
    const bytes = new TextEncoder()
        .encode(`${usuario}:${senha}`);

    let binario = "";
    bytes.forEach(byte => {
        binario += String.fromCharCode(byte);
    });

    return `Basic ${btoa(binario)}`;
}

// Comunicacao com a API
async function api(caminho, opcoes = {}) {
    const headers = {
        Accept: "application/json",
        Authorization: credenciais,
        ...(opcoes.headers || {})
    };

    if (opcoes.body) {
        headers["Content-Type"] = "application/json";
    }

    let resposta;

    try {
        resposta = await fetch(caminho, {
            ...opcoes,
            headers,
            cache: "no-store"
        });
    } catch {
        throw new Error(
            "Não foi possível conectar ao Spring Boot."
        );
    }

    if (resposta.status === 204) {
        return null;
    }

    const texto = await resposta.text();
    let dados = null;

    try {
        dados = texto ? JSON.parse(texto) : null;
    } catch {
        // Resposta sem JSON
    }

    if (!resposta.ok) {
        if (resposta.status === 401) {
            throw new Error("Usuário ou senha inválidos.");
        }

        const erros = dados?.erros
            ? Object.entries(dados.erros)
                .map(([campo, msg]) => `${campo}: ${msg}`)
                .join(" | ")
            : "";

        throw new Error(
            erros || dados?.mensagem ||
            `Erro HTTP ${resposta.status}`
        );
    }

    return dados;
}

// Login temporario
async function conectar(evento) {
    evento.preventDefault();

    credenciais = headerBasic(
        $("demo-user").value.trim(),
        $("demo-pass").value
    );

    try {
        instituicoes = await api("/api/instituicoes");

        await recarregar();
        preencherInstituicoes();

        $("demo-pass").value = "";
        mostrar($("acesso"), false);
        mostrar($("app"), true);

        aviso("Conectado com sucesso.");
    } catch (erro) {
        credenciais = null;
        aviso(erro.message, true);
    }
}

function desconectar() {
    credenciais = null;
    alunos = [];
    empresas = [];
    instituicoes = [];

    mostrar($("app"), false);
    mostrar($("acesso"), true);
}

// Carregar dados do banco
async function recarregar() {
    [alunos, empresas] = await Promise.all([
        api("/api/alunos"),
        api("/api/empresas")
    ]);

    $("total-alunos").textContent = alunos.length;
    $("total-empresas").textContent = empresas.length;

    desenharLista("alunos", alunos);
    desenharLista("empresas", empresas);
}

// Criar celulas da tabela
function celula(linha, valor) {
    const td = document.createElement("td");
    td.textContent = valor ?? "-";
    linha.appendChild(td);
}

// Mostrar os registros cadastrados
function desenharLista(tipo, dados) {
    const tbody = $(`lista-${tipo}`);
    tbody.replaceChildren();

    mostrar($(`vazio-${tipo}`), dados.length === 0);

    for (const item of dados) {
        const tr = document.createElement("tr");

        celula(tr, item.nome);
        celula(tr, item.email);

        celula(
            tr,
            tipo === "alunos" ? item.cpf : item.cnpj
        );

        if (tipo === "alunos") {
            celula(tr, item.instituicaoNome);
        }

        const td = document.createElement("td");

        const editar = document.createElement("button");
        editar.textContent = "Editar";
        editar.className = "secondary";
        editar.onclick = () => abrirFormulario(tipo, item);

        const excluir = document.createElement("button");
        excluir.textContent = "Excluir";
        excluir.className = "danger";
        excluir.onclick = () => excluirRegistro(tipo, item);

        td.append(editar, excluir);
        tr.appendChild(td);
        tbody.appendChild(tr);
    }
}

// Listagem de instituicoes
function preencherInstituicoes() {
    const select = $("aluno-instituicao");

    select.replaceChildren(
        new Option("Selecione uma instituição", "")
    );

    for (const instituicao of instituicoes) {
        select.add(new Option(
            instituicao.nome,
            String(instituicao.id)
        ));
    }
}

// Abrir formulario de cadastro ou edicao
function abrirFormulario(tipo, item = null) {
    const aluno = tipo === "alunos";
    const prefixo = aluno ? "aluno" : "empresa";
    const form = $(`form-${prefixo}`);

    form.reset();

    $(`${prefixo}-id`).value = item?.id ?? "";
    $(`${prefixo}-senha`).required = !item;

    $(`titulo-${prefixo}`).textContent =
        item ? "Editar Cadastro" : "Novo Cadastro";

    form.querySelector(".password-hint").textContent =
        item
            ? "Deixe em branco para manter a senha."
            : "Mínimo de 8 caracteres.";

    if (item) {
        const campos = [
            "nome", "email", "login",
            ...(aluno
                ? ["cpf", "rg", "curso", "endereco"]
                : ["cnpj"])
        ];

        for (const campo of campos) {
            $(`${prefixo}-${campo}`).value =
                item[campo] || "";
        }

        if (aluno) {
            $("aluno-instituicao").value =
                String(item.instituicaoId);
        }
    }

    mostrar(form, true);
    form.scrollIntoView({ behavior: "smooth" });
}

function fecharFormulario(tipo) {
    const prefixo = tipo === "alunos"
        ? "aluno"
        : "empresa";

    const form = $(`form-${prefixo}`);

    form.reset();
    mostrar(form, false);
}

// CREATE e UPDATE
async function salvar(tipo, evento) {
    evento.preventDefault();

    const aluno = tipo === "alunos";
    const prefixo = aluno ? "aluno" : "empresa";

    const form = $(`form-${prefixo}`);
    const id = $(`${prefixo}-id`).value;

    const dados = Object.fromEntries(
        new FormData(form).entries()
    );

    delete dados.id;

    dados.senha = dados.senha?.trim()
        ? dados.senha
        : null;

    if (aluno) {
        dados.instituicaoId = Number(dados.instituicaoId);
        dados.cpf = dados.cpf.replace(/\D/g, "");
    } else {
        dados.cnpj = dados.cnpj.replace(/\D/g, "");
    }

    try {
        await api(
            `/api/${tipo}${id ? `/${id}` : ""}`,
            {
                method: id ? "PUT" : "POST",
                body: JSON.stringify(dados)
            }
        );

        fecharFormulario(tipo);
        await recarregar();

        aviso("Cadastro salvo com sucesso!");
    } catch (erro) {
        aviso(erro.message, true);
    }
}

// DELETE
async function excluirRegistro(tipo, item) {
    if (!confirm(`Deseja excluir ${item.nome}?`)) {
        return;
    }

    try {
        await api(`/api/${tipo}/${item.id}`, {
            method: "DELETE"
        });

        await recarregar();
        aviso("Registro excluído com sucesso!");
    } catch (erro) {
        aviso(erro.message, true);
    }
}

// Alternar entre alunos e empresas
function trocarAba(tipo) {
    for (const tab of document.querySelectorAll(".tab")) {
        tab.classList.toggle(
            "active",
            tab.dataset.tab === tipo
        );
    }

    mostrar($("painel-alunos"), tipo === "alunos");
    mostrar($("painel-empresas"), tipo === "empresas");
}

// Eventos
$("login-form").addEventListener("submit", conectar);
$("logout").addEventListener("click", desconectar);

document.querySelectorAll(".tab").forEach(tab => {
    tab.addEventListener("click", () => {
        trocarAba(tab.dataset.tab);
    });
});

$("novo-aluno").addEventListener("click", () => {
    abrirFormulario("alunos");
});

$("nova-empresa").addEventListener("click", () => {
    abrirFormulario("empresas");
});

$("cancelar-aluno").addEventListener("click", () => {
    fecharFormulario("alunos");
});

$("cancelar-empresa").addEventListener("click", () => {
    fecharFormulario("empresas");
});

$("form-aluno").addEventListener("submit", evento => {
    salvar("alunos", evento);
});

$("form-empresa").addEventListener("submit", evento => {
    salvar("empresas", evento);
});
