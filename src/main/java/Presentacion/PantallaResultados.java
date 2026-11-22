package Presentacion;

import Presentacion.componentes.Avatar;
import Presentacion.componentes.Boton;
import Presentacion.componentes.Etiqueta;
import Presentacion.componentes.Icono;
import Presentacion.componentes.PanelDiseno;
import Presentacion.componentes.TituloDorado;
import Presentacion.estilo.Estilo;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * Pantalla con los resultados al finalizar la partida (storyboard: "Pantalla con los resultados...").
 * SIN lógica: muestra al ganador y hasta 3 lugares más.
 *
 * @author Equipo 4
 */
public class PantallaResultados extends PanelDiseno {

    private static final long serialVersionUID = 1L;

    private static final int X = 427, ANCHO_COL = 358;
    private static final int[] Y_FILAS = {380, 443, 506};

    private final Avatar avatarGanador = new Avatar(0);
    private final Etiqueta nombreGanador = new Etiqueta("", Etiqueta.Tipo.NEGRITA, 15, Color.BLACK);
    private final Icono[] medallas = new Icono[3];
    private final Avatar[] avatares = new Avatar[3];
    private final Etiqueta[] nombres = new Etiqueta[3];
    private final Etiqueta[] restantes = new Etiqueta[3];
    private final Boton continuar = new Boton("Continuar", Estilo.AZUL, Color.BLACK, 10, 10.5f, true);

    public PantallaResultados() {
        agregar(new TituloDorado("RESULTADOS", 120), 230, 0, 740, 165);
        agregar(new Etiqueta("Bien Jugado!", Etiqueta.Tipo.NEGRITA, 40, Color.BLACK), X, 170, ANCHO_COL, 52);
        agregar(new Icono(Icono.Tipo.CORONA, null), 565, 222, 84, 70);
        agregar(avatarGanador, 576, 283, 62, 62);
        agregar(nombreGanador, X, 352, ANCHO_COL, 22);
        for (int i = 0; i < 3; i++) {
            int y = Y_FILAS[i];
            medallas[i] = agregar(Icono.medalla(i + 2), 424, y + 6, 40, 50);
            avatares[i] = agregar(new Avatar(0), 461, y + 1, 60, 60);
            nombres[i] = agregar(new Etiqueta("", Etiqueta.Tipo.NEGRITA, 14.5f, Color.BLACK, SwingConstants.LEFT), 526, y + 18, 200, 24);
            restantes[i] = agregar(new Etiqueta("", Etiqueta.Tipo.NEGRITA, 9.5f, Color.BLACK), 718, y + 14, 75, 32);
        }
        continuar.setBrillo(Estilo.AZUL);
        agregar(continuar, 553, 584, 106, 34);
    }

    public void setGanador(String nombre, int indiceAvatar) {
        nombreGanador.setText(nombre);
        avatarGanador.setIndice(indiceAvatar);
    }

    /** Fila del lugar 2, 3 o 4. */
    public void setLugar(int lugar, String nombre, int indiceAvatar, int casillasRestantes) {
        int i = lugar - 2;
        medallas[i].setNumero(lugar);
        avatares[i].setIndice(indiceAvatar);
        nombres[i].setText(nombre);
        restantes[i].setText("<html><center>" + casillasRestantes + " casillas<br>rest.</center></html>");
        mostrarFila(i, true);
    }

    /** Oculta la fila del lugar 2, 3 o 4 (cuando jugaron menos de 4). */
    public void ocultarLugar(int lugar) {
        mostrarFila(lugar - 2, false);
    }

    private void mostrarFila(int i, boolean visible) {
        medallas[i].setVisible(visible);
        avatares[i].setVisible(visible);
        nombres[i].setVisible(visible);
        restantes[i].setVisible(visible);
    }

    public JButton getBotonContinuar() { return continuar; }

    /** Columna tan con líneas negras entre filas. */
    @Override
    protected void pintarFondo(Graphics2D g) {
        g.setColor(Estilo.TAN);
        g.fill(new Rectangle2D.Double(X, 167, ANCHO_COL, 459));
        g.setColor(new Color(0x14, 0x0A, 0x10));
        for (int y : new int[]{377, 440, 503, 566}) {
            g.fill(new Rectangle2D.Double(X, y, ANCHO_COL, 3.5));
        }
    }
}
