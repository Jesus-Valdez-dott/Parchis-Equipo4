package Presentacion.componentes;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

/**
 * Coloca cada componente en coordenadas de "diseño" (las del storyboard) y lo
 * escala proporcionalmente al tamaño real del contenedor, centrando el contenido.
 *
 * Uso: panel.setLayout(new LayoutEscalado(1200, 675));
 *      panel.add(boton, new Rectangle(190, 262, 163, 82));
 *
 * @author Equipo 4
 */
public class LayoutEscalado implements LayoutManager2 {

    private final int anchoDiseno;
    private final int altoDiseno;
    private final Map<Component, Rectangle> posiciones = new HashMap<>();
    private double escala = 1;
    private double origenX;
    private double origenY;

    public LayoutEscalado(int anchoDiseno, int altoDiseno) {
        this.anchoDiseno = anchoDiseno;
        this.altoDiseno = altoDiseno;
    }

    public double getEscala() { return escala; }
    public double getOrigenX() { return origenX; }
    public double getOrigenY() { return origenY; }
    public int getAnchoDiseno() { return anchoDiseno; }
    public int getAltoDiseno() { return altoDiseno; }

    /** Cambia la posición (en coordenadas de diseño) de un componente ya agregado. */
    public void mover(Component c, Rectangle r) {
        posiciones.put(c, new Rectangle(r));
    }

    @Override
    public void addLayoutComponent(Component comp, Object restriccion) {
        if (restriccion instanceof Rectangle r) {
            posiciones.put(comp, new Rectangle(r));
        }
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        // solo se usan restricciones Rectangle
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        posiciones.remove(comp);
    }

    @Override
    public void layoutContainer(Container padre) {
        Insets in = padre.getInsets();
        double w = padre.getWidth() - in.left - in.right;
        double h = padre.getHeight() - in.top - in.bottom;
        escala = Math.min(w / anchoDiseno, h / altoDiseno);
        origenX = in.left + (w - anchoDiseno * escala) / 2;
        origenY = in.top + (h - altoDiseno * escala) / 2;
        for (Component c : padre.getComponents()) {
            Rectangle r = posiciones.get(c);
            if (r == null) {
                continue;
            }
            int x = (int) Math.round(origenX + r.x * escala);
            int y = (int) Math.round(origenY + r.y * escala);
            int x2 = (int) Math.round(origenX + (r.x + r.width) * escala);
            int y2 = (int) Math.round(origenY + (r.y + r.height) * escala);
            c.setBounds(x, y, Math.max(1, x2 - x), Math.max(1, y2 - y));
            if (c instanceof Escalable e) {
                e.escalar(escala);
            }
        }
    }

    @Override
    public Dimension preferredLayoutSize(Container padre) {
        return new Dimension(anchoDiseno, altoDiseno);
    }

    @Override
    public Dimension minimumLayoutSize(Container padre) {
        return new Dimension(anchoDiseno / 4, altoDiseno / 4);
    }

    @Override
    public Dimension maximumLayoutSize(Container padre) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public float getLayoutAlignmentX(Container padre) {
        return 0.5f;
    }

    @Override
    public float getLayoutAlignmentY(Container padre) {
        return 0.5f;
    }

    @Override
    public void invalidateLayout(Container padre) {
        // sin caché
    }
}
