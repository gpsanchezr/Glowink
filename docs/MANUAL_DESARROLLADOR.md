# Manual del Desarrollador — Glowink

Guía técnica completa: arquitectura, cómo funciona cada sistema, cómo compilar el proyecto y
cómo extenderlo. Para el listado detallado de qué se corrigió y qué se agregó, ver
`docs/CHANGELOG.md`. Para el esquema de datos, ver `docs/FIRESTORE_SCHEMA.md`.

## 1. Stack técnico

| Capa | Tecnología |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Lenguaje | Kotlin |
| Navegación | Navigation-Compose (un único `NavHost`) |
| Backend | Firebase Auth (email/contraseña) + Cloud Firestore (tiempo real) |
| Cámara | CameraX (`Preview` + `ImageCapture`) |
| Arquitectura | MVVM ligero: `ChatRepository` (Firestore) → `ChatViewModel` (StateFlow) → pantallas Compose |
| Avatar | Composición de capas PNG locales con Jetpack Compose (`Image` + `ContentScale.Fit`) |
| Visor 3D | WebView + `<model-viewer>` (componente web de Google) |

## 2. Estructura del proyecto

```
app/src/main/java/com/example/glowink/
├── MainActivity.kt                 — punto de entrada único
├── data/
│   ├── Models.kt                   — User, Avatar3DConfig, GameStats, Message, GlowStory, GameState
│   └── ChatRepository.kt           — toda la comunicación con Firebase
├── ui/
│   ├── avatar/
│   │   ├── AvatarCatalog.kt        — catálogo de opciones (única fuente de verdad)
│   │   ├── GlowAvatar.kt           — composición de capas del avatar
│   │   └── AvatarEditorScreen.kt   — UI del editor (preview + categorías + grilla)
│   ├── games/
│   │   ├── SnakeGame.kt            — Culebra Glow
│   │   ├── DuelGame.kt             — Duelo Relámpago
│   │   └── RaceGame.kt             — Carrera Glow
│   ├── navigation/GlowinkNavigation.kt   — todas las rutas de la app
│   ├── screens/                    — el resto de las pantallas (Chat, Perfil, Cámara, etc.)
│   ├── theme/                      — colores, tipografía, glassmorphism, íconos vectoriales
│   └── viewmodel/ChatViewModels.kt — ChatViewModel (Auth + estado de la app)
└── assets/models/                  — carpeta para tus archivos .glb (Visor 3D)
```

## 3. Cómo compilar

1. Abrir la carpeta del proyecto en Android Studio (versión reciente, ver
   `gradle/libs.versions.toml` para las versiones exactas de AGP/Kotlin/Compose).
2. Colocar tu propio `app/google-services.json` (o usar el que ya está, ver la sección de
   Seguridad más abajo antes de subir el proyecto a un repositorio público).
3. Sync de Gradle → Run.
4. **Nota de este entorno:** este documento y todo el código se prepararon sin acceso a un
   compilador de Android (el entorno de trabajo no tiene los repositorios de Google
   Maven/Gradle disponibles), así que cada archivo se revisó a mano línea por línea, pero la
   primera compilación real la vas a hacer tú en Android Studio. Si algo no compila,
   probablemente sea un detalle menor de importaciones — revisa el mensaje de error exacto
   de Android Studio, casi siempre dice justo qué símbolo falta.

## 4. Sistema de Avatar — cómo funciona y por qué se hizo así

### La decisión de diseño más importante de esta actualización

Existían dos borradores de cómo debía funcionar el creador de personajes:

1. **Capas de imágenes PNG** (`Box` con varios `Image(painterResource(R.drawable...))`
   superpuestos, cambiando el ID del recurso por categoría) — el enfoque que describían
   algunos de los prompts/documentos de referencia del proyecto.
2. **Dibujo vectorial en código** (lo que se implementó).

El enfoque de PNGs es válido y es como lo hacen muchos juegos — pero **requiere que exista
un set de arte**: decenas de imágenes transparentes, todas al mismo tamaño y estilo, una por
cada combinación de peinado/ropa/accesorio. Hoy ese arte no existe: lo único disponible son
dos ilustraciones completas de referencia (una mujer, un hombre) generadas por IA, no capas
separadas. Usar el enfoque de PNGs ahora mismo habría significado mostrar recuadros grises
de "placeholder" en cada opción del editor — técnicamente "completo" pero inútil en la
práctica.

