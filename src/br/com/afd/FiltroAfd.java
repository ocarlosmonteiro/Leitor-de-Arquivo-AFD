package br.com.afd;

import java.time.LocalDate;

/** Filtros opcionais aplicados aos registros durante a leitura. Campo nulo ou vazio = sem filtro. */
public final class FiltroAfd {

    public static final FiltroAfd SEM_FILTRO = new FiltroAfd(null, null, null, null);

    private final String documento;
    private final LocalDate dataInicial;
    private final LocalDate dataFinal;
    private final TipoEvento evento;

    /**
     * @param documento   PIS ou CPF; qualquer caractere que não seja dígito é ignorado. Se tiver menos
     *                    de 11 dígitos, é completado com zeros à esquerda.
     * @param dataInicial primeiro dia incluído (opcional)
     * @param dataFinal   último dia incluído (opcional)
     * @param evento      restringe a um tipo de evento (opcional)
     */
    public FiltroAfd(String documento, LocalDate dataInicial, LocalDate dataFinal, TipoEvento evento) {
        String digitos = documento == null ? "" : documento.replaceAll("\\D", "");
        if (!digitos.isEmpty()) {
            digitos = Campos.normalizarDocumento(digitos);
            if (digitos.length() < 11) {
                digitos = "00000000000".substring(digitos.length()) + digitos;
            }
        }
        this.documento = digitos;
        this.dataInicial = dataInicial;
        this.dataFinal = dataFinal;
        this.evento = evento;
    }

    public boolean aceita(RegistroAfd registro) {
        if (evento != null && registro.getEvento() != evento) {
            return false;
        }
        LocalDate dia = registro.getDataHora().toLocalDate();
        if (dataInicial != null && dia.isBefore(dataInicial)) {
            return false;
        }
        if (dataFinal != null && dia.isAfter(dataFinal)) {
            return false;
        }
        return documento.isEmpty() || documento.equals(registro.getDocumento());
    }
}
