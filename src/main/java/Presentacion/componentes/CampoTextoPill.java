package Presentacion.componentes;

import Presentacion.estilo.Estilo;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.BorderFactory;
import javax.swing.JTextField;

/**
 * Campo de texto negro con letra blanca y un lápiz en la esquina
 * (pantalla "Nombre"). Es un JTextField normal.
 *
 * @author Equipo 4
 */
public class CampoTextoPill extends JTextField implements Escalable {

    private static final long serialVersionUID = 1L;

    private final float tamanoLetra;

    public CampoTextoPill(String texto, float tamanoLetra) {
        super(texto);
        this.tamanoLetra = tamanoLetra;
        setHorizontalAlignment(CENTER);
        setBackground(Estilo.NEGRO_PILL);
        setForeground(Color.WHITE);
        setCaretColor(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        escalar(1);
    }

    @Override
    public void escalar(double escala) {
        setFont(Estilo.fuenteNegrita((float) (tamanoLetra * escala)));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        Estilo.suavizar(g);
        int s = Math.max(8, getHeight() / 5);
        Icono lapiz = new Icono(Icono.Tipo.LAPIZ, new Color(0xD8, 0xD0, 0xE8));
        lapiz.setSize(s, s);
        Graphics2D gi = (Graphics2D) g.create(getWidth() - s - s / 2, s / 2, s, s);
        lapiz.paint(gi);
        gi.dispose();
        g.dispose();
    }
}
