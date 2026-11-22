# Parchis-Equipo4
Implementación del juego de Parchís en Java para la materia de Arquitectura de Software.
Arquitectura: **Capas** + **Igual a Igual (P2P) con Host autoritativo**. Interfaz con **MVC clásico** (Observer).

## Cómo ejecutarlo

Todos los peers ejecutan el mismo programa (`Parchis.jar`, se genera con `mvn package` en `target/`).
Por ahora solo funciona el modo demo (ver *Estado de la red*).

| Modo | Argumentos | Qué hace |
|---|---|---|
| Demo (por defecto) | *(ninguno)* o `demo [jugadores] [puerto]` | Abre el Host y los clientes en el mismo equipo (requerimiento 2). Por defecto 3 jugadores, empezando en el Lobby. Con `demo 4` la partida inicia sola. |
| Host | `host [jugadores] [puerto]` | Este equipo tiene el estado oficial y juega como J1. |
| Cliente | `cliente <idJugador> [ipHost] [puerto]` | Se une al Host. Ej.: `cliente J2 192.168.1.10 5000` |

En NetBeans los argumentos se ponen en *Project Properties → Run → Arguments*.
Puerto por defecto: 5000.

## Capas (diagrama "1. Componentes - arquitectura en capas")

| Paquete | Clases | Depende de |
|---|---|---|
| `Presentacion` | `PantallaPartida` y `PantallaLobby` «view», `PanelTablero`, `ControladorJuego` «controller», `ModeloPresentacion` «model», `ObservadorModelo`, `DatosVista`, `NavegadorPantallas`, las demás pantallas (ver abajo) | Aplicacion |
| `Aplicacion` | `GestorJuego`, `IPresentacionJuego`, `EstadoPartidaDTO`, `TipoAccion` | Dominio, Infraestructura |
| `Dominio` | `IFachadaPartida`, `FachadaPartida` «facade», `ResultadoAccion`, `Juego`, `Turno`, `Tablero`, `Casilla`, `Ficha`, `Jugador`, `Dado`, `ReglasMovimiento`, `AccionPendiente`, `SolicitudInicio`, ... | — |
| `Infraestructura` | `IGestorRed`, `INotificadorRed`, `MensajeRed` + `Solicitud`, `ConfirmacionAccion`, `RechazoAccion` (**`GestorRed` pendiente**) | — |

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
| Lobby | `PantallaLobby` (+ `TarjetaJugadorLobby`, `Notificacion`), con lógica de Iniciar partida | Unirse / Iniciar partida |
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
`Infraestructura.GestorRed` (sockets TCP) está pendiente. Mientras tanto, `com.mycompany.parchis.RedLocalSimulada`
implementa `IGestorRed` en memoria para poder probar en modo demo. Cuando `GestorRed` esté listo:
1. Que implemente `IGestorRed` (y entregue lo recibido con `INotificadorRed`).
2. En `Parchis.crearRed()` cambiar `new RedLocalSimulada()` por `new Infraestructura.GestorRed()`.
3. Borrar `RedLocalSimulada.java`.

Las interfaces y los mensajes salen de los diagramas y se pueden ajustar si hace falta.

## P2P (diagrama "2. Componentes - P2P")
- Estrella: el Host abre un `ServerSocket`; hasta 3 peers se conectan a él por TCP.
- `GestorJuego` implementa `INotificadorRed`; `GestorRed` implementa `IGestorRed`.
- Solo el Host tiene `FachadaPartida` (estado oficial). Los demás guardan la última instantánea en su `ModeloPresentacion`.
- Un peer que se conecta tarde recibe el último estado difundido.

## Flujo del CU Ejecutar turno
**Diagrama 3 (Petición):** clic → `PantallaPartida` → `ControladorJuego.solicitarAccion` → `ModeloPresentacion.validarAccion`
(si falla: `registrarAviso`) → `GestorJuego.solicitarAccion` → crea `Solicitud(idSolicitud, versión)` → `GestorRed.enviar` → Host.

**Diagrama 4 (Validación y difusión):** `GestorRed Host` → `GestorJuego.solicitudRecibida` → deduplica `idSolicitud` →
`FachadaPartida.procesarAccion` (turno, versión, reglas; versión + 1) →
- aceptada: `GestorRed.difundir(ConfirmacionAccion(estado, versión))` → cada peer: si la versión es más nueva →
  `ControladorJuego.actualizarEstado` → `ModeloPresentacion.reemplazarEstado` → `notificarCambio` → la vista llama `obtenerDatosVista`.
