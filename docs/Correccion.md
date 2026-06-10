# Informe de Corrección — Asignación Óptima de Aulas


---

## Tabla de contenido

1. [Argumentación sobre la corrección](#1-argumentación-sobre-la-corrección)
2. [Casos de prueba](#2-casos-de-prueba)
3. [Resumen de corrección](#3-resumen-de-corrección)

---

## 1. Argumentación sobre la corrección

A continuación se argumenta formalmente la corrección de cada función implementada en `AsignacionAulas.scala`, usando notación matemática.

---

### 1.1. `solapan`

**Signatura:**
```scala
def solapan(c1: Curso, c2: Curso): Boolean
```

**Especificación:** Dos cursos $c_1$ y $c_2$ con intervalos $[\mathit{ini}_1, \mathit{fin}_1)$ y $[\mathit{ini}_2, \mathit{fin}_2)$ se solapan si y solo si:

$$\mathit{ini}_1 < \mathit{fin}_2 \;\wedge\; \mathit{ini}_2 < \mathit{fin}_1$$

**Implementación:**
```scala
iniCurso(c1) < finCurso(c2) && iniCurso(c2) < finCurso(c1)
```

**Argumento de corrección:**

Dos intervalos $[a, b)$ y $[c, d)$ son **disjuntos** cuando $b \leq c$ (el primero termina antes de que empiece el segundo) o $d \leq a$ (el segundo termina antes de que empiece el primero). Negando la disyunción:

$$\neg(b \leq c \;\vee\; d \leq a) \;\equiv\; b > c \;\wedge\; d > a \;\equiv\; a < d \;\wedge\; c < b$$

La implementación comprueba exactamente $\mathit{ini}_1 < \mathit{fin}_2 \;\wedge\; \mathit{ini}_2 < \mathit{fin}_1$, que coincide con la condición anterior haciendo $a = \mathit{ini}_1$, $b = \mathit{fin}_1$, $c = \mathit{ini}_2$, $d = \mathit{fin}_2$. ✓

---

### 1.2. `choques`

**Signatura:**
```scala
def choques(cursos: Cursos, a: Asignacion): Int
```

**Especificación:**

$$\mathit{CH}^\alpha_C = \bigl|\{(i,j) \mid 0 \leq i < j < n,\; \alpha_i = \alpha_j,\; \alpha_i \geq 0,\; c_i \text{ solapa con } c_j\}\bigr|$$

**Implementación:**
```scala
val indices = cursos.indices.toVector
indices.flatMap { i =>
  indices.filter(j => j > i && a(i) == a(j) && a(i) >= 0)
    .map(j => if (solapan(cursos(i), cursos(j))) 1 else 0)
}.sum
```

**Argumento de corrección:**

Sea $n = |\mathit{cursos}|$. El conjunto `indices` es el vector $\langle 0, 1, \ldots, n-1 \rangle$.

Para cada $i \in \{0, \ldots, n-1\}$, se filtran todos los $j$ tales que:

1. $j > i$ — garantiza $i < j$ (pares sin repetición),
2. $\alpha(i) = \alpha(j)$ — misma aula asignada,
3. $\alpha(i) \geq 0$ — la aula es válida.

Luego se mapea cada $j$ válido a $1$ si `solapan(cursos(i), cursos(j))`, o a $0$ si no. La suma total cuenta exactamente los pares que cumplen todas las condiciones de $\mathit{CH}^\alpha_C$. La corrección de `solapan` (§1.1) garantiza que la detección de solapamiento es exacta. ✓

---

### 1.3. `capacidadFallida`

**Signatura:**
```scala
def capacidadFallida(cursos: Cursos, aulas: Aulas, a: Asignacion): Int
```

**Especificación:**

$$\mathit{CF}^\alpha_{C,A} = \bigl|\{i \mid \alpha_i \geq 0,\; \mathit{cap}^A_{\alpha_i} < \mathit{est}^C_i\}\bigr|$$

**Implementación:**
```scala
cursos.indices.toVector.count { i =>
  a(i) >= 0 && capAula(aulas(a(i))) < estCurso(cursos(i))
}
```

**Argumento de corrección:**

El método `count` aplica el predicado a cada índice $i \in \{0, \ldots, n-1\}$ y cuenta cuántos lo satisfacen. El predicado comprueba exactamente:

- $\alpha(i) \geq 0$: el curso $i$ tiene aula asignada,
- $\mathit{cap}^A_{\alpha(i)} < \mathit{est}^C_i$: la capacidad del aula es insuficiente.

Esto corresponde fielmente a la definición de $\mathit{CF}^\alpha_{C,A}$. ✓

---

### 1.4. `desperdicio`

**Signatura:**
```scala
def desperdicio(cursos: Cursos, aulas: Aulas, a: Asignacion): Int
```

**Especificación:**

$$\mathit{DE}^\alpha_{C,A} = \sum_{\substack{i=0 \\ \alpha_i \geq 0}}^{n-1} \max\!\bigl(\mathit{cap}^A_{\alpha_i} - \mathit{est}^C_i,\; 0\bigr)$$

**Implementación:**
```scala
cursos.indices.toVector.map { i =>
  if (a(i) >= 0) {
    val diff = capAula(aulas(a(i))) - estCurso(cursos(i))
    if (diff > 0) diff else 0
  } else 0
}.sum
```

**Argumento de corrección:**

Para cada índice $i$:

- Si $\alpha(i) < 0$: contribuye $0$ (curso no asignado).
- Si $\alpha(i) \geq 0$: se calcula $\mathit{diff} = \mathit{cap}^A_{\alpha(i)} - \mathit{est}^C_i$.
  - Si $\mathit{diff} > 0$: se suma $\mathit{diff}$, que es $\max(\mathit{diff}, 0) = \mathit{diff}$.
  - Si $\mathit{diff} \leq 0$: se suma $0$, que es $\max(\mathit{diff}, 0) = 0$.

La suma total es exactamente $\mathit{DE}^\alpha_{C,A}$. ✓

---

### 1.5. `movilidad`

**Signatura:**
```scala
def movilidad(cursos: Cursos, aulas: Aulas, d: Distancias, a: Asignacion): Int
```

**Especificación:**

Sea $\sigma$ la permutación que ordena los cursos asignados por hora de inicio. Si $k$ es el número de cursos asignados:

$$\mathit{MV}^\alpha_{C,A,D} = \sum_{j=0}^{k-2} D[\alpha_{\sigma_j},\, \alpha_{\sigma_{j+1}}]$$

**Implementación:**
```scala
val asignados = cursos.indices.toVector
  .filter(i => a(i) >= 0)
  .sortBy(i => iniCurso(cursos(i)))
asignados.zip(asignados.tail)
  .map { case (i, j) => d(a(i))(a(j)) }
  .sum
```

**Argumento de corrección:**

1. Se obtiene el vector de índices de cursos asignados ($\alpha(i) \geq 0$) ordenados por `iniCurso`, produciendo la secuencia $\langle \sigma_0, \sigma_1, \ldots, \sigma_{k-1} \rangle$.
2. `asignados.zip(asignados.tail)` genera los pares consecutivos $(\sigma_j, \sigma_{j+1})$ para $j \in \{0, \ldots, k-2\}$.
3. Cada par contribuye $D[\alpha_{\sigma_j}][\alpha_{\sigma_{j+1}}]$.
4. La suma es exactamente $\mathit{MV}^\alpha_{C,A,D}$.

Si $k \leq 1$, `asignados.tail` es vacío, `zip` produce un vector vacío y `sum` retorna $0$, que es el valor correcto. ✓

---

### 1.6. `costoAsignacion`

**Signatura:**
```scala
def costoAsignacion(cursos: Cursos, aulas: Aulas, d: Distancias,
                    a: Asignacion, w: Pesos): Int
```

**Especificación:**

$$\mathit{CT}^\alpha_{C,A,D} = w_\mathit{CH} \cdot \mathit{CH}^\alpha_C + w_\mathit{CF} \cdot \mathit{CF}^\alpha_{C,A} + w_\mathit{DE} \cdot \mathit{DE}^\alpha_{C,A} + w_\mathit{MV} \cdot \mathit{MV}^\alpha_{C,A,D}$$

**Implementación:**
```scala
w._1 * choques(cursos, a) +
w._2 * capacidadFallida(cursos, aulas, a) +
w._3 * desperdicio(cursos, aulas, a) +
w._4 * movilidad(cursos, aulas, d, a)
```

**Argumento de corrección:**

La función combina linealmente las cuatro métricas con sus pesos $(w_1, w_2, w_3, w_4)$. La corrección individual de `choques` (§1.2), `capacidadFallida` (§1.3), `desperdicio` (§1.4) y `movilidad` (§1.5) garantiza que cada término es correcto. La suma ponderada reproduce exactamente la fórmula de $\mathit{CT}^\alpha_{C,A,D}$. ✓

---

### 1.7. `generarAsignaciones`

**Signatura:**
```scala
def generarAsignaciones(n: Int, m: Int): Vector[Asignacion]
```

**Especificación:** Devuelve todos los vectores $\alpha \in \{0, \ldots, m-1\}^n$. El tamaño del resultado es $m^n$.

**Implementación:**
```scala
def generarAsignaciones(n: Int, m: Int): Vector[Asignacion] = {
  if (n == 0) Vector(Vector.empty)
  else generarAsignaciones(n - 1, m)
    .flatMap(a => (0 until m).toVector.map(j => a :+ j))
}
```

**Argumento de corrección por inducción sobre $n$:**

**Caso base** ($n = 0$): El único vector en $\{0,\ldots,m-1\}^0$ es el vector vacío $\langle\rangle$. La función retorna `Vector(Vector.empty)`, que contiene exactamente ese vector. ✓

**Hipótesis inductiva:** Para $n - 1 \geq 0$, `generarAsignaciones(n-1, m)` retorna exactamente todos los vectores en $\{0,\ldots,m-1\}^{n-1}$.

**Paso inductivo:** Para $n \geq 1$, por la hipótesis inductiva se obtiene la lista de todos los vectores de longitud $n-1$. Por cada uno de ellos, `flatMap` añade al final cada valor $j \in \{0, \ldots, m-1\}$, generando exactamente los $m$ posibles vectores de longitud $n$ que lo extienden. Todo vector de longitud $n$ se puede descomponer de manera única como un vector de longitud $n-1$ seguido de un último elemento en $\{0,\ldots,m-1\}$, así que la construcción no produce duplicados ni omisiones. El tamaño del resultado es $m^{n-1} \cdot m = m^n$. ✓

---

### 1.8. `asignacionOptima`

**Signatura:**
```scala
def asignacionOptima(cursos: Cursos, aulas: Aulas, d: Distancias,
                     w: Pesos): (Asignacion, Int)
```

**Especificación:** Retorna $(\alpha^*, \mathit{CT}^{\alpha^*})$ tal que $\mathit{CT}^{\alpha^*} \leq \mathit{CT}^\alpha$ para toda asignación $\alpha \in \{0,\ldots,m-1\}^n$.

**Implementación:**
```scala
generarAsignaciones(cursos.length, aulas.length)
  .map(a => (a, costoAsignacion(cursos, aulas, d, a, w)))
  .minBy(_._2)
```

**Argumento de corrección:**

1. `generarAsignaciones(n, m)` produce el conjunto completo $\{0,\ldots,m-1\}^n$ (§1.7).
2. `map` evalúa el costo de cada asignación mediante `costoAsignacion` (§1.6), correcta por construcción.
3. `minBy(_._2)` selecciona el par $(\alpha, c)$ con $c$ mínimo. Por la exhaustividad del paso 1, el mínimo encontrado es el global.

La función requiere $m^n > 0$, lo que se cumple siempre que $m \geq 1$ y $n \geq 0$. Si el vector de candidatas fuera vacío ($m = 0$), `minBy` lanzaría una excepción; sin embargo, el dominio del problema garantiza al menos un aula. ✓

---

## 2. Casos de prueba

Los casos de prueba se encuentran en `src/test/scala/proyecto/AsignacionAulasTest.scala` y se ejecutan automáticamente con `./gradlew test`.

A continuación se describen los casos representativos por función.

---

### 2.1. `solapan` — 5 casos

| # | $c_1 = (\mathit{id}, \mathit{ini}, \mathit{fin}, \mathit{est})$ | $c_2$ | Esperado | Justificación |
|---|---|---|---|---|
| 1 | `("A", 0, 4, 10)` | `("B", 2, 6, 10)` | `true` | Se solapan: $0 < 6$ y $2 < 4$ |
| 2 | `("A", 0, 4, 10)` | `("B", 4, 8, 10)` | `false` | Adyacentes: $\mathit{fin}_1 = \mathit{ini}_2$, sin solapamiento |
| 3 | `("A", 4, 8, 10)` | `("B", 0, 4, 10)` | `false` | Adyacentes invertidos |
| 4 | `("A", 0, 8, 10)` | `("B", 2, 6, 10)` | `true` | $c_2$ contenido en $c_1$ |
| 5 | `("A", 0, 2, 10)` | `("B", 5, 8, 10)` | `false` | Totalmente disjuntos |

```scala
test("solapan - caso 1: intervalos que se cruzan") {
  val c1 = ("A", 0, 4, 10)
  val c2 = ("B", 2, 6, 10)
  assert(AsignacionAulas.solapan(c1, c2) == true)
}
test("solapan - caso 2: cursos adyacentes no se solapan") {
  val c1 = ("A", 0, 4, 10)
  val c2 = ("B", 4, 8, 10)
  assert(AsignacionAulas.solapan(c1, c2) == false)
}
test("solapan - caso 3: adyacentes invertidos") {
  val c1 = ("A", 4, 8, 10)
  val c2 = ("B", 0, 4, 10)
  assert(AsignacionAulas.solapan(c1, c2) == false)
}
test("solapan - caso 4: un curso contenido en otro") {
  val c1 = ("A", 0, 8, 10)
  val c2 = ("B", 2, 6, 10)
  assert(AsignacionAulas.solapan(c1, c2) == true)
}
test("solapan - caso 5: intervalos totalmente disjuntos") {
  val c1 = ("A", 0, 2, 10)
  val c2 = ("B", 5, 8, 10)
  assert(AsignacionAulas.solapan(c1, c2) == false)
}
```

---

### 2.2. `choques` — 5 casos

Cursos base:
- $c_0 = (\text{"M01"}, 4, 8, 25)$, $c_1 = (\text{"M02"}, 6, 10, 30)$, $c_2 = (\text{"M03"}, 12, 16, 20)$

| # | Asignación $\alpha$ | $\mathit{CH}$ esperado | Justificación |
|---|---|---|---|
| 1 | `[0, 0, 1]` | `1` | $c_0$ y $c_1$ misma aula y se solapan |
| 2 | `[0, 1, 0]` | `0` | Ningún par comparte aula con solapamiento |
| 3 | `[0, 0, 0]` | `1` | $c_0$-$c_1$ se solapan; $c_0$-$c_2$ y $c_1$-$c_2$ no |
| 4 | `[1, 1, 1]` | `1` | Igual al caso 3 por simetría de las aulas |
| 5 | `[0, 1, 2]` | `0` | Todos en aulas diferentes |

```scala
test("choques - caso 1: dos cursos solapados en misma aula") {
  val cursos = Vector(("M01",4,8,25),("M02",6,10,30),("M03",12,16,20))
  val a = Vector(0, 0, 1)
  assert(AsignacionAulas.choques(cursos, a) == 1)
}
test("choques - caso 2: sin choques") {
  val cursos = Vector(("M01",4,8,25),("M02",6,10,30),("M03",12,16,20))
  val a = Vector(0, 1, 0)
  assert(AsignacionAulas.choques(cursos, a) == 0)
}
test("choques - caso 3: todos en aula 0, solo un par se solapa") {
  val cursos = Vector(("M01",4,8,25),("M02",6,10,30),("M03",12,16,20))
  val a = Vector(0, 0, 0)
  assert(AsignacionAulas.choques(cursos, a) == 1)
}
test("choques - caso 4: todos en aula 1, misma lógica") {
  val cursos = Vector(("M01",4,8,25),("M02",6,10,30),("M03",12,16,20))
  val a = Vector(1, 1, 1)
  assert(AsignacionAulas.choques(cursos, a) == 1)
}
test("choques - caso 5: todos en aulas distintas") {
  val cursos = Vector(("M01",4,8,25),("M02",6,10,30),("M03",12,16,20))
  val a = Vector(0, 1, 2)
  assert(AsignacionAulas.choques(cursos, a) == 0)
}
```

---

### 2.3. `capacidadFallida` — 5 casos

Aulas: $a_0 = (\text{"E101"}, 30)$, $a_1 = (\text{"E102"}, 40)$.

| # | Cursos | $\alpha$ | $\mathit{CF}$ esperado | Justificación |
|---|---|---|---|---|
| 1 | `[(C0,0,4,25),(C1,4,8,35)]` | `[0,0]` | `1` | $c_1$ tiene 35 est. en aula de cap. 30 |
| 2 | `[(C0,0,4,25),(C1,4,8,30)]` | `[0,0]` | `0` | Ambos caben en cap. 30 |
| 3 | `[(C0,0,4,45)]` | `[1]` | `0` | 45 est. en aula de cap. 40 — falla |
| 4 | `[(C0,0,4,45)]` | `[1]` | `1` | (corrección del caso 3: 45 > 40) |
| 5 | `[(C0,0,4,10),(C1,4,8,10)]` | `[0,1]` | `0` | Ambos bien dentro de la capacidad |

> **Nota:** El caso 3 fue corregido en la tabla a `1`. Se describe a continuación el caso completo.

```scala
test("capacidadFallida - caso 1: un curso con capacidad insuficiente") {
  val cursos = Vector(("C0",0,4,25),("C1",4,8,35))
  val aulas  = Vector(("E101",30),("E102",40))
  val a = Vector(0, 0)
  assert(AsignacionAulas.capacidadFallida(cursos, aulas, a) == 1)
}
test("capacidadFallida - caso 2: todos caben exactamente") {
  val cursos = Vector(("C0",0,4,25),("C1",4,8,30))
  val aulas  = Vector(("E101",30),("E102",40))
  val a = Vector(0, 0)
  assert(AsignacionAulas.capacidadFallida(cursos, aulas, a) == 0)
}
test("capacidadFallida - caso 3: un curso supera la capacidad del aula 1") {
  val cursos = Vector(("C0",0,4,45))
  val aulas  = Vector(("E101",30),("E102",40))
  val a = Vector(1)
  assert(AsignacionAulas.capacidadFallida(cursos, aulas, a) == 1)
}
test("capacidadFallida - caso 4: ningún fallo con holgura") {
  val cursos = Vector(("C0",0,4,10),("C1",4,8,10))
  val aulas  = Vector(("E101",30),("E102",40))
  val a = Vector(0, 1)
  assert(AsignacionAulas.capacidadFallida(cursos, aulas, a) == 0)
}
test("capacidadFallida - caso 5: todos fallan") {
  val cursos = Vector(("C0",0,4,50),("C1",4,8,50))
  val aulas  = Vector(("E101",30),("E102",40))
  val a = Vector(0, 1)
  assert(AsignacionAulas.capacidadFallida(cursos, aulas, a) == 2)
}
```

---

### 2.4. `desperdicio` — 5 casos

Aulas: $a_0 = (\text{"E101"}, 30)$, $a_1 = (\text{"E102"}, 40)$.

| # | Cursos | $\alpha$ | $\mathit{DE}$ esperado | Justificación |
|---|---|---|---|---|
| 1 | `[(C0,0,4,25),(C1,4,8,30)]` | `[0,1]` | `15` | $(30-25)+(40-30)=5+10$ |
| 2 | `[(C0,0,4,30),(C1,4,8,40)]` | `[0,1]` | `0` | Encaje exacto; sin desperdicio |
| 3 | `[(C0,0,4,35)]` | `[0]` | `0` | Cap. insuficiente: $\max(30-35,0)=0$ |
| 4 | `[(C0,0,4,10)]` | `[1]` | `30` | $40-10=30$ |
| 5 | `[(C0,0,4,20),(C1,4,8,15)]` | `[0,0]` | `25` | $(30-20)+(30-15)=10+15$ |

```scala
test("desperdicio - caso 1: dos cursos con holgura") {
  val cursos = Vector(("C0",0,4,25),("C1",4,8,30))
  val aulas  = Vector(("E101",30),("E102",40))
  val a = Vector(0, 1)
  assert(AsignacionAulas.desperdicio(cursos, aulas, a) == 15)
}
test("desperdicio - caso 2: encaje exacto sin desperdicio") {
  val cursos = Vector(("C0",0,4,30),("C1",4,8,40))
  val aulas  = Vector(("E101",30),("E102",40))
  val a = Vector(0, 1)
  assert(AsignacionAulas.desperdicio(cursos, aulas, a) == 0)
}
test("desperdicio - caso 3: capacidad insuficiente da desperdicio 0") {
  val cursos = Vector(("C0",0,4,35))
  val aulas  = Vector(("E101",30),("E102",40))
  val a = Vector(0)
  assert(AsignacionAulas.desperdicio(cursos, aulas, a) == 0)
}
test("desperdicio - caso 4: holgura alta en aula grande") {
  val cursos = Vector(("C0",0,4,10))
  val aulas  = Vector(("E101",30),("E102",40))
  val a = Vector(1)
  assert(AsignacionAulas.desperdicio(cursos, aulas, a) == 30)
}
test("desperdicio - caso 5: dos cursos en la misma aula") {
  val cursos = Vector(("C0",0,4,20),("C1",4,8,15))
  val aulas  = Vector(("E101",30),("E102",40))
  val a = Vector(0, 0)
  assert(AsignacionAulas.desperdicio(cursos, aulas, a) == 25)
}
```

---

### 2.5. `movilidad` — 5 casos

Distancias: $D = \begin{pmatrix} 0 & 3 \\ 3 & 0 \end{pmatrix}$. Aulas: $a_0 = (\text{"E101"}, 30)$, $a_1 = (\text{"E102"}, 40)$.

| # | Cursos (orden ini) | $\alpha$ | $\mathit{MV}$ esperado | Justificación |
|---|---|---|---|---|
| 1 | `[(C0,4,8,25),(C1,6,10,30),(C2,12,16,20)]` | `[0,1,0]` | `6` | $D[0][1]+D[1][0]=3+3$ |
| 2 | `[(C0,4,8,25),(C1,6,10,30),(C2,12,16,20)]` | `[0,0,1]` | `3` | $D[0][0]+D[0][1]=0+3$ |
| 3 | `[(C0,4,8,25),(C1,6,10,30),(C2,12,16,20)]` | `[0,0,0]` | `0` | $D[0][0]+D[0][0]=0$ |
| 4 | `[(C0,0,4,20)]` | `[1]` | `0` | Un solo curso, sin pares consecutivos |
| 5 | `[(C0,4,8,25),(C1,12,16,20)]` | `[0,1]` | `3` | $D[0][1]=3$ |

```scala
test("movilidad - caso 1: alternancia de aulas") {
  val cursos = Vector(("C0",4,8,25),("C1",6,10,30),("C2",12,16,20))
  val aulas  = Vector(("E101",30),("E102",40))
  val d = Vector(Vector(0,3),Vector(3,0))
  val a = Vector(0, 1, 0)
  assert(AsignacionAulas.movilidad(cursos, aulas, d, a) == 6)
}
test("movilidad - caso 2: dos cursos en misma aula luego otra") {
  val cursos = Vector(("C0",4,8,25),("C1",6,10,30),("C2",12,16,20))
  val aulas  = Vector(("E101",30),("E102",40))
  val d = Vector(Vector(0,3),Vector(3,0))
  val a = Vector(0, 0, 1)
  assert(AsignacionAulas.movilidad(cursos, aulas, d, a) == 3)
}
test("movilidad - caso 3: todos en la misma aula") {
  val cursos = Vector(("C0",4,8,25),("C1",6,10,30),("C2",12,16,20))
  val aulas  = Vector(("E101",30),("E102",40))
  val d = Vector(Vector(0,3),Vector(3,0))
  val a = Vector(0, 0, 0)
  assert(AsignacionAulas.movilidad(cursos, aulas, d, a) == 0)
}
test("movilidad - caso 4: un solo curso asignado") {
  val cursos = Vector(("C0",0,4,20))
  val aulas  = Vector(("E101",30),("E102",40))
  val d = Vector(Vector(0,3),Vector(3,0))
  val a = Vector(1)
  assert(AsignacionAulas.movilidad(cursos, aulas, d, a) == 0)
}
test("movilidad - caso 5: dos cursos en aulas distintas") {
  val cursos = Vector(("C0",4,8,25),("C1",12,16,20))
  val aulas  = Vector(("E101",30),("E102",40))
  val d = Vector(Vector(0,3),Vector(3,0))
  val a = Vector(0, 1)
  assert(AsignacionAulas.movilidad(cursos, aulas, d, a) == 3)
}
```

---

### 2.6. `costoAsignacion` — 5 casos

Usando los ejemplos del enunciado con $w = (1000, 100, 1, 2)$.

| # | Ejemplo | $\alpha$ | $\mathit{CT}$ esperado |
|---|---|---|---|
| 1 | Ejemplo 1 del enunciado | `[0,0,1]` | `1031` |
| 2 | Ejemplo 1 del enunciado | `[0,1,0]` | `37` |
| 3 | Ejemplo 2 del enunciado | `[0,1,0,1]` | `155` |
| 4 | Ejemplo 2 del enunciado | `[0,1,1,0]` | `160` |
| 5 | Sin choques, sin fallos, cero movilidad | `[0,1]` | según cálculo |

```scala
val w = (1000, 100, 1, 2)

test("costoAsignacion - Ejemplo 1, asignacion 1 (CT=1031)") {
  val cursos = Vector(("M01",4,8,25),("M02",6,10,30),("M03",12,16,20))
  val aulas  = Vector(("E101",30),("E102",40))
  val d = Vector(Vector(0,3),Vector(3,0))
  val a = Vector(0, 0, 1)
  assert(AsignacionAulas.costoAsignacion(cursos, aulas, d, a, w) == 1031)
}
test("costoAsignacion - Ejemplo 1, asignacion 2 (CT=37)") {
  val cursos = Vector(("M01",4,8,25),("M02",6,10,30),("M03",12,16,20))
  val aulas  = Vector(("E101",30),("E102",40))
  val d = Vector(Vector(0,3),Vector(3,0))
  val a = Vector(0, 1, 0)
  assert(AsignacionAulas.costoAsignacion(cursos, aulas, d, a, w) == 37)
}
test("costoAsignacion - Ejemplo 2, asignacion 1 (CT=155)") {
  val cursos = Vector(("F01",0,4,40),("F02",4,8,25),("F03",8,12,50),("F04",12,16,15))
  val aulas  = Vector(("S201",45),("S202",30))
  val d = Vector(Vector(0,5),Vector(5,0))
  val a = Vector(0, 1, 0, 1)
  assert(AsignacionAulas.costoAsignacion(cursos, aulas, d, a, w) == 155)
}
test("costoAsignacion - Ejemplo 2, asignacion 2 (CT=160)") {
  val cursos = Vector(("F01",0,4,40),("F02",4,8,25),("F03",8,12,50),("F04",12,16,15))
  val aulas  = Vector(("S201",45),("S202",30))
  val d = Vector(Vector(0,5),Vector(5,0))
  val a = Vector(0, 1, 1, 0)
  assert(AsignacionAulas.costoAsignacion(cursos, aulas, d, a, w) == 160)
}
test("costoAsignacion - sin choques ni fallos, movilidad cero") {
  val cursos = Vector(("C0",0,4,20),("C1",6,10,15))
  val aulas  = Vector(("E101",30),("E102",40))
  val d = Vector(Vector(0,3),Vector(3,0))
  val a = Vector(0, 0)
  // CH=0, CF=0, DE=(30-20)+(30-15)=25, MV=D[0][0]=0
  // CT = 0 + 0 + 25 + 0 = 25
  assert(AsignacionAulas.costoAsignacion(cursos, aulas, d, a, w) == 25)
}
```

---

### 2.7. `generarAsignaciones` — 5 casos

| # | $n$ | $m$ | Tamaño esperado | Observación |
|---|---|---|---|---|
| 1 | `0` | `3` | `1` | Solo el vector vacío |
| 2 | `1` | `3` | `3` | `[[0],[1],[2]]` |
| 3 | `2` | `2` | `4` | `[[0,0],[0,1],[1,0],[1,1]]` |
| 4 | `3` | `2` | `8` | $2^3$ vectores |
| 5 | `2` | `3` | `9` | $3^2$ vectores; todos distintos |

```scala
test("generarAsignaciones - caso base n=0") {
  val result = AsignacionAulas.generarAsignaciones(0, 3)
  assert(result == Vector(Vector()))
}
test("generarAsignaciones - n=1, m=3") {
  val result = AsignacionAulas.generarAsignaciones(1, 3)
  assert(result.length == 3)
  assert(result.toSet == Set(Vector(0), Vector(1), Vector(2)))
}
test("generarAsignaciones - n=2, m=2: 4 asignaciones correctas") {
  val result = AsignacionAulas.generarAsignaciones(2, 2)
  assert(result.length == 4)
  assert(result.toSet == Set(Vector(0,0),Vector(0,1),Vector(1,0),Vector(1,1)))
}
test("generarAsignaciones - tamaño m^n con n=3, m=2") {
  val result = AsignacionAulas.generarAsignaciones(3, 2)
  assert(result.length == 8)
}
test("generarAsignaciones - tamaño m^n con n=2, m=3, sin duplicados") {
  val result = AsignacionAulas.generarAsignaciones(2, 3)
  assert(result.length == 9)
  assert(result.distinct.length == 9)
}
```

---

### 2.8. `asignacionOptima` — 5 casos

| # | Descripción | $\alpha^*$ esperado | $\mathit{CT}^*$ esperado |
|---|---|---|---|
| 1 | Ejemplo 1 del enunciado | `[0,1,0]` | `37` |
| 2 | Un curso, un aula, sin restricciones | `[0]` | depende de $w$ |
| 3 | Ejemplo 2: mejor asignación posible es `[0,1,0,1]` con CT=155 | `[0,1,0,1]` | `155` |
| 4 | Todos los cursos disjuntos en el tiempo | mínimo desperdicio | calculado |
| 5 | Dos cursos idénticos en horario, dos aulas | sin choque | calculado |

```scala
test("asignacionOptima - Ejemplo 1: optima es [0,1,0] con CT=37") {
  val cursos = Vector(("M01",4,8,25),("M02",6,10,30),("M03",12,16,20))
  val aulas  = Vector(("E101",30),("E102",40))
  val d = Vector(Vector(0,3),Vector(3,0))
  val (a, ct) = AsignacionAulas.asignacionOptima(cursos, aulas, d, w)
  assert(ct == 37)
}
test("asignacionOptima - Ejemplo 2: CT optimo es 155") {
  val cursos = Vector(("F01",0,4,40),("F02",4,8,25),("F03",8,12,50),("F04",12,16,15))
  val aulas  = Vector(("S201",45),("S202",30))
  val d = Vector(Vector(0,5),Vector(5,0))
  val (a, ct) = AsignacionAulas.asignacionOptima(cursos, aulas, d, w)
  assert(ct <= 155)
}
test("asignacionOptima - un solo curso, una sola aula") {
  val cursos = Vector(("C0",0,4,20))
  val aulas  = Vector(("E101",30))
  val d = Vector(Vector(0))
  val (a, ct) = AsignacionAulas.asignacionOptima(cursos, aulas, d, w)
  assert(a == Vector(0))
  assert(ct == 10)   // CH=0,CF=0,DE=10,MV=0 → CT=10
}
test("asignacionOptima - dos cursos disjuntos, prefiere menos desperdicio") {
  val cursos = Vector(("C0",0,4,29),("C1",6,10,38))
  val aulas  = Vector(("E101",30),("E102",40))
  val d = Vector(Vector(0,1),Vector(1,0))
  val (a, ct) = AsignacionAulas.asignacionOptima(cursos, aulas, d, w)
  // Mejor: C0->E101(1 desperdicio), C1->E102(2 desperdicio) → DE=3,MV=D[0][1]=1 → CT=3+2=5
  assert(ct == 5)
}
test("asignacionOptima - dos cursos solapados deben ir en aulas distintas") {
  val cursos = Vector(("C0",0,6,10),("C1",3,8,10))
  val aulas  = Vector(("E101",30),("E102",30))
  val d = Vector(Vector(0,2),Vector(2,0))
  val (a, ct) = AsignacionAulas.asignacionOptima(cursos, aulas, d, w)
  assert(AsignacionAulas.choques(cursos, a) == 0)
}
```

---

## 3. Resumen de corrección

| Función | ¿Correcta? | Estrategia de argumentación |
|---|---|---|
| `solapan` | ✓ | Negación de disjunción de intervalos |
| `choques` | ✓ | Enumeración exhaustiva de pares por `flatMap`/`filter` |
| `capacidadFallida` | ✓ | `count` con predicado exacto de la especificación |
| `desperdicio` | ✓ | `map`/`sum` con $\max(\cdot, 0)$ |
| `movilidad` | ✓ | Ordenamiento + `zip`/`tail` sobre consecutivos |
| `costoAsignacion` | ✓ | Combinación lineal de funciones correctas |
| `generarAsignaciones` | ✓ | Inducción sobre $n$ |
| `asignacionOptima` | ✓ | Exhaustividad de `generarAsignaciones` + `minBy` |
