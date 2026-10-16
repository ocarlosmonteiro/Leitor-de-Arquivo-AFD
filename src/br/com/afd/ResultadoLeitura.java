package br.com.afd;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

/** Resultado da leitura de um arquivo AFD. */
public final class ResultadoLeitura {

    private final List<RegistroAfd> registros;
    private final int totalLinhas;
    private final int registrosLidos;
    private final int linhasIgnoradas;
    private final int linhasInvalidas;
    private final List<String> mensagensErro;
    private final Charset charset;

    ResultadoLeitura(List<RegistroAfd> registros, int totalLinhas, int registrosLidos, int linhasIgnoradas,
                     int linhasInvalidas, List<String> mensagensErro, Charset charset) {
        this.registros = Collections.unmodifiableList(registros);
        this.totalLinhas = totalLinhas;
        this.registrosLidos = registrosLidos;
        this.linhasIgnoradas = linhasIgnoradas;
        this.linhasInvalidas = linhasInvalidas;
        this.mensagensErro = Collections.unmodifiableList(mensagensErro);
        this.charset = charset;
    }

    /** Registros que passaram pelos filtros. */
    public List<RegistroAfd> getRegistros() {
        return registros;
    }

    public int getTotalLinhas() {
        return totalLinhas;
    }

    /** Registros de marcação/cadastro interpretados com sucesso, antes de aplicar os filtros. */
    public int getRegistrosLidos() {
        return registrosLidos;
    }

    /** Linhas de tipos que o programa não exibe (cabeçalho, trailer etc.) ou em branco. */
    public int getLinhasIgnoradas() {
        return linhasIgnoradas;
    }

    /** Linhas de marcação/cadastro que não seguem o layout. */
    public int getLinhasInvalidas() {
        return linhasInvalidas;
    }

    /** As primeiras mensagens de erro (o total pode ser maior; veja {@link #getLinhasInvalidas()}). */
    public List<String> getMensagensErro() {
        return mensagensErro;
    }

    public Charset getCharset() {
        return charset;
    }
}
