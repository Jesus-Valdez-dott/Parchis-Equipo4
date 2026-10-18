/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;

import Controlador.IController;
import Modelo.Fachada.IModeloLectura;
import Modelo.Fachada.IObservadorPartida;
import javax.swing.*;
import java.util.List;

/**
 *
 * @author Jesús
 */
public class PantallaPartida extends JFrame implements IObservadorPartida{
    // La vista solo conoce la versión de lectura del modelo
    private IModeloLectura modeloLectura; 
    
    // La vista conoce la abstracción del controlador, no la implementación concreta
    private IController controlador;

    // Supongamos que esta vista representa a un jugador específico en su computadora
    private String miIdJugador;

    public PantallaPartida(IModeloLectura modeloLectura, IController controlador, String miIdJugador) {
        this.modeloLectura = modeloLectura;
        this.controlador = controlador;
        this.miIdJugador = miIdJugador;

        setTitle("Parchís - Jugador: " + miIdJugador);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        // En una implementación real, aquí construirías la UI:
        // - Un botón para "Lanzar Dado" que llame a solicitarLanzamiento()
        // - Listeners de clics en el tablero que llamen a seleccionaFicha(idFicha)
    }

    // ==========================================
    // MÉTODOS DE INTERACCIÓN DEL USUARIO
    // ==========================================

    public void solicitarLanzamiento() {
        // La vista NO lanza el dado directamente. Le pide al controlador que lo intente.
        controlador.ejecutarLanzamiento(miIdJugador);
    }

    public void seleccionaFicha(String idFicha) {
        // La vista verifica visualmente si la ficha se puede mover antes de pedirlo
        List<String> movibles = modeloLectura.obtenerFichasMovibles();
        
        if (movibles.contains(idFicha)) {
            // Si es válida, le pide al controlador que procese el movimiento
            controlador.procesarMovimiento(miIdJugador, idFicha);
        } else {
            mostrarError("Esa ficha no se puede mover en este turno.");
        }
    }

    // ==========================================
    // MÉTODOS DE ACTUALIZACIÓN (Patrón Observer)
    // ==========================================

    @Override
    public void actualizar() {
        // Este método es llamado por la Fachada cuando el modelo cambia.
        // La vista vuelve a consultar el modelo (solo lectura) y redibuja la pantalla.
        
        System.out.println("El modelo ha cambiado. Actualizando la vista...");
        
        int valorDado = modeloLectura.obtenerValorDado();
        String jugadorActual = modeloLectura.obtenerJugadorActual();
        
        mostrarResultadoDado(valorDado);
        actualizarTablero();
        habilitarFichasMovibles();

        // En una UI real de Swing, aquí llamarías a repaint() o revalidate()
    }

    // ==========================================
    // MÉTODOS DE DIBUJO Y RENDERIZADO
    // ==========================================

    public void mostrarResultadoDado(int valor) {
        // Lógica para actualizar la imagen o texto del dado en la pantalla
        System.out.println("Dado actual: " + valor);
    }

    public void actualizarTablero() {
        // Lógica para repintar las fichas en sus nuevas posiciones
        // Se usaría modeloLectura.obtenerPosiciones() para saber dónde dibujar
        System.out.println("Redibujando el tablero...");
    }

    public void habilitarFichasMovibles() {
        // Lógica para resaltar o hacer clicables solo las fichas que el jugador puede mover
        String turnoActual = modeloLectura.obtenerJugadorActual();
        
        if (miIdJugador.equals(turnoActual)) {
             List<String> movibles = modeloLectura.obtenerFichasMovibles();
             System.out.println("Es tu turno. Puedes mover: " + movibles);
             // Código para activar eventos de clic solo en las fichas 'movibles'
        } else {
             System.out.println("Esperando el turno de " + turnoActual + "...");
        }
    }

    public void mostrarError(String mensaje) {
        // Usamos un simple cuadro de diálogo de Swing para mostrar errores
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}
