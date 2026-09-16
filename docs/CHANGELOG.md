## 2026-09-14 — Revisión integral de funcionamiento

- Login: correo de una sola línea, desplazamiento horizontal, teclado Email sin autocorrección y bloqueo del botón mientras Firebase autentica.
- Registro: ahora crea realmente la cuenta con Firebase Auth y guarda el perfil en Firestore antes de navegar al editor de avatar; se añadió nombre de usuario y estado de carga.
- Navegación: se evita entrar a la app sin autenticación y el cierre de sesión también cierra Firebase Auth desde Perfil.
- Chat: el ID de conversación ahora es determinista para que ambos participantes compartan el mismo chat; se eliminó el fallback ficticio `u1`.
- 3 en Raya: se usa el rival de la conversación seleccionada y solo puede jugar el usuario cuyo turno corresponde.
- Avatar: se corrigió la composición de capas, escala del cabello y alineación; Guardar Avatar ahora persiste `Avatar3DConfig` en Firestore.
- Historias: las fotos se suben realmente a Firebase Storage; si falla la subida ya no se publica un marcador falso. El feed muestra la URL real mediante Coil.
- Firebase: se añadieron `firestore.rules`, `storage.rules` y `firebase.json` para documentar el despliegue de reglas.

# Changelog — Auditoría y expansión de Glowink

Registro de todo lo que se revisó, corrigió y añadió en esta pasada sobre el proyecto.
Organizado por tema. Cada punto dice **qué había antes**, **qué se hizo** y **dónde**.

---

## 1. Bugs reales corregidos

### 1.1 Texto invisible en burbujas de chat propias (crítico)
- **Antes:** `ChatScreen.kt` pintaba el texto del emisor con `OnSecondaryText` (`#0A0714`,
  casi negro), un color pensado para texto sobre superficies **sólidas** de color lima. Las
  burbujas de chat, en cambio, son de cristal (glassmorphism) sobre el fondo oscuro de la
  app — el resultado era texto prácticamente invisible.
- **Además:** `Color.kt` ya tenía definidos `GlassBubbleSelfGradient` y
  `GlassBubbleOtherGradient` (degradados pensados exactamente para diferenciar ambas
  burbujas) pero **nunca se usaban** — `Glassmorphism.kt` aplicaba siempre el mismo
  degradado blanco genérico.
- **Corrección:** `Modifier.glassmorphic()` ahora acepta un `backgroundBrush` propio;
  `glassmorphicChatBubbleSelf()` / `Other()` usan cada uno su degradado real, y el texto de
  ambas burbujas usa colores claros de alto contraste (`OnBackgroundText` / `Color.White`).
- **Archivos:** `ui/theme/Glassmorphism.kt`, `ui/screens/ChatScreen.kt`.

### 1.2 Ícono de "cerrar sesión" fuera de estilo
- **Antes:** un emoji de puerta de madera 🚪 (rompe la estética cyberpunk-neón del resto de
  la app) en dos lugares: la cabecera de Chats y el botón de Perfil.
- **Corrección:** ícono vectorial propio dibujado en Canvas (`NeonLogoutIcon`, un símbolo de
  encendido/apagado minimalista), reemplazado en ambos lugares.
- **Archivos:** `ui/theme/IconGlyphs.kt` (nuevo), `ui/screens/ChatsListScreen.kt`,
  `ui/screens/ProfileScreen.kt`.

### 1.3 Glow Arena: minijuegos falsos
- **Antes:** de las 5 tarjetas de la Arena, solo "3 en Raya Neón" funcionaba. "Glow Race",
  "Cyber Quiz", "Neon Jump" y "Pixel Fight" no tenían ningún juego detrás: tocarlas mostraba
  siempre el mismo Toast de "bloqueado", sin comparar contra los GlowCoins reales del
  usuario.
- **Corrección:** 3 minijuegos reales y completos (ver sección 2), con desbloqueo real
  comparando `user.glowCoins`. El resto de las tarjetas se marcan honestamente como
  "Próximamente" en vez de simular un bloqueo falso.
- **Archivos:** `ui/screens/GamesScreen.kt`, `ui/games/*.kt`.

### 1.4 Registro: valores quemados y flujo silenciosamente roto
- **Antes:** el campo de apodo arrancaba pre-rellenado con `"GlowPro99"` como si fuera un
  dato real; y si el email o la contraseña quedaban vacíos, el botón de registro **igual
  avanzaba** como si la cuenta se hubiera creado (sin llamar a Firebase Auth).
