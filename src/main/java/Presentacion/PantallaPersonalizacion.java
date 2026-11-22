package Presentacion;

import Presentacion.componentes.Boton;
import Presentacion.componentes.PanelDiseno;
import Presentacion.componentes.TituloDorado;
import Presentacion.estilo.Estilo;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JButton;

/**
 * Base de las tres pantallas de personalización (Nombre, Avatar, Color):
 * barra lateral con sus botones, título y botones Cancelar / Guardar.
 * SIN lógica.
 *
 * @author Equipo 4
 */
public abstract class PantallaPersonalizacion extends PanelDiseno {

    private static final long serialVersionUID = 1L;

    private final Boton nombre = Boton.tan("Nombre", 6, 14);
    private final Boton avatar = Boton.tan("Avatar", 6, 14);
    private final Boton color = Boton.tan("Color", 6, 14);
    private final Boton cancelar = new Boton("Cancelar", Estilo.ROJO, new Color(0x40, 0x00, 0x00), 3, 10, false);
    private final Boton guardar = new Boton("Guardar", Estilo.VERDE, new Color(0x00, 0x30, 0x10), 3, 10, false);

    protected PantallaPersonalizacion(String titulo, float tamanoTitulo) {
        agregar(new TituloDorado("Personalizacion", 22), 15, 75, 175, 45);
        agregar(nombre, 45, 170, 114, 60);
        agregar(avatar, 45, 266, 103, 60);
        agregar(color, 45, 372, 96, 60);
        agregar(new TituloDorado(titulo, tamanoTitulo), 440, 0, 500, 110);
        agregar(cancelar, 994, 623, 84, 41);
        agregar(guardar, 1101, 623, 84, 41);
    }

    /** Línea vertical tan que separa la barra lateral. */
    @Override
    protected void pintarFondo(Graphics2D g) {
        g.setColor(Estilo.TAN);
        g.fill(new Rectangle2D.Double(200, 0, 6, ALTO));
    }

    public JButton getBotonNombre() { return nombre; }
    public JButton getBotonAvatar() { return avatar; }
    public JButton getBotonColor() { return color; }
    public JButton getBotonCancelar() { return cancelar; }
    public JButton getBotonGuardar() { return guardar; }
}
