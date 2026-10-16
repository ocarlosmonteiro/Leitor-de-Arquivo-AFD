package br.com.afd;

/** Linha do AFD que deveria ser interpretada, mas está fora do layout esperado. */
public class AfdFormatException extends Exception {

    private static final long serialVersionUID = 1L;

    public AfdFormatException(String mensagem) {
        super(mensagem);
    }
}
