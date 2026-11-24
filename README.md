# Parchis-Equipo4
Implementación del juego de Parchís en Java para la materia de Arquitectura de Software.
Arquitectura: **Capas** + **Igual a Igual (P2P) con Host autoritativo**. Interfaz con **MVC clásico** (Observer).

## Cómo ejecutarlo

Todos los peers ejecutan el mismo programa (`Parchis.jar`, se genera con `mvn package` en `target/`).

| Modo | Argumentos | Qué hace |
|---|---|---|
| Menú | *(ninguno)* o `menu` | Muestra Inicio; permite crear una sala o unirse con la IP y el puerto del Host. |
| Demo local | `demo [jugadores] [puerto]` | Abre el Host y los clientes en este equipo. Ej.: `demo 2` abre 2 ventanas; inicia la partida desde el lobby del Host. |
| Host | `host [capacidad] [puerto]` | Abre una sala (2 a 4 jugadores), juega como J1 y espera conexiones TCP. |
| Cliente | `cliente <idJugador> [ipHost] [puerto]` | Se registra en la sala del Host. Ej.: `cliente J2 192.168.1.10 5000` |

En NetBeans los argumentos se ponen en *Project Properties → Run → Arguments*. Si no se especifica puerto, se usa 5000.

## Capas (diagrama "1. Componentes - arquitectura en capas")

| Paquete | Clases | Depende de |
|---|---|---|
| `Presentacion` | `PantallaPartida` «view», `PanelTablero`, `ControladorJuego` «controller», `ModeloPresentacion` «model», `ObservadorModelo`, `DatosVista`, las demás pantallas (ver abajo) | Aplicacion |
| `Aplicacion` | `GestorJuego`, `IPresentacionJuego`, `EstadoPartidaDTO`, `TipoAccion` | Dominio, Infraestructura |
| `Dominio` | `IFachadaPartida`, `FachadaPartida` «facade», `ResultadoAccion`, `Juego`, `Turno`, `Tablero`, `Casilla`, `Ficha`, `Jugador`, `Dado`, `ReglasMovimiento`, `AccionPendiente`, ... | — |
| `Infraestructura` | `IGestorRed`, `GestorRed`, `ServidorTCP`, `ClienteTCP`, `ConexionPeer`, `ManejadorConexion`, `INotificadorRed`, `MensajeRed` y mensajes | — |

`IPresentacionJuego` vive en Aplicación y la implementa `ControladorJuego`. Así el GestorJuego le entrega
estados y rechazos al controlador sin que Aplicación dependa de Presentación.

## Pantallas (storyboard)
Todas las pantallas del storyboard ya existen **sin lógica**: cada CU conecta sus botones
(`getBotonX().addActionListener(...)`) y llama a los métodos para mostrar datos.

| Pantalla | Clase | Lo usa el CU |
|---|---|---|
| Inicio | `PantallaInicio` | todos (navegación) |
| Configuración de la partida | `PantallaConfiguracionPartida` | Configurar / crear partida |
| Unirse a partida | `PantallaUnirsePartida` (+ `TarjetaPartida`) | Unirse a partida |
| Personalizar nombre / avatar / color | `PantallaPersonalizarNombre`, `PantallaPersonalizarAvatar`, `PantallaPersonalizarColor` | Personalizar |
| Lobby | `PantallaLobby` (+ `TarjetaJugadorLobby`, `Notificacion`) | Unirse / Iniciar partida |
| Partida en curso | `PantallaPartida` (con lógica de Ejecutar turno) | Ejecutar turno |
| Resultados | `PantallaResultados` | Finalizar partida |

- `VentanaParchis.mostrar(pantalla)` cambia la pantalla que se ve.
- Para verlas sin jugar: ejecutar `com.mycompany.parchis.VistaPreviaPantallas` (clic derecho → Run File).
- Los componentes reutilizables están en `Presentacion.componentes` y los colores/fuentes en `Presentacion.estilo.Estilo`.
- Todo se dibuja en coordenadas del storyboard (1200 × 675) y se escala con la ventana.
- **Avatares:** por ahora son dibujos de ejemplo. Para usar imágenes, poner `avatar1.png` ... `avatar12.png` en
  `src/main/resources/avatares/` (cuadradas; se recortan en círculo solas).
- **Fuentes:** Lilita One (títulos) y Roboto (texto), libres (licencia OFL), en `src/main/resources/fuentes/`.

## Estado de la red
`Infraestructura.GestorRed` implementa la conexión TCP en estrella con las clases `ServidorTCP`, `ClienteTCP`, `ConexionPeer` y `ManejadorConexion`. El cliente se conecta con IP/puerto y envía `MensajeSolicitudUnion(idPartida, idJugador)`. El Host valida disponibilidad/capacidad con `IFachadaPartida`, registra el jugador y responde con `MensajeRespuestaUnion(aceptada, motivo)`.

El menú solicita la IP y el puerto del Host; no hay descubrimiento automático de salas. El modo `demo` también usa TCP real sobre `localhost`, igual que dos equipos separados.

## P2P (diagrama "2. Componentes - P2P")
- Estrella: el Host abre un `ServerSocket`; hasta 3 peers se conectan a él por TCP.
- `ControlUnirsePartida` procesa los mensajes de unión y delega los mensajes del juego a `GestorJuego`; `GestorRed` implementa `IGestorRed`.
- Solo el Host tiene `FachadaPartida` (estado oficial). Los demás guardan la última instantánea en su `ModeloPresentacion`.
- El Host admite hasta 4 jugadores en total; el cliente ingresa la IP y el puerto, y recibe el estado inicial o el motivo del rechazo.
- Al aceptar el registro, el Host envía el estado actual de la sala al nuevo peer.

## Flujo del CU Ejecutar turno
**Diagrama 3 (Petición):** clic → `PantallaPartida` → `ControladorJuego.solicitarAccion` → `ModeloPresentacion.validarAccion`
(si falla: `registrarAviso`) → `GestorJuego.solicitarAccion` → crea `Solicitud(idSolicitud, versión)` → `GestorRed.transmitirEvento` → Host.

**Diagrama 4 (Validación y difusión):** `GestorRed Host` → `GestorJuego.solicitudRecibida` → deduplica `idSolicitud` →
`FachadaPartida.procesarAccion` (turno, versión, reglas; versión + 1) →
- aceptada: `GestorRed.transmitirEvento(ConfirmacionAccion(estado, versión))` → cada peer: si la versión es más nueva →
  `ControladorJuego.actualizarEstado` → `ModeloPresentacion.reemplazarEstado` → `notificarCambio` → la vista llama `obtenerDatosVista`.
- rechazada: `GestorRed.transmitirEvento(RechazoAccion)` → `GestorJuego.rechazoRecibido` → `ControladorJuego.notificarRechazo`
  → `ModeloPresentacion.registrarAviso` → `notificarAviso`.

Cuando juega el propio Host, su GestorJuego procesa la solicitud localmente (sin red) y difunde igual.

## Tablero (Fig. 1 del documento del juego)
- Casillas 1–68. Salidas: amarillo 5, azul 22, rojo 39, verde 56.
- Seguras: 5, 12, 17, 22, 29, 34, 39, 46, 51, 56, 63, 68.
- Cada ficha avanza 63 casillas desde su salida hasta su entrada (amarillo 68, azul 17, rojo 34, verde 51), recorre su pasillo de 7 casillas y llega a la meta (rebota si se pasa).
- Posiciones en `obtenerPosiciones()`: `-1` base · `1..68` casilla · `69..75` pasillo · `76` meta.
