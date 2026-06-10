# Informe de Paralelización



---

## Estrategia de paralelización

### `choquesPar`

Se divide el vector de índices de cursos en dos mitades `[0, mid)` y `[mid, n)`.
Cada mitad calcula de forma independiente los pares $(i, j)$ con $i < j$ que comparten
aula y se solapan. Como el índice $i$ más pequeño de cada par pertenece exactamente a
uno de los dos rangos, no existe doble conteo. Los resultados parciales se suman al final:

$$CH^\alpha_C = \text{choquesRango}(0, \lfloor n/2 \rfloor) + \text{choquesRango}(\lfloor n/2 \rfloor, n)$$

### `desperdicioPar`

Se divide el vector de cursos en dos mitades. Cada mitad calcula la suma de
$\max(\text{cap}(a_i) - \text{est}(c_i),\ 0)$ para sus índices. Los resultados se suman:

$$DE^\alpha_{C,A} = \text{desperdicioRango}(0, \lfloor n/2 \rfloor) + \text{desperdicioRango}(\lfloor n/2 \rfloor, n)$$

### `movilidadPar`

Se divide el vector de cursos en dos mitades; cada mitad ordena sus índices asignados
por hora de inicio de forma independiente usando `parallel`. Luego se hace un **merge**
de las dos secuencias ordenadas para obtener el orden global correcto, y se suma la
distancia entre aulas consecutivas. Esta estrategia es correcta porque el merge preserva
el orden total por hora de inicio.

### `generarAsignacionesPar`

Se lanza una `task` por cada valor posible $v \in \{0, \ldots, m-1\}$ del primer curso.
Cada tarea genera de forma independiente todas las asignaciones de los $n-1$ cursos
restantes y les antepone $v$. El resultado es la concatenación de los $m$ sub-vectores:

$$\text{generarAsignacionesPar}(n, m) = \bigsqcup_{v=0}^{m-1}\ \{v\} \times \text{gen}(n-1, m)$$

### `asignacionOptimaPar`

Se genera el espacio completo de candidatas con `generarAsignacionesPar` y se divide
en dos mitades. Cada mitad busca su mínimo local en paralelo usando `costoAsignacion`.
El mínimo global es el menor de los dos mínimos locales:

$$\alpha^* = \arg\min\bigl(\min_{i \in [0,\ mid)} CT(\alpha_i),\ \min_{i \in [mid,\ |\mathcal{A}|)} CT(\alpha_i)\bigr)$$

---

## Resultados experimentales

### Conteos individuales (`n = 8`, `m = 5`)

| Función       | Secuencial (ms) | Paralela (ms) | Aceleración (%) |
|:-------------:|:---------------:|:-------------:|:---------------:|
| `choques`     | 0,20            | 4,21          | −2013,55        |
| `desperdicio` | 0,06            | 1,23          | −1859,71        |
| `movilidad`   | 0,24            | 6,65          | −2699,58        |

### Generación de asignaciones

| Cursos $n$ | Aulas $m$ | Secuencial (ms) | Paralela (ms) | Aceleración (%) |
|:----------:|:---------:|:---------------:|:-------------:|:---------------:|
| 4          | 3         | 1,15            | 0,56          | +51,20          |
| 5          | 3         | 1,68            | 1,09          | +34,98          |
| 6          | 3         | 2,24            | 0,87          | +61,01          |
| 6          | 4         | 3,83            | 4,36          | −13,92          |
| 7          | 4         | 7,23            | 7,37          | −1,96           |
| 7          | 5         | 26,27           | 31,81         | −21,08          |
| 8          | 4         | 11,50           | 18,71         | −62,73          |
| 8          | 5         | 41,25           | 163,62        | −296,62         |

### Asignación óptima

| Cursos $n$ | Aulas $m$ | Secuencial (ms) | Paralela (ms) | Aceleración (%) |
|:----------:|:---------:|:---------------:|:-------------:|:---------------:|
| 4          | 3         | 8,45            | 8,89          | −5,27           |
| 5          | 3         | 9,23            | 8,76          | +5,15           |
| 6          | 3         | 12,34           | 12,06         | +2,23           |
| 6          | 4         | 43,98           | 57,68         | −31,14          |
| 7          | 4         | 97,98           | 76,51         | +21,91          |
| 7          | 5         | 269,83          | 99,80         | +63,01          |
| 8          | 4         | 304,88          | 189,30        | +37,91          |
| 8          | 5         | 1073,10         | 678,15        | +36,80          |

---

## Análisis con la ley de Amdahl

La ley de Amdahl establece que la aceleración máxima teórica con $p$ procesadores es:

$$S(p) = \frac{1}{(1 - \alpha) + \dfrac{\alpha}{p}}$$

donde $\alpha$ es la fracción paralelizable del cómputo y $p$ es el número de procesadores.
Con $p = 2$ (dos mitades en paralelo), el límite teórico es $S(2) = \frac{1}{0{,}5 + 0{,}5/2} = 2{,}0$,
es decir, una aceleración máxima del $+100\%$.

