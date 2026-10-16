package br.com.afd;

import java.time.LocalDateTime;

/** Um registro de marcação ou de cadastro de empregado lido do AFD. */
public final class RegistroAfd {

    private final int numeroLinha;
    private final String nsr;
    private final char tipoRegistro;
    private final TipoEvento evento;
    private final LocalDateTime dataHora;
    private final String documento;
    private final String nome;

    public RegistroAfd(int numeroLinha, String nsr, char tipoRegistro, TipoEvento evento,
                       LocalDateTime dataHora, String documento, String nome) {
        this.numeroLinha = numeroLinha;
        this.nsr = nsr;
        this.tipoRegistro = tipoRegistro;
        this.evento = evento;
        this.dataHora = dataHora;
        this.documento = documento;
        this.nome = nome == null ? "" : nome;
    }

    /** Número da linha no arquivo (começa em 1). */
    public int getNumeroLinha() {
        return numeroLinha;
    }

    public String getNsr() {
        return nsr;
    }

    /** Código do tipo de registro no arquivo ('3', '5' ou '7'). */
    public char getTipoRegistro() {
        return tipoRegistro;
    }

    public TipoEvento getEvento() {
        return evento;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    /** PIS (portaria 1510) ou CPF (portaria 671), somente dígitos. */
    public String getDocumento() {
        return documento;
    }

    /** Nome do empregado; vazio nas marcações. */
    public String getNome() {
        return nome;
    }
}