- **Corrección:** el campo arranca vacío (con "GlowPro99" solo como texto de ejemplo); si el
  formulario no es válido, se explica exactamente qué falta en vez de fingir éxito.
- **Archivo:** `ui/screens/AuthScreens.kt`.

### 1.5 Validación de contraseña: sin feedback en tiempo real
- **Antes:** no existía ninguna validación de fuerza de contraseña más allá de "no vacío";
  el usuario solo se enteraba de un problema (contraseña muy corta) después de tocar
  "Registrarse" y recibir el error crudo de Firebase.
- **Corrección:** checklist reactivo de 3 reglas (6+ caracteres, un número, una mayúscula)
  que se pone verde neón al cumplirse cada una, en tiempo real, sin bloquear el campo.
- **Archivo:** `ui/screens/AuthScreens.kt`.

### 1.6 Cámara: botón de girar sin función y sin captura real
- **Antes:** el botón 🔄 tenía un `clickable { /* Girar Cámara */ }` — un comentario, no una
  acción. "Tomar Foto" solo activaba una bandera booleana y oscurecía la vista en vivo; no
  se capturaba ninguna imagen real.
- **Corrección:** el botón 🔄 alterna cámara frontal/trasera de verdad (reconstruye el
  `CameraSelector` y re-vincula CameraX). "Tomar Foto" usa un `ImageCapture` real de CameraX
  y muestra la foto genuina capturada (con corrección de rotación) en la pantalla de
  edición.
- **Archivo:** `ui/screens/CameraScreen.kt`.

### 1.7 Pestaña "Descubrir" no mostraba nada real
- **Antes:** la pestaña "Descubrir" de la barra inferior apuntaba a `GlowinkShowcaseScreen`,
  una pantalla que solo mostraba la paleta de colores del sistema de diseño — no una
  función real de la app.
- **Corrección:** pantalla `DiscoverScreen` real con un feed de comunidades para explorar
  (datos de ejemplo, listos para conectarse a Firestore).
- **Archivos:** `ui/screens/DiscoverScreen.kt` (nuevo), `ui/navigation/GlowinkNavigation.kt`,
  `MainActivity.kt` (se retiró la pantalla de muestra).

### 1.8 Avatares: emoji fijo en vez de datos reales
- **Antes:** el avatar de cada persona se elegía con un `when (friend.id)` que devolvía
  siempre el mismo emoji fijo para cada ID de usuario de ejemplo — no reflejaba ninguna
  personalización real ni escalaba a usuarios nuevos.
- **Corrección:** avatar vectorial real (ver sección 2) dibujado a partir de
  `user.avatarConfig`, consistente en lista de chats, burbujas, historias y Perfil.
- **Archivos:** `ui/screens/ChatsListScreen.kt`, `ChatScreen.kt`, `GlowFeedScreen.kt`,
  `ProfileScreen.kt`.

### 1.9 Seguridad: credenciales y basura de build en el repositorio
- **Antes:** `.gitignore` no excluía `google-services.json` (contiene la API key de
  Firebase/Google Cloud — de ahí la exposición detectada), y dos logs de crash del JVM
  (~8&nbsp;MB) estaban sueltos en la raíz del proyecto.
- **Corrección:** `.gitignore` ahora excluye `google-services.json`, keystores,
  `hs_err_pid*.log`, `replay_pid*.log`, `.apk`/`.aab`. Los logs de crash se eliminaron del
  proyecto. **Acción pendiente que solo tú puedes hacer:** rotar la API key expuesta desde
  Google Cloud Console — ver `docs/MANUAL_DESARROLLADOR.md`, sección "Seguridad".
- **Archivo:** `.gitignore`.

### 1.10 Código muerto / confuso
- 3 archivos huérfanos sin usar en ningún lado (`LoginScreen.kt`, `SignUpScreen.kt`,
  `SplashScreen.kt` — versiones antiguas ya reemplazadas por `AuthScreens.kt`): eliminados.
- `MainActivity.kt` tenía ~150 líneas de una pantalla de muestra sin conexión real (ver
  1.7) más dos "extension properties" con sintaxis inusual para parchear el tema: se
  eliminó todo, dejando solo la actividad principal.
- El `README.md` original enlazaba imágenes con rutas absolutas de Windows
  (`C:/Users/...`) que no se ven en GitHub ni en ningún lado fuera de esa máquina, y tenía
  un bloque de código sin cerrar. Corregido.

---

