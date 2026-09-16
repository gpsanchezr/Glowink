# Carpeta de modelos 3D (.glb)

Coloca aquí los archivos .glb exportados desde Tripo3D (o cualquier otro generador de
modelos 3D) para que el "Visor 3D" de Glowink (Perfil → Visor 3D) los muestre.

Nombre esperado por defecto: `avatar_showcase.glb`

Si subes un archivo con otro nombre, actualiza la constante `MODEL_FILE_NAME` en
`ui/screens/Avatar3DViewerScreen.kt`.

## Por qué el visor no "viste" al modelo 3D con la ropa del editor 2D

El editor de avatar de Glowink (piel, pelo, ojos, ropa) dibuja un personaje vectorial en
código — no depende de ningún archivo 3D. El Visor 3D es una pieza aparte: muestra tal cual
un modelo .glb ya terminado (como los que genera Tripo3D), rotable con el dedo.

Los modelos que exporta Tripo3D son una malla única y fusionada (todo el personaje, ropa y
pelo incluidos, es "una sola pieza"), sin esqueleto ni piezas de ropa separadas. Por eso hoy
no es técnicamente posible tomar ESE modelo y cambiarle la chaqueta o el peinado en tiempo
real dentro de la app: se necesitaría reconstruir el personaje en un programa como Blender
como un "personaje modular" (un esqueleto/rig + prendas como mallas independientes que se
puedan mostrar/ocultar) — un trabajo de arte 3D aparte, no una limitación de código. Este
punto queda documentado también en docs/MANUAL_DESARROLLADOR.md.
