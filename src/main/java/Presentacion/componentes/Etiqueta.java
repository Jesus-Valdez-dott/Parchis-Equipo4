package Presentacion.componentes;

import Presentacion.estilo.Estilo;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Texto simple cuya letra crece o se achica con la ventana.
 * Acepta HTML (por ejemplo "<html><center>dos<br>líneas</center></html>").
 *
 * @author Equipo 4
 */
public class Etiqueta extends JLabel implements Escalable {

    private static final long serialVersionUID = 1L;

    public enum Tipo { TEXTO, NEGRITA, TITULO }

    private final Tipo tipo;
    private final float tamanoDiseno;

    public Etiqueta(String texto, Tipo tipo, float tamanoDiseno, Color color) {
        this(texto, tipo, tamanoDiseno, color, SwingConstants.CENTER);
    }

    public Etiqueta(String texto, Tipo tipo, float tamanoDiseno, Color color, int alineacion) {
        super("", alineacion);
        this.tipo = tipo;
        this.tamanoDiseno = tamanoDiseno;
        setForeground(color);
        setText(texto);
        escalar(1);
    }

    @Override
    public void setText(String texto) {
        // En HTML, Swing no respeta la fuente negrita: se marca con <b>
        if (tipo == Tipo.NEGRITA && texto != null && texto.startsWith("<html>") && !texto.contains("<b>")) {
            texto = "<html><b>" + texto.substring(6).replace("</html>", "") + "</b></html>";
        }
        super.setText(texto);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Estilo.suavizar(g2);
        super.paintComponent(g2);
        g2.dispose();
    }

    @Override
    public void escalar(double escala) {
        float t = (float) (tamanoDiseno * escala);
        switch (tipo) {
            case NEGRITA: setFont(Estilo.fuenteNegrita(t)); break;
            case TITULO: setFont(Estilo.fuenteTitulo(t)); break;
            default: setFont(Estilo.fuenteTexto(t)); break;
        }
    }
}