## 2. Funcionalidades nuevas

### 2.1 Sistema de Avatar personalizable (real, funcional hoy)
Composición de capas gráficas locales (`ui/avatar/GlowAvatar.kt`) con escala
terceros — que renderiza un personaje a partir de un `Avatar3DConfig`: silueta, color de
piel, peinado + color, estilo y color de ojos, ropa + colores, accesorio. Con animación de
parpadeo y balanceo sutil. Editor con vista previa en vivo, categorías y cuadrícula de
opciones (`ui/avatar/AvatarEditorScreen.kt`), disponible en el registro y desde
Perfil → Editar Avatar. Ver `MANUAL_DESARROLLADOR.md` para por qué se eligió este enfoque en
vez de capas de imágenes PNG.

### 2.2 Tres minijuegos nuevos y originales
- **Culebra Glow** — el clásico juego de la serpiente con un giro propio (Prismas Glow, un
  power-up de puntos extra), Canvas de Compose, controles por gestos + D-pad.
- **Duelo Relámpago** — mi interpretación de "pistolitas": duelo de reflejos a 2 jugadores
  en un mismo teléfono, con detección de salida en falso.
- **Carrera Glow** — inspirado en el mecanismo genérico de "fichas que recorren un circuito
  y se capturan entre sí" (el mismo principio detrás de juegos de siglos de antigüedad de
  dominio público como el Pachisi) sobre un circuito ovalado propio — no copia el tablero
  en cruz ni el arte de ningún juego comercial. Base, capturas, casillas seguras, turno
  extra al sacar 6.

Los tres otorgan GlowCoins reales y guardan sus estadísticas (`GameStats`) en el documento
del usuario en Firestore. Ver `docs/FIRESTORE_SCHEMA.md`.

### 2.3 Visor 3D (para tus modelos de Tripo3D)
Pantalla nueva (Perfil → Visor 3D) que muestra un archivo `.glb` con el componente
`<model-viewer>` de Google dentro de un WebView, con cámara inicial mirando de frente y
rotación libre con el dedo. Ver `assets/models/LEEME.md` para instrucciones y para la
explicación honesta de sus límites (no reemplaza al editor de avatar 2D).

### 2.4 Estadísticas de la Arena en el Perfil
Nueva sección en Perfil que muestra el mejor puntaje/victorias reales de cada minijuego.

---

## 3. Lo que se dejó igual (a propósito) y por qué

- **CameraX, Firebase Auth/Firestore en tiempo real**: ya estaban correctamente
  implementados en la versión que subiste (a diferencia de lo que describía el prompt
  maestro que acompañaba el proyecto) — se verificó y no se tocó.
- **La lista de amigos y las historias de ejemplo** siguen usando datos de muestra
  (`u1`–`u5` hardcodeados en `ChatRepository`) porque construir un sistema real de
  solicitudes de amistad es un alcance nuevo y grande, fuera de lo que se pidió corregir.

---

## 4. Segunda pasada — nuevas incorporaciones

### 4.1 Cuarto minijuego: Cyber Quiz 🧠
Trivia original (10 preguntas de cultura gamer/tecnología, todas escritas para este
proyecto — ninguna copiada de un banco de preguntas existente), opción múltiple, 12
segundos por pregunta, con racha de aciertos. Reemplaza la tarjeta "Próximamente" que tenía
antes en la Arena. Otorga GlowCoins reales y guarda `highScoreQuiz` en Firestore.
**Archivo:** `ui/games/QuizGame.kt`.

### 4.2 Publicación de fotos reales en Firebase Storage
- **Antes:** `publishStory()` siempre guardaba el texto fijo `"story_media"` como
  `mediaUrl`, sin importar qué foto se hubiera tomado — la captura real (agregada en la
  primera pasada) no se subía a ningún lado.
- **Ahora:** al tocar "Publicar Estado", la foto capturada se comprime a JPEG y se sube a
  Firebase Storage (`stories/{uid}/{timestamp}.jpg`); el Estado se guarda con la URL real
  de descarga. Si la subida falla (por ejemplo, porque Storage todavía no está habilitado
  en tu proyecto de Firebase — ver `MANUAL_DESARROLLADOR.md`), no se deja a la persona
  atascada: se publica igual con un marcador y se continúa.
- **Archivos:** `app/build.gradle.kts` (dependencia `firebase-storage` nueva),
  `ui/screens/CameraScreen.kt`.