- rechazada: `GestorRed.responderAlSolicitante(RechazoAccion)` → `GestorJuego.rechazoRecibido` → `ControladorJuego.notificarRechazo`
  → `ModeloPresentacion.registrarAviso` → `notificarAviso`.

Cuando juega el propio Host, su GestorJuego procesa la solicitud localmente (sin red) y difunde igual.

## Flujo del CU Iniciar partida
Usa el mismo camino P2P que Ejecutar turno, con dos acciones nuevas: `SOLICITAR_INICIO` y `CONFIRMAR_INICIO`.

**Reglas (en `Dominio.Juego`):**
- Cualquier inscrito puede pedir el inicio si hay al menos 2. Queda una `SolicitudInicio` y el solicitante cuenta como confirmado.
- Los demás inscritos tienen que confirmar. No hay botón de rechazar: la solicitud solo espera.
- Cuando todos confirman, la partida pasa a `EN_CURSO`.
- Con 4 inscritos la partida inicia sola.
- Si alguien se une mientras hay una solicitud, se reinician las confirmaciones (solo queda confirmado el solicitante).
- Los turnos van en orden de entrada: el Host primero, luego el segundo en unirse, y así (`asignarTurnosPorOrdenDeEntrada`).

**Flujo:**
- **SD-01 (petición):** INICIAR PARTIDA → `PantallaLobby.solicitarInicio` → `ControladorJuego.solicitarAccion(SOLICITAR_INICIO)` →
  `ModeloPresentacion.validarAccion` → `GestorJuego` → `Solicitud` al Host.
- **SD-02 (Host):** `FachadaPartida.procesarAccion` → `Juego.validarSolicitudInicio` / `solicitarInicio`.
  - Si se acepta: se difunde el estado, que trae `solicitanteInicio` y `jugadoresConfirmados`.
  - Si se rechaza: le llega un `RechazoAccion` solo al solicitante (p. ej. si dos lo pidieron a la vez).
- **SD-03 (confirmar e iniciar):** botón "Confirmar" del aviso → `CONFIRMAR_INICIO` → `Juego.confirmarInicio`.
  - Si ya confirmaron todos, `iniciarPartida()`.
  - Cada peer recibe el estado nuevo, y `ControladorJuego.actualizarEstado` ve el cambio a `EN_CURSO` y llama a `NavegadorPantallas.mostrarPartida()`.
- **SD-04 (alguien se une):** `FachadaPartida.inscribirJugador` → `Juego.agregarJugador`, que reinicia las confirmaciones o inicia con 4 jugadores.

**Lobby:**
- Muestra a los inscritos en orden de entrada, con "LISTO" en quienes ya confirmaron.
- INICIAR PARTIDA solo se habilita si se puede pedir el inicio.
- A quien falta confirmar le aparece el aviso "X quiere iniciar la partida" con "Confirmar".
- Abajo se dice de quién falta la confirmación.

**Para el CU Unirse a partida:**
- Por ahora los jugadores se inscriben en `Parchis.crearHost`. Al unirse alguien hay que llamar a `FachadaPartida.inscribirJugador(...)` y luego
  difundir el estado. `GestorJuego.difundirEstadoOficial()` sigue privado, así que habrá que exponer un método para eso.
- El aviso `Notificacion` del Lobby es compartido. `PantallaLobby` cambia su texto y su acción según lo que esté pasando.

## Pruebas
- Las pruebas no están en `src/test` todavía.
- Lo que se comprobó en este CU:
  - 29 reglas del dominio.
  - 15 casos P2P entre 3 peers: solicitar, confirmar, iniciar, carrera de dos solicitudes e inicio automático con 4.
  - El flujo con clics en 3 ventanas: las tres cambian a la partida.
  - Las pruebas anteriores de Ejecutar turno siguen pasando.

## Tablero (Fig. 1 del documento del juego)
- Casillas 1–68. Salidas: amarillo 5, azul 22, rojo 39, verde 56.
- Seguras: 5, 12, 17, 22, 29, 34, 39, 46, 51, 56, 63, 68.
- Cada ficha avanza 63 casillas desde su salida hasta su entrada (amarillo 68, azul 17, rojo 34, verde 51), recorre su pasillo de 7 casillas y llega a la meta (rebota si se pasa).
- Posiciones en `obtenerPosiciones()`: `-1` base · `1..68` casilla · `69..75` pasillo · `76` meta.
