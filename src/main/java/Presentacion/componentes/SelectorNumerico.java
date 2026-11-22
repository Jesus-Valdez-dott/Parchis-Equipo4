package Presentacion.componentes;

import Presentacion.estilo.Estilo;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * Barra con título, botón "-", valor y botón "+" (pantalla de configuración).
 * SIN lógica: los botones no cambian el valor solos. Quien implemente el CU
 * conecta getBotonMenos()/getBotonMas() y llama setValor().
 *
 * @author Equipo 4
 */
public class SelectorNumerico extends Tarjeta {

    private static final long serialVersionUID = 1L;

    private final BotonCircular menos = new BotonCircular("-");
    private final BotonCircular mas = new BotonCircular("+");
    private final Etiqueta valor;

    public SelectorNumerico(String titulo, int valorInicial) {
        super(597, 66, Estilo.TAN, 8);
        agregar(new Etiqueta(titulo, Etiqueta.Tipo.NEGRITA, 9.5f, Estilo.TEXTO_OSCURO, SwingConstants.LEFT), 12, 9, 500, 16);
        agregar(menos, 12, 29, 26, 26);
        valor = agregar(new Etiqueta(String.valueOf(valorInicial), Etiqueta.Tipo.NEGRITA, 19f, Color.BLACK), 248, 26, 100, 32);
        agregar(mas, 559, 29, 26, 26);
    }

    public JButton getBotonMenos() { return menos; }
    public JButton getBotonMas() { return mas; }

    public void setValor(int v) {
        valor.setText(String.valueOf(v));
    }

    public int getValor() {
        return Integer.parseInt(valor.getText());
    }

    /** Botoncito blanco redondo con "-" o "+". */
    static class BotonCircular extends JButton {

        private static final long serialVersionUID = 1L;

        BotonCircular(String texto) {
            super(texto);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Estilo.suavizar(g);
            double s = Math.min(getWidth(), getHeight()) - 1;
            Ellipse2D c = new Ellipse2D.Double((getWidth() - s) / 2, (getHeight() - s) / 2, s, s);
            g.setColor(getModel().isPressed() ? new Color(0xDD, 0xDD, 0xDD) : Color.WHITE);
            g.fill(c);
            g.setColor(new Color(0x55, 0x45, 0x40));
            g.setStroke(new BasicStroke(1f));
            g.draw(c);
            g.setFont(Estilo.fuenteNegrita((float) (s * 0.55)));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(Color.BLACK);
            g.drawString(getText(), (float) (getWidth() / 2.0 - fm.stringWidth(getText()) / 2.0),
                    (float) (getHeight() / 2.0 + fm.getAscent() / 2.9));
            g.dispose();
        }
    }
}