### 4.3 Ícono de la app: no solo parecido a una marca — también mal formado
- **Hallazgo nuevo:** además del parecido con el monograma de Gucci ya señalado en la
  primera pasada, el archivo `ic_launcher_glowink.png` medía **896×1200 px** — no es
  cuadrado. Un ícono de lanzador no cuadrado se ve estirado/recortado de forma inconsistente
  según el launcher del teléfono.
- **Corrección:** se generó un ícono nuevo, 100% original (una burbuja de chat con un rayo
  al centro, en tu misma paleta morado→cian→lima), cuadrado (512×512, con transparencia
  real). El archivo original queda guardado como
  `res/drawable/ic_launcher_glowink_original_backup.png` por si prefieres volver a él o
  usarlo como punto de partida para tu propio diseño.
- **Nota aparte:** el proyecto también tiene el set de íconos adaptativos por defecto que
  genera Android Studio (`mipmap-anydpi-v26/ic_launcher.xml` + `ic_launcher_background` /
  `ic_launcher_foreground`), pero el `AndroidManifest.xml` nunca los usa — apunta
  directamente a `@drawable/ic_launcher_glowink`. No es un bug (el ícono sí se ve
  correctamente), pero son recursos sin usar; se documenta acá por si en el futuro quieres
  migrar a un ícono adaptativo "de verdad" (con capas de fondo/frente independientes, mejor
  soportado por launchers de Android modernos).

### 4.4 Ajedrez Glow como próxima incorporación
Se dejó marcada honestamente como "Próximamente" en vez de una tarjeta bloqueada falsa,
recogiendo el pedido de sumar ajedrez más adelante. Un motor de ajedrez completo (reglas,
jaque/mate, IA o multijugador) es un proyecto propio bastante grande — se documenta como
siguiente paso en `MANUAL_DESARROLLADOR.md` en vez de improvisar una versión incompleta.

---

## 5. Tercera pasada — auditoría de errores y pulido

### 5.1 Contraseña sin opción de mostrarla (👁️)
Los mockups de referencia mostraban un ícono de ojo para revelar la contraseña al
escribirla; el formulario real no lo tenía en ningún lado. Se agregó en Login y Registro:
toca 👁 / 🙈 para alternar entre oculta y visible.

### 5.2 Validación de email inexistente
Antes solo se comprobaba que el campo no estuviera vacío — `"cualquier-cosa"` sin arroba
pasaba como email válido. Ahora se valida el formato con `android.util.Patterns.EMAIL_ADDRESS`
en Login y Registro, con un mensaje claro si el formato no es válido.

### 5.3 Chat: sin scroll automático ni estado vacío
- **Antes:** al enviar o recibir un mensaje nuevo, la conversación no se desplazaba sola —
  había que arrastrar el dedo manualmente cada vez. Y un chat sin mensajes se veía como un
  espacio en blanco sin ninguna explicación.
- **Corrección:** la lista de mensajes ahora se desplaza automáticamente al más reciente, y
  un chat vacío muestra un mensaje de bienvenida ("Todavía no hay mensajes — ¡Envía el
  primer saludo!") en vez de verse roto o incompleto.

### 5.4 Errores de Firestore completamente silenciosos
- **Antes:** los 5 listeners en tiempo real de `ChatRepository` (perfil, usuarios, mensajes,
  minijuego, historias) ignoraban cualquier error sin dejar ningún rastro — si las reglas de
  seguridad de Firestore estaban mal configuradas, la app simplemente no cargaba nada y no
  había forma de saber por qué mirando Logcat.
- **Corrección:** cada listener ahora registra el error real con `Log.e(...)` antes de
  salir, identificando cuál de los cinco falló.

### 5.5 Bug sutil de sincronización en el Visor de Estados
En `GlowFeedScreen`, si las Historias reales de Firestore llegaban justo mientras la barra
de progreso ya estaba corriendo, el efecto que decide "¿avanzo a la siguiente historia o
cierro el feed?" seguía mirando la lista vacía que había capturado al arrancar — y cerraba
el feed de golpe aunque sí hubiera historias para mostrar. Corregido con
`rememberUpdatedState`, el patrón correcto de Compose para este caso.

### 5.6 Consistencia técnica en barras de progreso
Se unificó el uso de `LinearProgressIndicator` a la firma clásica (`progress: Float`) en
todo el proyecto — la firma más nueva (`progress: () -> Float`) depende de una versión
específica de Material3 que no se pudo confirmar en este entorno sin compilador; usar la
forma clásica en todos lados elimina ese riesgo por completo.
