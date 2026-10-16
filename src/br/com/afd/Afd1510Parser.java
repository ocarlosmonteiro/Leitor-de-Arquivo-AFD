package br.com.afd;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Layout do AFD da Portaria 1510/2009 (identificação pelo PIS).
 *
 * <p>As posições abaixo são base 1 e inclusivas, como na documentação.
 *
 * <pre>
 * Tipo 3 - marcação
 *   1-9   NSR
 *   10    tipo do registro (3)
 *   11-18 data ddMMaaaa
 *   19-22 hora hhmm
 *   23-34 PIS (12 posições; o relógio coloca um 0 à esquerda quando o PIS tem 11 dígitos)
 *
 * Tipo 5 - inclusão, alteração ou exclusão de empregado
 *   1-9   NSR
 *   10    tipo do registro (5)
 *   11-18 data ddMMaaaa da gravação
 *   19-22 hora hhmm da gravação
 *   23    operação: I (inclusão), A (alteração) ou E (exclusão)
 *   24-35 PIS (12 posições, mesmo preenchimento com 0 à esquerda)
 *   36-87 nome do empregado
 * </pre>
 *
 * Se algum arquivo real usar outras posições, ajuste as constantes abaixo.
 */
public final class Afd1510Parser implements AfdParser {

    private static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("ddMMuuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HHmm");

    private static final int POS_TIPO = 10;
    private static final int POS_OPERACAO = 23;

    private static final int MARCACAO_PIS_INI = 23;
    private static final int MARCACAO_PIS_FIM = 34;

    private static final int CADASTRO_PIS_INI = 24;
    private static final int CADASTRO_PIS_FIM = 35;
    private static final int CADASTRO_NOME_INI = 36;
    private static final int CADASTRO_NOME_FIM = 87;

    @Override
    public RegistroAfd parse(String linha, int numeroLinha) throws AfdFormatException {
        if (linha.length() < POS_TIPO) {
            return null;
        }
        char tipo = linha.charAt(POS_TIPO - 1);
        switch (tipo) {
            case '3':
                return marcacao(linha, numeroLinha);
            case '5':
                return cadastro(linha, numeroLinha);
            default:
                return null;
        }
    }

    private RegistroAfd marcacao(String linha, int numeroLinha) throws AfdFormatException {
        Campos.exigirTamanho(linha, MARCACAO_PIS_FIM);
        LocalDateTime dataHora = dataHora(linha);
        String pis = Campos.normalizarDocumento(Campos.campo(linha, MARCACAO_PIS_INI, MARCACAO_PIS_FIM));
        Campos.validarDocumento(pis, "PIS");
        return new RegistroAfd(numeroLinha, Campos.campo(linha, 1, 9), '3',
                TipoEvento.MARCACAO, dataHora, pis, "");
    }

    private RegistroAfd cadastro(String linha, int numeroLinha) throws AfdFormatException {
        Campos.exigirTamanho(linha, CADASTRO_PIS_FIM);
        LocalDateTime dataHora = dataHora(linha);
        char operacao = linha.charAt(POS_OPERACAO - 1);
        TipoEvento evento = TipoEvento.daOperacao(operacao);
        if (evento == null) {
            throw new AfdFormatException("operação desconhecida no campo 23: '" + operacao + "'");
        }
        String pis = Campos.normalizarDocumento(Campos.campo(linha, CADASTRO_PIS_INI, CADASTRO_PIS_FIM));
        Campos.validarDocumento(pis, "PIS");
        String nome = Campos.campo(linha, CADASTRO_NOME_INI, CADASTRO_NOME_FIM).trim();
        return new RegistroAfd(numeroLinha, Campos.campo(linha, 1, 9), '5', evento, dataHora, pis, nome);
    }

    private static LocalDateTime dataHora(String linha) throws AfdFormatException {
        try {
            LocalDate data = LocalDate.parse(Campos.campo(linha, 11, 18), DATA);
            LocalTime hora = LocalTime.parse(Campos.campo(linha, 19, 22), HORA);
            return LocalDateTime.of(data, hora);
        } catch (DateTimeParseException e) {
            throw new AfdFormatException("data/hora inválida (campos 11-22): \""
                    + Campos.campo(linha, 11, 22) + "\"");
        }
    }
}
