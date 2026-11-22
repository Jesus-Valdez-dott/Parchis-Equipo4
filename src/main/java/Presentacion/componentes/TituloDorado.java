package Presentacion.componentes;

import Presentacion.estilo.Estilo;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;

/**
 * Título con letras doradas en relieve (como "Lobby", "Color", "Resultados").
 * Acepta varias líneas separadas por '\n'.
 *
 * @author Equipo 4
 */
public class TituloDorado extends JComponent implements Escalable {

    private static final long serialVersionUID = 1L;

    private String texto;
    private final float tamanoDiseno;
    private double escala = 1;

    public TituloDorado(String texto, float tamanoDiseno) {
        this.texto = texto;
        this.tamanoDiseno = tamanoDiseno;
    }

    public void setTexto(String texto) {
        this.texto = texto;
        repaint();
    }

    @Override
    public void escalar(double escala) {
        this.escala = escala;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Estilo.suavizar(g);
        Font f = Estilo.fuenteTitulo((float) (tamanoDiseno * escala));
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        String[] lineas = texto.split("\n");
        double altoLinea = fm.getAscent() * 0.98;
        double altoTotal = altoLinea * lineas.length;
        double y = (getHeight() - altoTotal) / 2 + fm.getAscent() * 0.9;
        for (String linea : lineas) {
            Estilo.pintarTextoDorado(g, linea, f, getWidth() / 2.0, y);
            y += altoLinea;
        }
        g.dispose();
    }
}
