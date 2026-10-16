package br.com.afd;

/** Portarias de AFD suportadas. */
public enum Portaria {
    P1510("Portaria 1510 (PIS)", "PIS", new Afd1510Parser()),
    P671("Portaria 671 (CPF)", "CPF", new Afd671Parser());

    private final String descricao;
    private final String rotuloDocumento;
    private final AfdParser parser;

    Portaria(String descricao, String rotuloDocumento, AfdParser parser) {
        this.descricao = descricao;
        this.rotuloDocumento = rotuloDocumento;
        this.parser = parser;
    }

    public String getDescricao() {
        return descricao;
    }

    /** "PIS" ou "CPF", conforme a portaria. */
    public String getRotuloDocumento() {
        return rotuloDocumento;
    }

    public AfdParser getParser() {
        return parser;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
