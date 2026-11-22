package Presentacion;

import Presentacion.componentes.Boton;
import Presentacion.componentes.PanelDiseno;
import Presentacion.estilo.Estilo;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;
import javax.swing.JComponent;

/**
 * Pantalla de inicio del juego (storyboard: "Pantalla de inicio del Juego parchis").
 * SIN lógica: cada CU conecta los botones con getBotonX().addActionListener(...).
 *
 * @author Equipo 4
 */
public class PantallaInicio extends PanelDiseno {

    private static final long serialVersionUID = 1L;

    private final Boton crearPartida = Boton.tan("Crear Partida", Boton.PILDORA, 18);
    private final Boton personalizar = Boton.tan("Personalizar", Boton.PILDORA, 18);
    private final Boton unirsePartida = Boton.tan("Unirse a Partida", Boton.PILDORA, 18);

    public PantallaInicio() {
        agregar(new Logo(), 470, 32, 260, 135);
        agregar(crearPartida, 190, 262, 163, 82);
        agregar(personalizar, 518, 260, 163, 82);
        agregar(unirsePartida, 840, 262, 163, 82);
    }

    public JButton getBotonCrearPartida() { return crearPartida; }
    public JButton getBotonPersonalizar() { return personalizar; }
    public JButton getBotonUnirsePartida() { return unirsePartida; }

    /** Líneas doradas del fondo (en coordenadas del storyboard, 1200 x 675). */
    @Override
    protected void pintarFondo(Graphics2D g) {
        g.setColor(new Color(0xDE, 0xA5, 0x53, 60));
        g.setStroke(new BasicStroke(7f));
        dibujarLineas(g);
        g.setColor(Estilo.DORADO);
        g.setStroke(new BasicStroke(3f));
        dibujarLineas(g);
    }

    private void dibujarLineas(Graphics2D g) {
        // Diagonales
        double[][] lineas = {
            {150, -5, 492, 205}, {-5, 78, 170, 186}, {-5, 527, 165, 426}, {38, 680, 498, 410},
            {1050, -5, 708, 205}, {1205, 78, 1030, 186}, {1205, 527, 1035, 426}, {1162, 680, 702, 410},
            {405, -5, 520, 62}, {795, -5, 680, 62},
            {325, 680, 600, 497}, {600, 497, 875, 680}
        };
        for (double[] l : lineas) {
            g.draw(new Line2D.Double(l[0], l[1], l[2], l[3]));
        }
        // Círculo central
        g.draw(new Ellipse2D.Double(600 - 143, 307 - 143, 286, 286));
        // "Pistas" de los lados
        g.draw(pista(-10, 317, 187, 423, true));
        g.draw(pista(-10, 313, 232, 382, true));
        g.draw(pista(1210, 883, 187, 423, false));
        g.draw(pista(1210, 887, 232, 382, false));
    }

    private static Path2D pista(double xBorde, double xCentroArco, double arriba, double abajo, boolean izquierda) {
        double r = (abajo - arriba) / 2;
        Path2D p = new Path2D.Double();
        p.moveTo(xBorde, arriba);
        p.lineTo(xCentroArco, arriba);
        p.append(new Arc2D.Double(xCentroArco - r, arriba, 2 * r, 2 * r, 90, izquierda ? -180 : 180, Arc2D.OPEN), true);
        p.lineTo(xBorde, abajo);
        return p;
    }

    /** Logo: cuatro fichas de colores sobre un tablerito y el texto "Parchis!". */
    private static class Logo extends JComponent {

        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Estilo.suavizar(g);
            double s = getWidth() / 260.0;
            g.scale(s, s);

            // Tablerito detrás de las fichas
            Path2D tablero = new Path2D.Double();
            tablero.moveTo(70, 62);
            tablero.lineTo(190, 62);
            tablero.lineTo(205, 82);
            tablero.lineTo(55, 82);
            tablero.closePath();
            g.setColor(new Color(0xF2, 0xEE, 0xE4));
            g.fill(tablero);
            g.setColor(new Color(0x7A, 0x6A, 0x5A));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(tablero);

            Color[] colores = {new Color(0xD8, 0x25, 0x2B), new Color(0x2F, 0x6F, 0xD1),
                new Color(0x1F, 0x9D, 0x55), new Color(0xF1, 0xB8, 0x25)};
            double[] xs = {72, 105, 150, 185};
            double[] ys = {44, 28, 28, 42};
            for (int i = 0; i < 4; i++) {
                ficha(g, xs[i], ys[i], colores[i]);
            }
            Estilo.pintarTextoDorado(g, "Parchis!", Estilo.fuenteTitulo(64), 130, 122);
            g.dispose();
        }

        private static void ficha(Graphics2D g, double cx, double y, Color c) {
            GradientPaint brillo = new GradientPaint((float) (cx - 10), 0, c.brighter(), (float) (cx + 12), 0, c.darker());
            g.setPaint(brillo);
            g.fill(new RoundRectangle2D.Double(cx - 13, y + 30, 26, 9, 8, 8));       // base
            Path2D cuerpo = new Path2D.Double();
            cuerpo.moveTo(cx - 10, y + 32);
            cuerpo.quadTo(cx, y + 6, cx + 10, y + 32);
            cuerpo.closePath();
            g.fill(cuerpo);
            g.fill(new Ellipse2D.Double(cx - 8, y, 16, 16));                         // cabeza
            g.setColor(new Color(0, 0, 0, 120));
            g.setStroke(new BasicStroke(1f));
            g.draw(new Ellipse2D.Double(cx - 8, y, 16, 16));
            g.draw(new RoundRectangle2D.Double(cx - 13, y + 30, 26, 9, 8, 8));
            g.setColor(new Color(255, 255, 255, 150));
            g.fill(new Ellipse2D.Double(cx - 4, y + 3, 5, 5));
        }
    }
}