Por eso `ui/avatar/GlowAvatar.kt` dibuja el personaje directamente con `Canvas` de Compose
(círculos, óvalos y curvas Bézier) a partir de los campos de `Avatar3DConfig`. Ninguna
imagen de por medio, cero riesgo de derechos de autor, y **toda combinación funciona hoy
mismo**, no cuando alguien produzca el arte.

### Cómo agregar una opción nueva (ej. un peinado)

1. Agregar la entrada a la lista correspondiente en `AvatarCatalog.kt` (un `StyleOption` o
   `SwatchOption` más).
2. Si es un peinado/ropa/accesorio nuevo (no solo un color), agregar un caso al `when` de
   `drawHairFront` / `drawOutfit` / `drawAccessory` en `GlowAvatar.kt` con la geometría
   deseada.

Eso es todo — el editor (`AvatarEditorScreen.kt`) genera su grilla de opciones iterando el
catálogo automáticamente, no hay que tocar la UI.

### Migrar a PNGs en el futuro

Si en algún momento se produce un set de arte real (transparente, mismo tamaño, por
categoría), migrar es acotado: `Avatar3DConfig` ya tiene el campo correcto para cada
categoría — solo habría que escribir una versión de `GlowAvatar` que en vez de dibujar en
`Canvas`, mapee cada campo a un `R.drawable.*` y lo dibuje con `Image()`. Ninguna pantalla
que use `GlowAvatarFrame` tendría que cambiar.

## 5. Visor 3D — límites honestos

El Visor 3D (`Avatar3DViewerScreen.kt`) muestra un archivo `.glb` completo con
`<model-viewer>`. Es la pieza que sí ofrece "3D real" — rotable, con reflejos e iluminación.

**Lo que NO hace, y por qué:** no puede tomar un modelo exportado de Tripo3D y cambiarle la
ropa o el peinado en tiempo real. Los modelos de Tripo3D son una malla única y fusionada
(cuerpo, ropa y pelo son literalmente el mismo objeto 3D) sin esqueleto ("rig") ni piezas
separadas. Para lograr un personaje 3D con ropa intercambiable de verdad, se necesitaría:

1. Modelar un personaje base en Blender con un esqueleto (rig) para animación.
2. Modelar cada prenda/peinado como una malla independiente, ajustada al mismo esqueleto.
3. Exportar todo junto a `.glb` con nombres de nodo consistentes.
4. Recién ahí, el visor podría mostrar/ocultar mallas por nombre según la selección — un
   trabajo real de pipeline de arte 3D, no una limitación del código de la app.

Esto también responde la pregunta de "animaciones 3D realistas": animar (caminar, saludar,
un ciclo de reposo) requiere ese mismo esqueleto — una malla estática de Tripo3D no trae
animación, así que hoy no hay forma de animar ese modelo específico sin rehacerlo en un
pipeline con rig.

## 6. Los cuatro minijuegos nuevos

Cada uno vive en su propio archivo bajo `ui/games/`, es una función `@Composable`
autocontenida (no conoce Firebase ni el ViewModel directamente) y expone callbacks simples
(`onExit`, `onGameOver`/`onMatchOver`) — la pantalla de navegación
(`GlowinkNavigation.kt`) es la única que traduce el resultado del juego en una llamada a
`viewModel.grantGameReward(...)`. Esto hace que cada juego sea fácil de probar o reutilizar
por separado.

Sobre originalidad: ninguno de los cuatro copia arte, nombres ni tableros de un juego
existente. Lo único "compartido" con juegos clásicos es el **mecanismo genérico** (una
serpiente que crece, un duelo de reflejos, fichas que recorren un circuito y se capturan, o
una trivia de opción múltiple) — reglas de juego de este tipo no son material protegido por
derechos de autor (son ideas / mecánicas, no expresión concreta); lo que sí importa es no
copiar el arte, el tablero exacto ni la marca de un juego específico, y eso se evitó a
propósito (por ejemplo, Carrera Glow usa un circuito ovalado propio, no el tablero en forma
de cruz del parchís tradicional).

### Agregar un quinto minijuego (ej. Ajedrez Glow)

1. Crear `ui/games/NombreDelJuego.kt` siguiendo el mismo patrón: estado inmutable + función
   `advanceX(state, input): State` + un `@Composable` de UI (ver `QuizGame.kt` como el
   ejemplo más simple, o `RaceGame.kt` como el más completo).
