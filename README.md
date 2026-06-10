[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/XgbWPkKq)

# Proyecto Final — Asignación Óptima de Aulas

## Fundamentos de Programación Funcional y Concurrente

**Fecha de entrega:** Miércoles, 03 de junio de 2026 a las 17:58:59
**Sustentación:** Jueves, 04 de junio de 2026 en horario de clase
**Docente:** Carlos Andres Delgado S — carlos.andres.delgado@correounivalle.edu.co

---

## Integrantes del grupo


| Nombre completo           | Código  | Correo institucional                            |
|---------------------------|---------|------------------------------------------------|
| Rafael Steven Izquierdo   | 2459696 | rafael.izquierdo@correounivalle.edu.co          |
| Walter Lozano Pelaez      | 2459487 | walter.lozano@correounivalle.edu.co             |
| Sebastian Cardona Murillo | 2459714 | sebastian.cardona.murillo@correounivalle.edu.co |
| Kevin Smith Bermeo Rico   | 2477363 | Kevin.bermeo@correounivalle.edu.co              |

**Obligatorio:** editar esta tabla con los datos reales de todos los integrantes.
Si un integrante no aparece aquí, su nota individual será 0.0.

---

## Descripción

El proyecto consiste en implementar, en Scala, una solución funcional y paralela al
**problema de la asignación óptima de aulas**: dado un conjunto de cursos y un conjunto
de aulas, encontrar la asignación que minimiza una función de costo que combina choques
de horario, desperdicio de capacidad y distancia de movilidad entre aulas consecutivas.

El enunciado completo está disponible en el campus virtual.

---

## Estructura del repositorio

```
proyecto-final-2026-1/
├── .github/workflows/      ← CI — NO EDITAR
├── app/
│   ├── build.gradle        ← NO EDITAR
│   ├── scalastyle_config.xml ← NO EDITAR
│   └── src/
│       ├── main/scala/
│       │   ├── proyecto/
│       │   │   ├── App.scala
│       │   │   ├── AsignacionAulas.scala     ← versión secuencial
│       │   │   └── AsignacionAulasPar.scala  ← versión paralela
│       │   └── common/
│       │       └── package.scala             ← NO EDITAR
│       └── test/scala/proyecto/
│           ├── AsignacionAulasTest.scala
│           └── AsignacionAulasParTest.scala
├── docs/
│   ├── Proceso.md          ← informe de proceso (completar)
│   ├── Correccion.md       ← informe de corrección (completar)
│   ├── Paralelizacion.md   ← informe de paralelización (completar)
│   ├── Conclusiones.md     ← conclusiones (completar)
│   └── GuiaMarkdown.md     ← referencia de sintaxis Markdown/LaTeX/Mermaid
├── gradle/                 ← NO EDITAR
├── gradlew                 ← NO EDITAR
├── gradlew.bat             ← NO EDITAR
├── settings.gradle         ← NO EDITAR
└── README.md               ← EDITAR con integrantes
```

---

## Cómo correr el proyecto

```bash
# Compilar
./gradlew compileScala

# Ejecutar pruebas
./gradlew test

# Ver reporte de pruebas
open app/build/reports/tests/test/index.html
```

---

## Reglas

- No usar variables mutables, ciclos `for`/`while` ni `return`.
- No modificar `build.gradle`, `settings.gradle`, `gradlew`, `gradlew.bat`,
  `gradle/`, `.github/workflows/`, `scalastyle_config.xml` ni el paquete `common`.
- Los informes van en `docs/` en formato Markdown; no se aceptan otros formatos.
- Usar notación matemática en LaTeX dentro de los Markdown.
- Diagramas de pila de llamadas con Mermaid (no imágenes).
