package br.com.afd;

/** Tipo de evento representado por um registro do AFD que o programa exibe. */
public enum TipoEvento {
    MARCACAO("Marcação"),
    INCLUSAO("Inclusão"),
    ALTERACAO("Alteração"),
    EXCLUSAO("Exclusão");

    private final String descricao;

    TipoEvento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Converte o indicador de operação do registro de empregado (I, A ou E).
     *
     * @return o evento correspondente ou {@code null} se o indicador for desconhecido
     */
    public static TipoEvento daOperacao(char operacao) {
        switch (operacao) {
            case 'I':
                return INCLUSAO;
            case 'A':
                return ALTERACAO;
            case 'E':
                return EXCLUSAO;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return descricao;
    }
}
