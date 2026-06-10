# Conclusiones


---

## Tabla de contenido

1. [Programación funcional](#1-programación-funcional)
2. [Corrección](#2-corrección)
3. [Paralelismo](#3-paralelismo)
4. [Aprendizajes](#4-aprendizajes)

---

## Conclusiones del proyecto

### 1. Programación funcional

Implementar la solución con recursión y funciones de alto orden en lugar de ciclos imperativos presentó ventajas claras desde el inicio. El uso de `map`, `flatMap`, `filter`, `sortBy` y `zip` permitió expresar cada cálculo de forma declarativa y directamente alineada con su especificación matemática: por ejemplo, `choques` se traduce casi literalmente desde la definición de $\text{CH}^\alpha_C$, y `generarAsignaciones` refleja de manera natural la construcción inductiva de $\{0,\ldots,m-1\}^n$.

La ausencia de variables mutables eliminó una clase entera de errores difíciles de rastrear. Al no haber estado compartido entre llamados, cada función puede analizarse de forma aislada, lo que facilitó tanto la depuración como la argumentación de corrección. Esto resultó especialmente valioso al momento de paralelizar: como ninguna función modifica estado externo, fue posible lanzar `parallel` y `task` sin ningún mecanismo de sincronización adicional como semáforos o monitores.

Otra ventaja concreta del estilo funcional fue la **composabilidad**: `costoAsignacion` se construye directamente sobre `choques`, `capacidadFallida`, `desperdicio` y `movilidad`, y `asignacionOptima` se construye sobre `generarAsignaciones` y `costoAsignacion`. Esta cadena de composición hizo que los errores se localizaran con facilidad: si el costo calculado era incorrecto, bastaba con probar cada componente por separado para identificar cuál fallaba.

La **transparencia referencial** también jugó un papel importante en la escritura de pruebas: al ser funciones puras, cada una produce siempre el mismo resultado para los mismos argumentos, lo que permitió escribir casos de prueba deterministas sin necesidad de preparar o limpiar estado entre pruebas.

La principal dificultad estuvo en `generarAsignaciones` y en la función `merge` de `movilidadPar`: pensar el problema en términos de casos base y paso recursivo requirió un cambio de perspectiva respecto al enfoque iterativo habitual. En particular, diseñar un `merge` con acumulador para evitar desbordamiento de pila en listas largas fue el reto técnico más exigente del proyecto. Una versión ingenua sin acumulador habría fallado con entradas moderadas debido al crecimiento lineal de la pila de llamados en Scala/JVM.

---

### 2. Corrección

La estrategia central para argumentar la corrección fue la correspondencia directa entre cada implementación y su especificación matemática. Para funciones no recursivas (`solapan`, `capacidadFallida`, `desperdicio`, `movilidad`, `costoAsignacion`) bastó con mostrar que cada expresión Scala computa exactamente el término correspondiente de la fórmula, usando propiedades conocidas de las funciones de alto orden (`count`, `map`/`sum`, `zip`/`tail`).

Para `generarAsignaciones`, la técnica adecuada fue la **inducción estructural sobre $n$**: el caso base $n=0$ produce el único vector de $\{0,\ldots,m-1\}^0$, y el paso inductivo extiende cada asignación de longitud $n-1$ con los $m$ valores posibles, garantizando que el resultado tiene exactamente $m^n$ elementos sin duplicados ni omisiones. Este fue el único punto del proyecto donde la argumentación informal no era suficiente: sin inducción no es evidente que `flatMap` con `:+` cubra todo el espacio sin repeticiones.

El **invariante de representación** más importante del proyecto es que `generarAsignaciones(n, m)` produce exactamente todos los vectores de longitud $n$ sobre el alfabeto $\{0,\ldots,m-1\}$. Este invariante es el que sostiene la corrección de `asignacionOptima`: si el espacio de búsqueda es completo, el `minBy` sobre él garantiza el óptimo global. Cualquier error en `generarAsignaciones` se propagaría silenciosamente a `asignacionOptima`, devolviendo un falso óptimo sin señal de error, razón por la cual este invariante merece atención especial.

Un aspecto adicional que refuerza la confianza en la corrección es la **validación cruzada con los ejemplos del enunciado**: los costos calculados para los Ejemplos 1 y 2 ($\text{CT} = 1031$, $37$, $155$, $160$) coinciden exactamente con los valores esperados, lo que constituye una verificación independiente de que `choques`, `desperdicio`, `movilidad` y `costoAsignacion` funcionan correctamente de forma conjunta.

Para las versiones paralelas, la corrección se argumentó por reducción a las versiones secuenciales: `choquesPar` y `desperdicioPar` son correctas porque la suma de dos rangos disjuntos cubre el rango completo; `movilidadPar` es correcta porque el `merge` preserva el orden total por hora de inicio; y `asignacionOptimaPar` es correcta porque los dos `minimoEn` cubren particiones complementarias del espacio completo.

---

### 3. Paralelismo

El paralelismo resultó beneficioso únicamente cuando el trabajo asignado a cada hilo era suficientemente grande para amortizar el costo de creación y sincronización de tareas. Los resultados experimentales permiten distinguir tres escenarios claros:

**Escenarios con ganancia real:** `asignacionOptimaPar` con $n \geq 7$ y $m \geq 4$ mostró aceleraciones consistentes de entre $+22\%$ y $+63\%$. Con $5^7 = 78\,125$ o $5^8 = 390\,625$ candidatas, cada mitad tiene trabajo suficiente para justificar el uso de dos hilos, y la tendencia se acerca al límite teórico de $+100\%$ que predice la ley de Amdahl con $p=2$ y $\alpha \to 1$.

**Escenarios donde la sobrecarga superó la ganancia:** las versiones paralelas de `choques`, `desperdicio` y `movilidad` fueron siempre más lentas que las secuenciales (hasta $-2700\%$ en `movilidadPar`). El problema es estructural: con $n \leq 8$ el trabajo total es una suma sobre pocos elementos, y el costo de inicializar el `ForkJoinPool` supera con creces el cómputo útil. `generarAsignacionesPar` con $m \geq 4$ también resultó perjudicial: lanzar más tareas no ayuda cuando el trabajo por tarea no crece proporcionalmente.

**Interpretación desde la ley de Amdahl:** aunque la fracción paralelizable $\alpha$ es teóricamente cercana a $1$ en todas las funciones (no hay trabajo secuencial obligatorio salvo la combinación final), la ley de Amdahl asume que el costo de paralelizar es nulo. En la práctica, el overhead de `ForkJoinPool` introduce una constante aditiva que hace que la ganancia neta sea negativa para tamaños pequeños. Esto ilustra la diferencia entre el modelo teórico y el comportamiento real en la JVM.

La regla práctica derivada de los experimentos es que el paralelismo en este problema vale la pena cuando $m^n \gtrsim 10\,000$, es decir, cuando el espacio de búsqueda es lo suficientemente grande para que cada mitad tenga carga de trabajo real. Por debajo de ese umbral, la versión secuencial es siempre preferible.

Finalmente, vale la pena notar que la naturaleza del problema favorece el **paralelismo de datos** sobre el **paralelismo de tareas** para la búsqueda del óptimo: el espacio de candidatas es un vector independiente donde cada elemento puede evaluarse sin comunicación con los demás, lo que lo convierte en un caso ideal para colecciones paralelas (`.par`) o estrategias de divide y vencerás más profundas que la división binaria aquí implementada.

---

### 4. Aprendizajes

Los conceptos del curso que resultaron más útiles fueron la **inducción como mecanismo de argumentación**, las **funciones de alto orden** (`flatMap`, `map`, `filter`, `sortBy`) y el modelo de **paralelismo de tareas** con `parallel` y `task`. La inducción permitió razonar sobre `generarAsignaciones` con precisión formal; las funciones de alto orden hicieron que el código fuera conciso y directamente verificable contra la especificación; y el modelo de tareas sin estado compartido simplificó el razonamiento sobre corrección de las versiones paralelas.

El concepto de **transparencia referencial** resultó más valioso de lo esperado: no solo facilitó las pruebas unitarias, sino que fue el argumento central para justificar que `parallel` es seguro en todas las funciones implementadas. Si alguna función hubiera tenido efectos secundarios, la paralelización habría requerido sincronización explícita y el análisis de corrección habría sido considerablemente más complejo.

También fue muy útil comprender la diferencia entre **recursión lineal** y **recursión de cola**: `generarAsignaciones` no es recursión de cola (el resultado de la llamada recursiva se pasa a `flatMap`), lo que implica que la pila crece con $n$. Para los valores de $n \leq 8$ del proyecto esto no representa un problema, pero en instancias más grandes sería necesario una transformación a recursión de cola o el uso de trampolines.

Si volviéramos a empezar, cambiaríamos tres aspectos del diseño. Primero, paralelizaríamos `asignacionOptimaPar` de forma **recursiva tipo divide y vencerás** — dividiendo el espacio en más de dos mitades en cada nivel — para aprovechar mejor máquinas con más de dos núcleos, en lugar de limitarnos a una división binaria plana. Segundo, añadiríamos un **umbral de granularidad** en `choquesPar`, `desperdicioPar` y `movilidadPar`: si $n$ es menor que cierto valor (por ejemplo, $n < 50$), la función caería directamente en la versión secuencial, evitando la sobrecarga observada en los benchmarks. Tercero, exploraríamos el uso de **colecciones paralelas** (`.par`) para `asignacionOptima`, que delegan el particionado y la combinación al runtime de Scala de forma más granular que una división binaria manual, potencialmente obteniendo mejores aceleraciones en espacios de búsqueda grandes.