2. Agregar a `GameStats` en `data/Models.kt` el campo que haga falta para guardar el
   progreso de ese juego.
3. Agregar la ruta en `GlowinkNavigation.kt` y la tarjeta en `GamesScreen.kt`
   (`arenaGames`), retirando la tarjeta de "Próximamente" correspondiente.

## 7. Seguridad y credenciales

**Acción pendiente que debes hacer tú** (no se puede hacer desde este entorno de trabajo):
el prompt original del proyecto reportaba una API key de Google Cloud/Firebase expuesta en
el historial del repositorio. Para neutralizarla:

1. Entra a [Google Cloud Console](https://console.cloud.google.com/apis/credentials) del
   proyecto de Firebase de Glowink.
2. Genera una API key nueva y **revoca/elimina** la comprometida.
3. Descarga el `google-services.json` actualizado y reemplázalo localmente (este archivo ya
   quedó agregado a `.gitignore`, así que a partir de ahora no se va a volver a subir por
   accidente).
4. Si el repositorio ya es público, ten en cuenta que la clave vieja pudo quedar en el
   historial de commits — rotarla es lo que realmente la neutraliza, no borrarla del último
   commit.

## 8. Ícono de la app

El ícono original (dos "G" entrelazadas en anillos, estilo holográfico) se parecía bastante
al monograma "GG" de Gucci, una marca registrada — y además el archivo medía 896×1200 px,
no cuadrado, así que probablemente se veía estirado según el launcher del teléfono.

Se reemplazó por un diseño propio: una burbuja de chat con un rayo al centro (glow = brillo/
energía, la burbuja = chat), en tu misma paleta morado→cian→lima, cuadrado y con
transparencia real (`res/drawable/ic_launcher_glowink.png`, 512×512). El ícono anterior
queda disponible en `ic_launcher_glowink_original_backup.png` por si prefieres volver a él o
usarlo de referencia para tu propio diseño — para revertirlo, solo renombra ese archivo de
vuelta a `ic_launcher_glowink.png` (reemplazando el nuevo).

## 9. Limitaciones conocidas / roadmap sugerido

- Filtros de cámara con IA real (detección facial ML Kit, reemplazo de fondo) — hoy son
  overlays visuales (emoji + pulso animado) sobre el feed real de la cámara, no
  segmentación real. Implementarlo es viable pero es un módulo grande aparte (ML Kit
  Face Detection + Subject Segmentation con `ImageAnalysis` de CameraX).
- Lista de amigos/historias de ejemplo: `ChatRepository` sigue usando usuarios de muestra
  (`u1`–`u5`); un sistema real de solicitudes de amistad es trabajo nuevo, no un bug.
- Ajedrez Glow: quedó marcado como "Próximamente" honesto en la Arena. Un motor de ajedrez
  completo (reglas, jaque/mate, y multijugador o IA) es un proyecto propio de buen tamaño;
  el patrón para agregarlo es el mismo que el resto de los juegos (ver sección 6).
- Neon Jump / Pixel Fight (las dos tarjetas de la Arena original que no se implementaron):
  quedan como ranuras libres para minijuegos futuros siguiendo el mismo patrón.

### Habilitar Firebase Storage (necesario para que se suban las fotos)

Desde esta actualización, "Publicar Estado" sube la foto real a Firebase Storage. Para que
funcione en tu proyecto de Firebase:

1. Entra a la [consola de Firebase](https://console.firebase.google.com) → tu proyecto →
   **Storage** → "Comenzar" (si nunca lo activaste, Firebase te pide elegir una ubicación y
   confirmar las reglas de seguridad iniciales).
2. Reglas mínimas recomendadas para empezar (ajusta cuando tengas roles/permiso reales):
   ```
   rules_version = '2';
   service firebase.storage {
     match /b/{bucket}/o {
       match /stories/{userId}/{allPaths=**} {
         allow read: if true;
         allow write: if request.auth != null && request.auth.uid == userId;
       }
     }
   }
   ```
3. No hace falta ningún cambio de código adicional: si Storage no está habilitado, la app
   detecta el fallo de subida y publica igual con un marcador en vez de quedarse
   trabada — ver `docs/CHANGELOG.md` sección 4.2.

