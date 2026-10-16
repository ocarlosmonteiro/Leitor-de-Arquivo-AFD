package br.com.afd;

/** Interpreta uma linha de AFD de uma portaria específica. */
public interface AfdParser {

    /**
     * @param linha       conteúdo da linha, sem o terminador
     * @param numeroLinha posição da linha no arquivo (começa em 1)
     * @return o registro, ou {@code null} se a linha for de um tipo que o programa não exibe
     *         (cabeçalho, trailer, ajuste de relógio etc.)
     * @throws AfdFormatException se a linha for de um tipo exibido, mas estiver fora do layout
     */
    RegistroAfd parse(String linha, int numeroLinha) throws AfdFormatException;
}
