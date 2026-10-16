package br.com.afd;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Ponto de entrada do leitor de AFD. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // segue com o visual padrão do Swing
            }
            new AfdFrame().setVisible(true);
        });
    }
}
