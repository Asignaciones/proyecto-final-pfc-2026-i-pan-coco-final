package proyecto

import common._
import AsignacionAulas._

object AsignacionAulasPar {

  def choquesPar(cursos: Cursos, a: Asignacion): Int = {
    val n    = cursos.length
    val mid  = n / 2
    val indices = cursos.indices.toVector

    // Cuenta los choques para los índices i en el rango [desde, hasta)
    def choquesRango(desde: Int, hasta: Int): Int =
      (desde until hasta).toVector.flatMap { i =>
        indices
          .filter(j => j > i && a(i) == a(j) && a(i) >= 0)
          .map(j => if (solapan(cursos(i), cursos(j))) 1 else 0)
      }.sum

    val (izq, der) = parallel(
      choquesRango(0, mid),
      choquesRango(mid, n)
    )
    izq + der
  }

  def desperdicioPar(cursos: Cursos, aulas: Aulas, a: Asignacion): Int = {
    val n   = cursos.length
    val mid = n / 2

    def desperdicioRango(desde: Int, hasta: Int): Int =
      (desde until hasta).toVector.map { i =>
        if (a(i) >= 0) {
          val diff = capAula(aulas(a(i))) - estCurso(cursos(i))
          if (diff > 0) diff else 0
        } else 0
      }.sum

    val (izq, der) = parallel(
      desperdicioRango(0, mid),
      desperdicioRango(mid, n)
    )
    izq + der
  }

  def movilidadPar(cursos: Cursos, aulas: Aulas, d: Distancias,
                   a: Asignacion): Int = {
    val n   = cursos.length
    val mid = n / 2

    // Obtiene los índices de cursos asignados en [desde, hasta), ordenados
    // por hora de inicio.
    def indicesOrdenados(desde: Int, hasta: Int): Vector[Int] =
      (desde until hasta).toVector
        .filter(i => a(i) >= 0)
        .sortBy(i => iniCurso(cursos(i)))

    val (izq, der) = parallel(
      indicesOrdenados(0, mid),
      indicesOrdenados(mid, n)
    )

    // Merge de las dos secuencias ordenadas por iniCurso
    def merge(xs: Vector[Int], ys: Vector[Int]): Vector[Int] =
      (xs, ys) match {
        case (Vector(), _) => ys
        case (_, Vector()) => xs
        case (xh +: xt, yh +: yt) =>
          if (iniCurso(cursos(xh)) <= iniCurso(cursos(yh)))
            xh +: merge(xt, ys)
          else
            yh +: merge(xs, yt)
      }

    val ordenados = merge(izq, der)

    // Suma de distancias entre aulas consecutivas
    if (ordenados.length < 2) 0
    else
      ordenados.zip(ordenados.tail).map { case (i, j) =>
        d(a(i))(a(j))
      }.sum
  }
  
 def generarAsignacionesPar(n: Int, m: Int): Vector[Asignacion] = {
    def gen(k: Int): Vector[Asignacion] =
      if (k == 0) Vector(Vector.empty)
      else {
        val sub = gen(k - 1)
        (0 until m).toVector.flatMap(v => sub.map(v +: _))
      }
 
    if (n == 0) Vector(Vector.empty)
    else {
      val tareas = (0 until m).toVector.map { v =>
        task {
          val resto = gen(n - 1)
          resto.map(v +: _)
        }
      }
      tareas.flatMap(_.join())
    }
  }

    // Lanzamos una tarea por cada valor posible del primer curso
    val tareas = (0 until m).toVector.map { v =>
      task {
        val resto = gen(n - 1)          // asignaciones de los n-1 cursos restantes
        resto.map(v +: _)               // anteponemos v
      }
    }

    // Recolectamos y concatenamos en orden
    tareas.flatMap(_.join())
  }

  def asignacionOptimaPar(cursos: Cursos, aulas: Aulas, d: Distancias,
                          w: Pesos): (Asignacion, Int) = {
    val n           = cursos.length
    val m           = aulas.length
    val candidatas  = generarAsignacionesPar(n, m)
    val mid         = candidatas.length / 2

    // Mínimo de costo en un subvector de candidatas
    def minimoEn(sub: Vector[Asignacion]): (Asignacion, Int) =
      sub.map(a => (a, costoAsignacion(cursos, aulas, d, a, w)))
        .minBy(_._2)

    val (minIzq, minDer) = parallel(
      minimoEn(candidatas.slice(0, mid)),
      minimoEn(candidatas.slice(mid, candidatas.length))
    )

    if (minIzq._2 <= minDer._2) minIzq else minDer
  }
}
