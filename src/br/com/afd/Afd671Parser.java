package br.com.afd;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Layout do AFD da Portaria MTP 671/2021 (identificação pelo CPF).
 *
 * <p>As posições abaixo são base 1 e inclusivas. A data/hora usa ISO 8601
 * (por exemplo {@code 2026-10-02T07:45:00-0300}, 24 caracteres).
 *
 * <pre>
 * Tipo 3 - marcação de ponto (REP-C e REP-A)
 *   1-9   NSR
 *   10    tipo do registro (3)
 *   11-34 data e hora da marcação
 *   35-46 CPF (12 posições; zero à esquerda quando o CPF tem 11 dígitos)
 *   47-50 CRC-16
 *
 * Tipo 5 - inclusão, alteração ou exclusão de empregado
 *   1-9     NSR
 *   10      tipo do registro (5)
 *   11-34   data e hora da gravação
 *   35      operação: I (inclusão), A (alteração) ou E (exclusão)
 *   36-47   CPF (12 posições)
 *   48-99   nome do empregado
 *   100-103 demais dados
 *   104-107 CRC-16
 *
 * Tipo 7 - marcação de ponto (REP-P); CPF na mesma posição do tipo 3
 * </pre>
 *
 * Confira estas posições com o manual da portaria e com um arquivo real; se algo divergir,
 * ajuste as constantes abaixo.
 */
public final class Afd671Parser implements AfdParser {

    private static final int POS_TIPO = 10;
    private static final int POS_OPERACAO = 35;

    private static final int DATAHORA_INI = 11;
    /** Fim da parte "AAAA-MM-DDThh:mm:ss"; o restante do campo é o fuso horário. */
    private static final int DATAHORA_FIM_SEM_FUSO = 29;
    private static final int DATAHORA_FIM = 34;

    private static final int MARCACAO_CPF_INI = 35;
    private static final int MARCACAO_CPF_FIM = 46;

    private static final int CADASTRO_CPF_INI = 36;
    private static final int CADASTRO_CPF_FIM = 47;
    private static final int CADASTRO_NOME_INI = 48;
    private static final int CADASTRO_NOME_FIM = 99;

    @Override
    public RegistroAfd parse(String linha, int numeroLinha) throws AfdFormatException {
        if (linha.length() < POS_TIPO) {
            return null;
        }
        char tipo = linha.charAt(POS_TIPO - 1);
        switch (tipo) {
            case '3':
            case '7':
                return marcacao(linha, numeroLinha, tipo);
            case '5':
                return cadastro(linha, numeroLinha);
            default:
                return null;
        }
    }

    private RegistroAfd marcacao(String linha, int numeroLinha, char tipo) throws AfdFormatException {
        Campos.exigirTamanho(linha, MARCACAO_CPF_FIM);
        LocalDateTime dataHora = dataHora(linha);
        String cpf = Campos.normalizarDocumento(Campos.campo(linha, MARCACAO_CPF_INI, MARCACAO_CPF_FIM));
        Campos.validarDocumento(cpf, "CPF");
        return new RegistroAfd(numeroLinha, Campos.campo(linha, 1, 9), tipo,
                TipoEvento.MARCACAO, dataHora, cpf, "");
    }

    private RegistroAfd cadastro(String linha, int numeroLinha) throws AfdFormatException {
        Campos.exigirTamanho(linha, CADASTRO_CPF_FIM);
        LocalDateTime dataHora = dataHora(linha);
        char operacao = linha.charAt(POS_OPERACAO - 1);
        TipoEvento evento = TipoEvento.daOperacao(operacao);
        if (evento == null) {
            throw new AfdFormatException("operação desconhecida no campo 35: '" + operacao + "'");
        }
        String cpf = Campos.normalizarDocumento(Campos.campo(linha, CADASTRO_CPF_INI, CADASTRO_CPF_FIM));
        Campos.validarDocumento(cpf, "CPF");
        String nome = Campos.campo(linha, CADASTRO_NOME_INI, CADASTRO_NOME_FIM).trim();
        return new RegistroAfd(numeroLinha, Campos.campo(linha, 1, 9), '5', evento, dataHora, cpf, nome);
    }

    private static LocalDateTime dataHora(String linha) throws AfdFormatException {
        try {
            return LocalDateTime.parse(Campos.campo(linha, DATAHORA_INI, DATAHORA_FIM_SEM_FUSO));
        } catch (DateTimeParseException e) {
            throw new AfdFormatException("data/hora inválida (campos " + DATAHORA_INI + "-" + DATAHORA_FIM
                    + "): \"" + Campos.campo(linha, DATAHORA_INI, DATAHORA_FIM) + "\"");
        }
    }
}
