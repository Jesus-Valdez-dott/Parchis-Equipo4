package Presentacion;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.JPanel;

/**
 * Dibuja el tablero: numeración, salidas y colores de la Fig. 1 del documento
 * del juego, con el estilo del storyboard (marco crema y esquinas cuadradas
 * con un cuadro blanco donde esperan las fichas).
 *
 * Geometría: el tablero mide 21 unidades (u). Cada brazo de la cruz mide 7u de
 * ancho (3 columnas) y 8 filas de 1u (la fila más interna es un trapecio). El
 * brazo de arriba se define una sola vez y los otros tres se obtienen rotándolo.
 *
 * Solo pinta lo que recibe; no conoce al modelo.
 *
 * @author Jesús
 */
public class PanelTablero extends JPanel {

    private static final long serialVersionUID = 1L;

    // Colores del tablero (Fig. 1)
    // Tonos del storyboard
    static final Color ROJO = new Color(0xD3, 0x2F, 0x2F);
    static final Color AZUL = new Color(0x2C, 0x6E, 0xC6);
    static final Color VERDE = new Color(0x23, 0x92, 0x4B);
    static final Color AMARILLO = new Color(0xF2, 0xC7, 0x26);
    /** El storyboard no muestra números en las casillas; ponerlo en true ayuda a depurar. */
    private static final boolean MOSTRAR_NUMEROS = false;
    private static final Color CREMA = new Color(0xF5, 0xE9, 0xD0);

    private static final int UNIDADES = 21;
    /** Brazos en orden de rotación: 0 arriba, 1 derecha, 2 abajo, 3 izquierda. */
    private static final String[] COLOR_BRAZO = {"ROJO", "AZUL", "AMARILLO", "VERDE"};
    /** Casilla central exterior de cada brazo (casilla de entrada al pasillo). */
    private static final int[] CASILLA_ENTRADA = {34, 17, 68, 51};
    private static final Set<Integer> SEGURAS = Set.of(5, 12, 17, 22, 29, 34, 39, 46, 51, 56, 63, 68);
    private static final Map<Integer, String> SALIDAS = Map.of(39, "ROJO", 22, "AZUL", 5, "AMARILLO", 56, "VERDE");

    // Datos a pintar (los pone la PantallaPartida)
    private Map<String, Integer> posiciones = new LinkedHashMap<>();
    private Map<String, String> coloresJugadores = new HashMap<>();
    private List<String> fichasSeleccionables = new ArrayList<>();

    // Dónde quedó dibujada cada ficha (para detectar clics)
    private final Map<String, Ellipse2D> fichasDibujadas = new HashMap<>();
    private Consumer<String> alSeleccionarFicha = id -> { };

