# Estructura de Firestore

Responde directamente la pregunta de cierre del prompt original: **sí, el sistema de
desbloqueo de minijuegos por GlowCoins ya queda guardado de forma permanente**, porque usa
el mismo campo `glowCoins` que ya se sincronizaba en tiempo real. No hace falta ninguna
colección nueva para el desbloqueo en sí — solo se agregó `gameStats` para las estadísticas.

## Colección `users_glowink`

Un documento por usuario, con el UID de Firebase Auth como ID de documento.

```
users_glowink/{uid}
├── id: String
├── username: String
├── email: String
├── avatarUrl: String                 (reservado; hoy el avatar se guarda como config, no como imagen)
├── glowCoins: Int                    (fuente de verdad para desbloquear juegos en la Arena)
├── rachaVictorias: Int               (usada para calcular el "nivel" — no se guarda el nivel en sí)
├── avatarConfig: Map
│   ├── siluetaBase: String           ("FEMENINO" | "MASCULINO")
│   ├── colorPiel: String             (hex, ej. "#F4C2A1")
│   ├── estiloPelo: String
│   ├── colorPeloPrimario: String
│   ├── colorPeloSecundario: String
│   ├── estiloOjos: String
│   ├── colorOjos: String
│   ├── estiloRopa: String
│   ├── colorRopaPrimario: String
│   ├── colorRopaSecundario: String
│   └── accesorioId: String
└── gameStats: Map
    ├── highScoreCulebra: Int
    ├── victoriasDuelo: Int
    ├── partidasDuelo: Int
    ├── victoriasCarrera: Int
    ├── highScoreQuiz: Int            (Cyber Quiz)
    └── victoriasTresEnRaya: Int
```

Todos los campos de `Avatar3DConfig` y `GameStats` (ver `data/Models.kt`) tienen valores por
defecto, así que un documento antiguo sin estos campos se sigue leyendo sin errores —
Firestore simplemente rellena los valores por defecto del lado de la app.

## Colecciones existentes (sin cambios)

- `chats/{chatId}/messages/{messageId}` — mensajes de una conversación, en tiempo real vía
  `addSnapshotListener`.
- `stories_glowink/{storyId}` — Estados/Historias temporales (`GlowStory`).
- `games_glowink/{gameId}` — estado de una partida de 3 en Raya Neón (tablero, turno,
  ganador).

## Firebase Storage

```
stories/{uid}/{timestampMillis}.jpg
```

Cada foto publicada desde la Cámara se sube acá; `GlowStory.mediaUrl` guarda la URL de
descarga pública que devuelve Storage. Ver `docs/MANUAL_DESARROLLADOR.md` sección 9 para
habilitar Storage en tu proyecto de Firebase (no viene activado por defecto).

## Cómo se persiste una recompensa de juego

Los tres minijuegos nuevos nunca hablan directo con Firestore — pasan por
`ChatViewModel.grantGameReward(coinsEarned, statsTransform)`, que:

1. Lee el `User` actual en memoria (`_currentUser.value`).
2. Suma `coinsEarned` a `glowCoins` y aplica `statsTransform` sobre `gameStats`.
3. Escribe el documento completo actualizado con `.set(user)` en
   `users_glowink/{uid}`.

Esto significa que agregar un cuarto minijuego (por ejemplo, cuando implementes Cyber Quiz)
solo requiere: (a) agregar el campo que falte a `GameStats`, y (b) llamar a
`grantGameReward` desde la pantalla del juego nuevo — no hay que tocar Firestore
directamente ni migrar nada.

## Próximo paso sugerido (si agregas comunidades reales a "Descubrir")

`DiscoverScreen.kt` hoy usa una lista de ejemplo en memoria. Para conectarlo a datos reales,
el patrón más simple es una colección nueva:

```
comunidades_glowink/{comunidadId}
├── nombre: String
├── emoji: String
├── descripcion: String
└── miembros: Array<String>   (UIDs)
```

siguiendo el mismo patrón de `ChatRepository` (un `StateFlow` alimentado por
`addSnapshotListener`).
