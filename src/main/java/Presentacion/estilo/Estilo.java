package Presentacion.estilo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;

/**
 * Colores, fuentes y dibujos comunes de todas las pantallas (tomados del storyboard).
 * Solo apariencia: aquí no hay lógica del juego.
 *
 * Fuentes incluidas en src/main/resources/fuentes (licencia libre OFL):
 * Lilita One para los títulos y Roboto para el texto.
 *
 * @author Equipo 4
 */
public final class Estilo {

    private Estilo() {
    }

    // ------------------------------------------------------------------
    // Colores del storyboard
    // ------------------------------------------------------------------
    public static final Color FONDO = new Color(0x22, 0x02, 0x27);
    public static final Color DORADO = new Color(0xDE, 0xA5, 0x53);
    public static final Color DORADO_CLARO = new Color(0xFF, 0xE0, 0x7A);
    public static final Color DORADO_OSCURO = new Color(0xA8, 0x6A, 0x12);
    public static final Color CONTORNO_TITULO = new Color(0x3A, 0x1C, 0x05);
    public static final Color TAN = new Color(0xE9, 0xAE, 0x62);
    public static final Color TAN_OSCURO = new Color(0x98, 0x6A, 0x49);
    public static final Color NEGRO_PILL = new Color(0x0D, 0x09, 0x10);
    public static final Color ROSA = new Color(0xDC, 0x81, 0xC6);
    public static final Color VERDE = new Color(0x00, 0x99, 0x51);
    public static final Color VERDE_LISTO = new Color(0x2E, 0xE0, 0x5A);
    public static final Color ROJO = new Color(0xEC, 0x22, 0x1E);
    public static final Color ROJO_RECHAZAR = new Color(0xD6, 0x2D, 0x2C);
    public static final Color ROJO_PURO = new Color(0xFB, 0x00, 0x02);
    public static final Color AZUL = new Color(0x29, 0x6B, 0xBF);
    public static final Color CREMA = new Color(0xF5, 0xE9, 0xD0);
    public static final Color BLANCO_PANEL = new Color(0xED, 0xF1, 0xF2);
    public static final Color TEXTO_OSCURO = new Color(0x2A, 0x14, 0x05);
    public static final Color TEXTO_GRIS = new Color(0x9A, 0x8C, 0xA0);
    public static final Color AMARILLO_DADO = new Color(0xEF, 0xC9, 0x2D);

    /** Los 12 colores de la pantalla "Color" (en orden de izquierda a derecha, de arriba abajo). */
    public static final Color[] PALETA_JUGADOR = {
        new Color(0xD8, 0x25, 0x2B), new Color(0xF0, 0x7C, 0x01), new Color(0xF1, 0xCA, 0x25),
        new Color(0x99, 0xCF, 0x31), new Color(0x29, 0xB4, 0x97), new Color(0x54, 0xAF, 0xDE),
        new Color(0x48, 0x4C, 0xB0), new Color(0x7D, 0x3C, 0xB2), new Color(0xB1, 0x23, 0x99),
        new Color(0x60, 0x3C, 0x24), new Color(0x05, 0x30, 0x02), new Color(0x00, 0x00, 0x00)
    };

    // ------------------------------------------------------------------
    // Fuentes
    // ------------------------------------------------------------------
    private static final Font TITULO_BASE = cargar("/fuentes/LilitaOne-Regular.ttf", new Font(Font.DIALOG, Font.BOLD, 12));
    private static final Font TEXTO_BASE = cargar("/fuentes/Roboto-Regular.ttf", new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    private static final Font TEXTO_NEGRITA_BASE = cargar("/fuentes/Roboto-Bold.ttf", new Font(Font.SANS_SERIF, Font.BOLD, 12));

    private static Font cargar(String ruta, Font respaldo) {
        try (InputStream in = Estilo.class.getResourceAsStream(ruta)) {
            if (in == null) {
                return respaldo;
            }
            Font f = Font.createFont(Font.TRUETYPE_FONT, in);
            // Registrarla permite que los textos HTML (<b>) usen la versión en negrita
            java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
            return f;
        } catch (IOException | FontFormatException e) {
            return respaldo;
        }
    }

    public static Font fuenteTitulo(float tamano) {
        return TITULO_BASE.deriveFont(Font.PLAIN, tamano);
    }

    public static Font fuenteTexto(float tamano) {
        return TEXTO_BASE.deriveFont(Font.PLAIN, tamano);
    }

    public static Font fuenteNegrita(float tamano) {
        return TEXTO_NEGRITA_BASE.deriveFont(Font.PLAIN, tamano);
    }

    /** La misma fuente si puede mostrar el texto; si no (p. ej. "←"), una del sistema. */
    public static Font paraTexto(Font f, String texto) {
        return texto == null || f.canDisplayUpTo(texto) == -1 ? f
                : new Font(Font.SANS_SERIF, f.getStyle(), Math.round(f.getSize2D()));
    }

    // ------------------------------------------------------------------
    // Dibujos comunes
    // ------------------------------------------------------------------

    public static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    /**
     * Texto dorado "3D" de los títulos: sombra, contorno oscuro y degradado dorado.
     * (cx, base) = centro horizontal y línea base del texto.
     */
    public static void pintarTextoDorado(Graphics2D g, String texto, Font fuente, double cx, double base) {
        FontRenderContext frc = g.getFontRenderContext();
        GlyphVector gv = fuente.createGlyphVector(frc, texto);
        Rectangle2D caja = gv.getVisualBounds();
        double x = cx - caja.getWidth() / 2 - caja.getX();
        Shape forma = gv.getOutline((float) x, (float) base);
        float tam = fuente.getSize2D();

        // Sombra / relieve hacia abajo
        Shape sombra = AffineTransform.getTranslateInstance(0, tam * 0.07).createTransformedShape(forma);
        g.setColor(DORADO_OSCURO.darker());
        g.fill(sombra);
        g.setStroke(new BasicStroke(tam * 0.09f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(CONTORNO_TITULO);
        g.draw(sombra);

        // Contorno
        g.setColor(CONTORNO_TITULO);
        g.draw(forma);

        // Relleno con degradado dorado
        Rectangle2D b = forma.getBounds2D();
        g.setPaint(new GradientPaint(0, (float) b.getMinY(), DORADO_CLARO,
                0, (float) b.getMaxY(), DORADO_OSCURO));
        g.fill(forma);

        // Brillo superior
        g.setPaint(new GradientPaint(0, (float) b.getMinY(), new Color(255, 255, 255, 110),
                0, (float) (b.getMinY() + b.getHeight() * 0.45), new Color(255, 255, 255, 0)));
        g.fill(forma);
    }
}
