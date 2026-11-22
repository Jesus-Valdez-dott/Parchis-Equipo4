package Presentacion.componentes;

import Presentacion.estilo.Estilo;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * Recuadro semitransparente con texto grande encima del tablero
 * (p. ej. "Sabrosuras3000 obtiene 20 movimientos de recompensa...").
 * Está oculto hasta que se llama mostrar().
 *
 * @author Equipo 4
 */
public class MensajeSuperpuesto extends JComponent implements Escalable {

    private static final long serialVersionUID = 1L;

    private String texto = "";
    private double escala = 1;
    private Timer temporizador;

    public MensajeSuperpuesto() {
        setOpaque(false);
        setVisible(false);
    }

    /** Muestra el mensaje; si milisegundos > 0 se oculta solo después de ese tiempo. */
    public void mostrar(String mensaje, int milisegundos) {
        this.texto = mensaje;
        setVisible(true);
        repaint();
        if (temporizador != null) {
            temporizador.stop();
        }
        if (milisegundos > 0) {
            temporizador = new Timer(milisegundos, e -> setVisible(false));
            temporizador.setRepeats(false);
            temporizador.start();
        }
    }

    public void ocultar() {
        setVisible(false);
    }

    @Override
    public void escalar(double escala) {
        this.escala = escala;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Estilo.suavizar(g);
        g.setColor(new Color(0xD9, 0xD9, 0xDE, 200));
        g.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 22 * escala, 22 * escala));
        g.setFont(Estilo.fuenteNegrita((float) (34 * escala)));
        FontMetrics fm = g.getFontMetrics();
        List<String> lineas = partir(texto, fm, getWidth() * 0.9);
        double alto = fm.getHeight() * lineas.size();
        double y = (getHeight() - alto) / 2 + fm.getAscent();
        g.setColor(Color.BLACK);
        for (String l : lineas) {
            g.drawString(l, (float) ((getWidth() - fm.stringWidth(l)) / 2.0), (float) y);
            y += fm.getHeight();
        }
        g.dispose();
    }

    private static List<String> partir(String texto, FontMetrics fm, double ancho) {
        List<String> lineas = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String palabra : texto.split(" ")) {
            String prueba = actual.length() == 0 ? palabra : actual + " " + palabra;
            if (fm.stringWidth(prueba) > ancho && actual.length() > 0) {
                lineas.add(actual.toString());
                actual = new StringBuilder(palabra);
            } else {
                actual = new StringBuilder(prueba);
            }
        }
        if (actual.length() > 0) {
            lineas.add(actual.toString());
        }
        return lineas;
    }
}
