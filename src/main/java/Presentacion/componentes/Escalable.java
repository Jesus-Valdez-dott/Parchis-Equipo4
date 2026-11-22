package Presentacion.componentes;

/**
 * Componente que ajusta su letra al tamaño de la ventana.
 * LayoutEscalado le avisa la escala actual (1.0 = tamaño del storyboard).
 *
 * @author Equipo 4
 */
public interface Escalable {

    void escalar(double escala);
}
