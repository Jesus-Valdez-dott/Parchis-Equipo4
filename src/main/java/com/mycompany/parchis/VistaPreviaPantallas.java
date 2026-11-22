package com.mycompany.parchis;

import Presentacion.PantallaConfiguracionPartida;
import Presentacion.PantallaInicio;
import Presentacion.PantallaLobby;
import Presentacion.PantallaPersonalizarAvatar;
import Presentacion.PantallaPersonalizarColor;
import Presentacion.PantallaPersonalizarNombre;
import Presentacion.PantallaResultados;
import Presentacion.PantallaUnirsePartida;
import Presentacion.VentanaParchis;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * HERRAMIENTA DE DESARROLLO (no es parte del juego): muestra cada pantalla del
 * storyboard con datos de ejemplo, para verlas sin tener que jugar.
 * Ejecutar esta clase directamente (clic derecho -> Run File).
 *
 * @author Equipo 4
 */
public class VistaPreviaPantallas {

    public static final String[] PANTALLAS = {
        "Inicio", "Configuracion", "Unirse", "Personalizar nombre", "Personalizar avatar",
        "Personalizar color", "Lobby", "Partida", "Resultados"
    };

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaParchis ventana = new VentanaParchis("Parchís - Vista previa de pantallas");
            JPanel contenedor = new JPanel(new BorderLayout());
            JComboBox<String> selector = new JComboBox<>(PANTALLAS);
            JPanel barra = new JPanel();
            barra.setBackground(new Color(0x15, 0x01, 0x18));
            JLabel etiqueta = new JLabel("Pantalla:");
            etiqueta.setForeground(Color.WHITE);
            barra.add(etiqueta);
            barra.add(selector);
            contenedor.add(barra, BorderLayout.NORTH);
            contenedor.add(crearEjemplo(PANTALLAS[0]), BorderLayout.CENTER);
            selector.addActionListener(e -> {
                BorderLayout bl = (BorderLayout) contenedor.getLayout();
                contenedor.remove(bl.getLayoutComponent(BorderLayout.CENTER));
                contenedor.add(crearEjemplo((String) selector.getSelectedItem()), BorderLayout.CENTER);
                contenedor.revalidate();
                contenedor.repaint();
            });
            ventana.mostrar(contenedor);
            ventana.setVisible(true);
        });
    }

    /** Crea la pantalla indicada con los datos de ejemplo del storyboard. */
    public static JComponent crearEjemplo(String nombre) {
        switch (nombre) {
            case "Configuracion":
                return new PantallaConfiguracionPartida();
            case "Unirse": {
                PantallaUnirsePartida p = new PantallaUnirsePartida();
                p.agregarPartida("LunaVioleta", 1, 4, 4, 5, 3);
                return p;
            }
            case "Personalizar nombre": {
                PantallaPersonalizarNombre p = new PantallaPersonalizarNombre();
                p.getCampoNombre().setText("GamerRey_99");
                return p;
            }
            case "Personalizar avatar": {
                PantallaPersonalizarAvatar p = new PantallaPersonalizarAvatar();
                p.getAvatares()[0].setMarca("1");
                return p;
            }
            case "Personalizar color": {
                PantallaPersonalizarColor p = new PantallaPersonalizarColor();
                p.getCirculosColor()[0].setMarca("1");
                return p;
            }
            case "Lobby": {
                PantallaLobby p = new PantallaLobby();
                p.getLugar(0).setJugador(1, "Sabrosuras3000", 7, true);
                p.getLugar(1).setJugador(2, "Patricio", 3, true);
                p.getNotificacion().mostrar(1, "Mr. Aura quiere unirse a tu partida", "Aceptar", "Rechazar");
                return p;
            }
            case "Partida":
                return PartidaDeEjemplo.crear();
            case "Resultados": {
                PantallaResultados p = new PantallaResultados();
                p.setGanador("Sabrosuras3000", 7);
                p.setLugar(2, "Devorador_de_galaxias", 4, 35);
                p.setLugar(3, "Patricio", 3, 67);
                p.setLugar(4, "Mr.Aura", 1, 100);
                return p;
            }
            default:
                return new PantallaInicio();
        }
    }
}
