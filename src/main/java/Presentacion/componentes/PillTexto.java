package Presentacion.componentes;

import Presentacion.estilo.Estilo;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;

/**
 * Rectángulo negro redondeado con texto blanco (nombres y datos de partida).
 * Solo muestra texto; opcionalmente dibuja un lápiz a la derecha.
 *
 * @author Equipo 4
 */
public class PillTexto extends JComponent implements Escalable {

    private static final long serialVersionUID = 1L;

    private String texto;
    private final float tamanoLetra;
    private final boolean negrita;
    private final boolean centrado;
    private boolean lapiz;
    private final int radio;
    private double escala = 1;

    public PillTexto(String texto, float tamanoLetra, boolean negrita, boolean centrado, int radio) {
        this.texto = texto;
        this.tamanoLetra = tamanoLetra;
        this.negrita = negrita;
        this.centrado = centrado;
        this.radio = radio;
        setOpaque(false);
    }

    public PillTexto conLapiz() {
        this.lapiz = true;
        return this;
    }

    public void setTexto(String texto) {
        this.texto = texto;
        repaint();
    }

    public String getTexto() {
        return texto;
    }

    @Override
    public void escalar(double escala) {
        this.escala = escala;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Estilo.suavizar(g);
        double r = radio * 2 * escala;
        g.setColor(Estilo.NEGRO_PILL);
        g.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), r, r));
        g.setColor(new Color(255, 255, 255, 50));
        g.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, r, r));

        float t = (float) (tamanoLetra * escala);
        Font f = negrita ? Estilo.fuenteNegrita(t) : Estilo.fuenteTexto(t);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Color.WHITE);
        double x = centrado ? (getWidth() - fm.stringWidth(texto)) / 2.0 : 8 * escala;
        g.drawString(texto, (float) x, (float) ((getHeight() - fm.getHeight()) / 2.0 + fm.getAscent()));

        if (lapiz) {
            double s = Math.min(getHeight() * 0.55, 14 * escala);
            Icono i = new Icono(Icono.Tipo.LAPIZ, new Color(0xD8, 0xD0, 0xE8));
            i.setSize((int) s, (int) s);
            Graphics2D gi = (Graphics2D) g.create((int) (getWidth() - s - 6 * escala), (int) ((getHeight() - s) / 2), (int) s, (int) s);
            i.paint(gi);
            gi.dispose();
        }
        g.dispose();
    }
}