    public PanelTablero() {
        setOpaque(false);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (Map.Entry<String, Ellipse2D> f : fichasDibujadas.entrySet()) {
                    if (f.getValue().contains(e.getPoint()) && fichasSeleccionables.contains(f.getKey())) {
                        alSeleccionarFicha.accept(f.getKey());
                        return;
                    }
                }
            }
        });
    }

    public void setAlSeleccionarFicha(Consumer<String> accion) {
        this.alSeleccionarFicha = accion;
    }

    public void mostrar(Map<String, Integer> posiciones, Map<String, String> coloresJugadores,
            List<String> fichasSeleccionables) {
        this.posiciones = posiciones != null ? posiciones : new LinkedHashMap<>();
        this.coloresJugadores = coloresJugadores != null ? coloresJugadores : new HashMap<>();
        this.fichasSeleccionables = fichasSeleccionables != null ? fichasSeleccionables : Collections.emptyList();
        repaint();
    }

    // ==================================================================
    // GEOMETRÍA
    // ==================================================================

    private double u;          // tamaño de la unidad en píxeles
    private double ox, oy;     // esquina superior izquierda del tablero

    private double marco;      // grosor del marco crema

    private void calcularEscala() {
        double total = Math.min(getWidth(), getHeight());
        marco = total * 0.025;
        double lado = total - 2 * marco;
        u = lado / UNIDADES;
        ox = (getWidth() - lado) / 2;
        oy = (getHeight() - lado) / 2;
    }

    /** Celda del brazo de ARRIBA: col 0..2 (izq. a der.), fila 0..7 (borde a centro). */
    private Shape celdaBrazoArriba(int col, int fila) {
        double ancho = 7.0 / 3.0;
        if (fila < 7) {
            return new Rectangle2D.Double(7 + col * ancho, fila, ancho, 1);
        }
        double anchoInterno = 5.0 / 3.0; // la fila 7 se angosta hacia el centro
        Path2D p = new Path2D.Double();
        p.moveTo(7 + col * ancho, 7);
        p.lineTo(7 + (col + 1) * ancho, 7);
        p.lineTo(8 + (col + 1) * anchoInterno, 8);
        p.lineTo(8 + col * anchoInterno, 8);
        p.closePath();
        return p;
    }

    /** Rota una figura (en unidades) del brazo de arriba al brazo indicado y la pasa a píxeles. */
    private Shape enPixeles(Shape enUnidades, int brazo) {
        AffineTransform t = new AffineTransform();
        t.translate(ox, oy);
        t.scale(u, u);
        t.rotate(Math.toRadians(90 * brazo), UNIDADES / 2.0, UNIDADES / 2.0);
        return t.createTransformedShape(enUnidades);
    }

    private Shape celda(int brazo, int col, int fila) {
        return enPixeles(celdaBrazoArriba(col, fila), brazo);
    }

    /** Figura de la casilla 1..68. */
    private Shape casilla(int numero) {
        for (int brazo = 0; brazo < 4; brazo++) {
            int entrada = CASILLA_ENTRADA[brazo];
            for (int fila = 0; fila < 8; fila++) {
                if (numero == normalizar(entrada + 1 + fila)) {
                    return celda(brazo, 0, fila);
                }
                if (numero == normalizar(entrada - 1 - fila)) {
                    return celda(brazo, 2, fila);
                }
            }
            if (numero == entrada) {
                return celda(brazo, 1, 0);
            }
        }
        throw new IllegalArgumentException("Casilla inválida: " + numero);
    }

    /** Casilla k (1..7) del pasillo del color. */
    private Shape casillaPasillo(String color, int k) {
        return celda(brazoDeColor(color), 1, k);
    }

    private Shape meta(String color) {
        Path2D tri = new Path2D.Double();
        tri.moveTo(8, 8);
        tri.lineTo(13, 8);
        tri.lineTo(10.5, 10.5);
        tri.closePath();
        return enPixeles(tri, brazoDeColor(color));
    }

    /** Centro del círculo de salida (esquina) de cada color. */
    private Point2D centroBase(String color) {
        double c1 = 3.5, c2 = 17.5;
        double x, y;
        switch (color) {
            case "ROJO": x = c1; y = c1; break;
            case "AZUL": x = c2; y = c1; break;
            case "AMARILLO": x = c2; y = c2; break;
            default: x = c1; y = c2; break; // VERDE
        }
        return new Point2D.Double(ox + x * u, oy + y * u);
    }

    private static int brazoDeColor(String color) {
        for (int i = 0; i < COLOR_BRAZO.length; i++) {
            if (COLOR_BRAZO[i].equals(color)) {
                return i;
            }
        }
        return 0;
    }

    /** La fila más interna de cada brazo (8 casillas) tiene forma de trapecio. */
    private static boolean esTrapecio(int numero) {
        for (int entrada : CASILLA_ENTRADA) {
            if (numero == normalizar(entrada + 8) || numero == normalizar(entrada - 8)) {
                return true;
            }
        }
        return false;
    }

    private static int normalizar(int n) {
        return ((n - 1) % 68 + 68) % 68 + 1;
    }

    static Color colorAwt(String color) {
        if (color == null) {
            return Color.GRAY;
        }
        switch (color) {
            case "ROJO": return ROJO;
            case "AZUL": return AZUL;
            case "VERDE": return VERDE;
            case "AMARILLO": return AMARILLO;
            default: return Color.GRAY;
        }
    }

    // ==================================================================
    // DIBUJO
    // ==================================================================

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        calcularEscala();
        if (u <= 0) {
            g.dispose();
            return;
        }

        // Marco crema con borde oscuro
        double lado = UNIDADES * u;
        g.setColor(CREMA);
        g.fill(new java.awt.geom.RoundRectangle2D.Double(ox - marco, oy - marco, lado + 2 * marco, lado + 2 * marco, marco, marco));
        g.setColor(new Color(0x42, 0x2F, 0x33));
        g.setStroke(new BasicStroke((float) Math.max(1.5, marco * 0.25)));
        g.draw(new java.awt.geom.RoundRectangle2D.Double(ox - marco, oy - marco, lado + 2 * marco, lado + 2 * marco, marco, marco));
        g.setColor(Color.WHITE);
        g.fill(new Rectangle2D.Double(ox, oy, lado, lado));

        dibujarBases(g);
        dibujarCasillas(g);
        dibujarPasillosYMeta(g);
        g.setColor(new Color(0x42, 0x2F, 0x33));
        g.setStroke(new BasicStroke((float) Math.max(1.5, u * 0.08)));
        g.draw(new Rectangle2D.Double(ox, oy, lado, lado));
        dibujarFichas(g);
        g.dispose();
    }

    /** Esquinas del storyboard: cuadro de color con un cuadro blanco y 4 lugares para las fichas. */
    private void dibujarBases(Graphics2D g) {
        for (String color : COLOR_BRAZO) {
            Point2D c = centroBase(color);
            double x0 = c.getX() - 3.5 * u, y0 = c.getY() - 3.5 * u;
            Rectangle2D esquina = new Rectangle2D.Double(x0, y0, 7 * u, 7 * u);
            g.setColor(colorAwt(color));
            g.fill(esquina);
            g.setColor(new Color(0x42, 0x2F, 0x33));
            g.setStroke(new BasicStroke((float) Math.max(1, u * 0.06)));
            g.draw(esquina);

            java.awt.geom.RoundRectangle2D blanco = new java.awt.geom.RoundRectangle2D.Double(
                    c.getX() - 2.2 * u, c.getY() - 2.2 * u, 4.4 * u, 4.4 * u, u * 0.8, u * 0.8);
            g.setColor(Color.WHITE);
            g.fill(blanco);
            g.setColor(new Color(0x42, 0x2F, 0x33));
            g.draw(blanco);

            // Lugares vacíos (las fichas en base se dibujan encima)
            for (int n = 0; n < 4; n++) {
                Point2D p = lugarEnBase(c, n);
                double r = u * 0.48;
                g.setColor(Color.BLACK);
                g.fill(new Ellipse2D.Double(p.getX() - r, p.getY() - r, 2 * r, 2 * r));
            }
        }
    }

    /** Centro del lugar n (0..3) dentro del cuadro blanco de la esquina. */
    private Point2D lugarEnBase(Point2D centro, int n) {
        double d = 1.1 * u;
        return new Point2D.Double(centro.getX() + ((n % 2 == 0) ? -d : d), centro.getY() + ((n < 2) ? -d : d));
    }

    private void dibujarCasillas(Graphics2D g) {
        g.setFont(getFont().deriveFont(Font.PLAIN, (float) (u * 0.42)));
        for (int n = 1; n <= 68; n++) {
            Shape s = casilla(n);
            String salida = SALIDAS.get(n);
            g.setColor(salida != null ? colorAwt(salida) : Color.WHITE);
            g.fill(s);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1f));
            g.draw(s);

            Rectangle2D b = s.getBounds2D();
            double cx = b.getCenterX(), cy = b.getCenterY();
            if (SEGURAS.contains(n)) {
                // Seguro: puntito gris; salida: puntito blanco con borde
                double r = u * (salida != null ? 0.2 : 0.15);
                Ellipse2D circ = new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r);
                g.setColor(salida != null ? Color.WHITE : new Color(0x88, 0x88, 0x88));
                g.fill(circ);
                g.setColor(new Color(0x55, 0x55, 0x55));
                g.draw(circ);
            }
            if (!MOSTRAR_NUMEROS) {
                continue;
            }
            String txt = String.valueOf(n);
            FontMetrics fm = g.getFontMetrics();
            g.setColor(new Color(0x9A, 0x9A, 0x9A));
            boolean ancha = b.getWidth() > b.getHeight();
            double tx = ancha ? b.getX() + u * 0.15 : cx - fm.stringWidth(txt) / 2.0;
            double ty = ancha ? cy + fm.getAscent() / 2.5 : b.getY() + fm.getAscent() + u * 0.05;
            if (esTrapecio(n)) { // casillas junto al centro: número centrado
                tx = cx - fm.stringWidth(txt) / 2.0;
                ty = cy + fm.getAscent() / 2.5;
            }
            g.drawString(txt, (float) tx, (float) ty);
        }
    }

    private void dibujarPasillosYMeta(Graphics2D g) {
        g.setStroke(new BasicStroke(1f));
        for (String color : COLOR_BRAZO) {
            for (int k = 1; k <= 7; k++) {
                Shape s = casillaPasillo(color, k);
                g.setColor(colorAwt(color));
                g.fill(s);
                g.setColor(Color.BLACK);
                g.draw(s);
            }
            Shape m = meta(color);
            g.setColor(colorAwt(color));
            g.fill(m);
            g.setColor(Color.BLACK);
            g.draw(m);
        }
    }

    private void dibujarFichas(Graphics2D g) {
        fichasDibujadas.clear();

        // Agrupar fichas que comparten lugar para dibujarlas lado a lado
        Map<String, List<String>> porLugar = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : posiciones.entrySet()) {
            String color = coloresJugadores.get(dueno(e.getKey()));
            int pos = e.getValue();
            String lugar = (pos < 0 || pos > 68) ? color + ":" + pos : "C:" + pos;
            if (pos < 0) {
                lugar = e.getKey(); // en base cada ficha tiene su lugar fijo
            }
            porLugar.computeIfAbsent(lugar, x -> new ArrayList<>()).add(e.getKey());
        }

        double r = u * 0.4;
        for (List<String> grupo : porLugar.values()) {
            for (int i = 0; i < grupo.size(); i++) {
                String id = grupo.get(i);
                String color = coloresJugadores.get(dueno(id));
                Point2D p = centroFicha(id, color, posiciones.get(id), i, grupo.size());
                if (p == null) {
                    continue;
                }
                double rf = posiciones.get(id) < 0 ? u * 0.48 : r;
                Ellipse2D circ = new Ellipse2D.Double(p.getX() - rf, p.getY() - rf, 2 * rf, 2 * rf);
                fichasDibujadas.put(id, circ);

                boolean seleccionable = fichasSeleccionables.contains(id);
                if (seleccionable) {
                    double rh = rf * 1.45;
                    g.setColor(new Color(0, 0, 0, 90));
                    g.fill(new Ellipse2D.Double(p.getX() - rh, p.getY() - rh, 2 * rh, 2 * rh));
                }
                g.setColor(colorAwt(color));
                g.fill(circ);
                g.setColor(seleccionable ? Color.WHITE : Color.BLACK);
                g.setStroke(new BasicStroke((float) (seleccionable ? Math.max(2, u * 0.12) : 1.2)));
                g.draw(circ);

                String num = id.substring(id.lastIndexOf('F') + 1);
                g.setFont(getFont().deriveFont(Font.BOLD, (float) (u * 0.45)));
                FontMetrics fm = g.getFontMetrics();
                g.setColor(colorAwt(color).equals(AMARILLO) ? Color.BLACK : Color.WHITE);
                g.drawString(num, (float) (p.getX() - fm.stringWidth(num) / 2.0), (float) (p.getY() + fm.getAscent() / 2.8));
            }
        }
    }

    private Point2D centroFicha(String id, String color, int pos, int indice, int total) {
        if (color == null) {
            return null;
        }
        if (pos < 0) { // en base: su lugar dentro del cuadro blanco
            return lugarEnBase(centroBase(color), Math.floorMod(numeroFicha(id) - 1, 4));
        }
        Shape s;
        if (pos <= 68) {
            s = casilla(pos);
        } else if (pos < 76) {
            s = casillaPasillo(color, pos - 68);
        } else {
            s = meta(color);
        }
        Rectangle2D b = s.getBounds2D();
        double cx = b.getCenterX(), cy = b.getCenterY();
        if (pos == 76) { // centro del triángulo
            int brazo = brazoDeColor(color);
            double dc = 1.2 * u;
            double[][] dir = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
            cx = ox + 10.5 * u + dir[brazo][0] * dc;
            cy = oy + 10.5 * u + dir[brazo][1] * dc;
        }
        if (total > 1) {
            boolean ancha = b.getWidth() >= b.getHeight();
            double paso = u * 0.85;
            double desp = (indice - (total - 1) / 2.0) * paso;
            if (ancha) {
                cx += desp;
            } else {
                cy += desp;
            }
        }
        return new Point2D.Double(cx, cy);
    }

    private static String dueno(String idFicha) {
        int i = idFicha.lastIndexOf("_F");
        return i > 0 ? idFicha.substring(0, i) : idFicha;
    }

    private static int numeroFicha(String idFicha) {
        try {
            return Integer.parseInt(idFicha.substring(idFicha.lastIndexOf("_F") + 2));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
