package Presentacion;

import Presentacion.componentes.CampoTextoPill;
import Presentacion.componentes.Tarjeta;
import Presentacion.estilo.Estilo;
import javax.swing.JTextField;

/**
 * Pantalla para configurar el nombre (storyboard: "Pantalla para configurar el nombre").
 * SIN lógica.
 *
 * @author Equipo 4
 */
public class PantallaPersonalizarNombre extends PantallaPersonalizacion {

    private static final long serialVersionUID = 1L;

    private final CampoTextoPill campoNombre = new CampoTextoPill("", 24);

    public PantallaPersonalizarNombre() {
        super("NOMBRE", 70);
        Tarjeta marco = new Tarjeta(397, 116, Estilo.TAN, 14).conBorde(Estilo.ROSA, false).conBrillo(Estilo.ROSA);
        marco.agregar(campoNombre, 24, 26, 350, 64);
        agregar(marco, 450, 231, 397, 116);
    }

    public JTextField getCampoNombre() { return campoNombre; }
}
