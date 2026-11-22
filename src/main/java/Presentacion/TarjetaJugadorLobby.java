package Presentacion;

import Presentacion.componentes.Avatar;
import Presentacion.componentes.Etiqueta;
import Presentacion.componentes.Icono;
import Presentacion.componentes.PillTexto;
import Presentacion.componentes.Tarjeta;
import Presentacion.estilo.Estilo;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

/**
 * Lugar de un jugador en el Lobby. Tiene dos aspectos:
 * - ocupado: avatar, "JUGADOR n", nombre y etiqueta "LISTO" (opcional);
 * - libre: "Esperando jugador...".
 * Solo muestra datos.
 *
 * @author Equipo 4
 */
public class TarjetaJugadorLobby extends Tarjeta {

    private static final long serialVersionUID = 1L;

    private final Avatar avatar = new Avatar(0);
    private final Etiqueta numero = new Etiqueta("", Etiqueta.Tipo.NEGRITA, 7.5f, Estilo.ROSA, SwingConstants.LEFT);
    private final PillTexto nombre = new PillTexto("", 10, true, false, 4).conLapiz();
    private final EtiquetaListo listo = new EtiquetaListo();
    private final JComponent iconoEspera = new CirculoAgregar();
    private final Etiqueta textoEspera = new Etiqueta("Esperando jugador...", Etiqueta.Tipo.TEXTO, 10, new Color(0x5A, 0x3C, 0x48));

    public TarjetaJugadorLobby() {
        super(396, 130, Estilo.TAN, 16);
        avatar.setAnillo(Estilo.ROSA);
        agregar(avatar, 14, 15, 36, 36);
        agregar(numero, 60, 15, 120, 10);
        agregar(nombre, 59, 27, 322, 21);
        agregar(listo, 330, 57, 50, 16);
        agregar(iconoEspera, 180, 37, 35, 35);
        agregar(textoEspera, 0, 78, 396, 18);
        setEsperando();
    }

    /** Muestra al jugador en este lugar. */
    public void setJugador(int numeroJugador, String nombreJugador, int indiceAvatar, boolean estaListo) {
        avatar.setIndice(indiceAvatar);
        numero.setText("JUGADOR " + numeroJugador);
        nombre.setTexto(nombreJugador);
        listo.setVisible(estaListo);
        mostrarOcupado(true);
    }

    /** Deja el lugar libre ("Esperando jugador..."). */
    public void setEsperando() {
        listo.setVisible(false);
        mostrarOcupado(false);
    }

    private void mostrarOcupado(boolean ocupado) {
        avatar.setVisible(ocupado);
        numero.setVisible(ocupado);
        nombre.setVisible(ocupado);
        iconoEspera.setVisible(!ocupado);
        textoEspera.setVisible(!ocupado);
        setFondo(ocupado ? Estilo.TAN : Estilo.TAN_OSCURO);
        if (ocupado) {
            conBorde(new Color(0xD9, 0x5C, 0xE0), false).conBrillo(new Color(0xD9, 0x5C, 0xE0));
        } else {
            conBorde(new Color(0x6A, 0x44, 0x30), true).conBrillo(null);
        }
    }

    /** Etiqueta verde "● LISTO". */
    private static class EtiquetaListo extends JComponent {

        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(java.awt.Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Estilo.suavizar(g);
            double w = getWidth(), h = getHeight();
            g.setColor(new Color(0x2E, 0xE0, 0x5A, 45));
            g.fill(new RoundRectangle2D.Double(0, 0, w, h, h * 0.4, h * 0.4));
            g.setColor(new Color(0x2E, 0xE0, 0x5A, 120));
            g.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, h * 0.4, h * 0.4));
            g.setColor(Estilo.VERDE_LISTO);
            double r = h * 0.18;
            g.fill(new Ellipse2D.Double(h * 0.35, h / 2 - r, 2 * r, 2 * r));
            g.setFont(Estilo.fuenteNegrita((float) (h * 0.55)));
            g.drawString("LISTO", (float) (h * 0.8), (float) (h / 2 + g.getFontMetrics().getAscent() / 2.8));
            g.dispose();
        }
    }

    /** Círculo oscuro con el ícono de "agregar usuario". */
    private static class CirculoAgregar extends JComponent {

        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(java.awt.Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Estilo.suavizar(g);
            double s = Math.min(getWidth(), getHeight());
            g.setColor(new Color(0x2A, 0x12, 0x30));
            g.fill(new Ellipse2D.Double(0, 0, s, s));
            Icono i = new Icono(Icono.Tipo.AGREGAR_USUARIO, new Color(0x8C, 0x70, 0x90));
            i.setSize((int) (s * 0.5), (int) (s * 0.5));
            Graphics2D gi = (Graphics2D) g.create((int) (s * 0.25), (int) (s * 0.25), (int) (s * 0.5), (int) (s * 0.5));
            i.paint(gi);
            gi.dispose();
            g.dispose();
        }
    }
}