### 1. Fracción paralelizada en cada función

**Conteos (`choquesPar`, `desperdicioPar`, `movilidadPar`):** la fracción paralelizable
es $\alpha \approx 1$ en teoría, ya que todo el recorrido se divide en dos mitades
independientes. Sin embargo, los resultados muestran aceleraciones muy negativas
(hasta $-2700\%$). Esto se debe a que el trabajo por elemento es extremadamente pequeño
(sumas simples sobre vectores de 8 elementos), y el costo de crear y sincronizar dos
hilos supera con creces el cómputo útil. En este caso la sobrecarga de `parallel` domina
completamente.

**`generarAsignacionesPar`:** la estrategia de lanzar una `task` por valor del primer
curso paraleliza la generación de $m$ sub-vectores. Con $m = 3$ y $n \leq 6$ los
resultados son positivos (hasta $+61\%$), lo que indica que el trabajo por tarea es
suficiente para amortizar la sobrecarga. Con $m \geq 4$ el número de tareas crece y la
coordinación entre ellas introduce más overhead del que se ahorra, resultando en
aceleraciones negativas.

**`asignacionOptimaPar`:** la búsqueda del mínimo sobre el espacio completo de
$m^n$ candidatas se divide en dos mitades. Los resultados son los más consistentes:
a partir de $(n=7, m=4)$ se obtienen ganancias sostenidas de entre $+22\%$ y $+63\%$.
Esto se explica porque con $n \geq 7$ el espacio de búsqueda crece exponencialmente
($4^7 = 16\,384$ y $5^7 = 78\,125$ candidatas), haciendo que cada mitad tenga trabajo
suficiente para justificar el paralelismo.

### 2. Pares $(n, m)$ donde el paralelismo genera ganancias significativas

Para `asignacionOptimaPar` los pares beneficiosos son:

| Par $(n, m)$  | Aceleración | Razón                                              |
|:-------------:|:-----------:|:---------------------------------------------------|
| $(7,\ 5)$     | $+63{,}01\%$| $5^7 = 78\,125$ candidatas; cada mitad tiene ~39K  |
| $(7,\ 4)$     | $+21{,}91\%$| $4^7 = 16\,384$ candidatas; trabajo suficiente     |
| $(8,\ 4)$     | $+37{,}91\%$| $4^8 = 65\,536$ candidatas                         |
| $(8,\ 5)$     | $+36{,}80\%$| $5^8 = 390\,625$ candidatas; mayor beneficio total |

La regla práctica observada es que el paralelismo en `asignacionOptimaPar` resulta
beneficioso cuando $m^n \gtrsim 10\,000$, es decir, aproximadamente cuando
$n \cdot \log_2 m \gtrsim 13$.

### 3. Casos donde el paralelismo introduce sobrecarga

- **Conteos con $n = 8$, $m = 5$:** la sobrecarga es devastadora porque el trabajo
  total (recorrer 8 elementos) es menor que el tiempo de crear y sincronizar los hilos.
  En ScalaMeter se mide solo una ejecución, por lo que el costo de inicialización del
  `ForkJoinPool` se carga completamente en la primera llamada.

- **`generarAsignacionesPar` con $m \geq 4$:** lanzar más tareas no ayuda cuando el
  trabajo por tarea no crece lo suficiente. Con $m = 5$ y $n = 8$ la versión paralela
  es casi **4 veces más lenta** ($+296\%$ de sobrecarga).

- **`asignacionOptimaPar` con $n \leq 6$ y $m \leq 4$:** el espacio tiene como máximo
  $4^6 = 4\,096$ candidatas, demasiado pocas para que la división en dos mitades
  compense el overhead.

---

## Conclusiones de paralelización

El paralelismo no es universalmente beneficioso: su utilidad depende críticamente del
tamaño del trabajo que se asigna a cada hilo. En este problema, las funciones de conteo
(`choques`, `desperdicio`, `movilidad`) operan sobre vectores pequeños ($n \leq 8$) y
nunca justifican el uso de paralelismo; sus versiones paralelas son siempre más lentas
en los rangos evaluados.

La función `asignacionOptimaPar` es donde el paralelismo muestra valor real: a partir
de $n \geq 7$ con $m \geq 4$ se obtienen aceleraciones consistentes de entre $+22\%$ y
$+63\%$, y la tendencia indica que la ganancia seguirá creciendo con instancias más
grandes, acercándose al límite teórico de $+100\%$ que predice la ley de Amdahl para
$p = 2$ con $\alpha \to 1$.

La conclusión general es que vale la pena paralelizar la búsqueda óptima cuando el
espacio de candidatas supera aproximadamente $10\,000$ asignaciones ($m^n \gtrsim 10^4$),
y que los conteos individuales deben mantenerse en su versión secuencial para instancias
del tamaño evaluado en este proyecto.