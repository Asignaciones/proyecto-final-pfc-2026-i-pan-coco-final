package proyecto

import org.scalameter._
import AsignacionAulas._
import AsignacionAulasPar._

object App {

  // ---------------------------------------------------------------------------
  // Utilidades de presentación
  // ---------------------------------------------------------------------------

  def bloqueAHora(b: Int): String = {
    val totalMin = 6 * 60 + b * 30
    val h        = totalMin / 60
    val m        = totalMin % 60
    f"$h%02d:$m%02d"
  }

  def linea(cursos: Cursos, aulas: Aulas, a: Asignacion, i: Int): String =
    s"  ${idCurso(cursos(i))} → ${idAula(aulas(a(i)))}"

  def lineaCurso(c: Curso): String =
    s"  ${idCurso(c)}: ${bloqueAHora(iniCurso(c))}–${bloqueAHora(finCurso(c))}, ${estCurso(c)} estudiantes"

  def lineaAula(a: Aula): String =
    s"  ${idAula(a)}: capacidad ${capAula(a)}"

  def mostrarCursos(cursos: Cursos): Unit =
    cursos.map(lineaCurso).foreach(println)

  def mostrarAulas(aulas: Aulas): Unit =
    aulas.map(lineaAula).foreach(println)

  def mostrarAsignacion(cursos: Cursos, aulas: Aulas, a: Asignacion): Unit =
    cursos.indices.toVector.map(i => linea(cursos, aulas, a, i)).foreach(println)

  def mostrarMetricas(cursos: Cursos, aulas: Aulas, d: Distancias,
                      a: Asignacion, w: Pesos): Unit = {
    val ch = choques(cursos, a)
    val cf = capacidadFallida(cursos, aulas, a)
    val de = desperdicio(cursos, aulas, a)
    val mv = movilidad(cursos, aulas, d, a)
    val ct = costoAsignacion(cursos, aulas, d, a, w)
    println(s"  CH=$ch  CF=$cf  DE=$de  MV=$mv  CT=$ct")
  }

  // ---------------------------------------------------------------------------
  // Ejemplos del enunciado
  // ---------------------------------------------------------------------------

  def ejemploEnunciado(): Unit = {
    println("=" * 60)
    println("EJEMPLO 1 DEL ENUNCIADO")
    println("=" * 60)

    val c1: Cursos     = Vector(("M01", 4, 8, 25), ("M02", 6, 10, 30), ("M03", 12, 16, 20))
    val a1: Aulas      = Vector(("E101", 30), ("E102", 40))
    val d1: Distancias = Vector(Vector(0, 3), Vector(3, 0))
    val w:  Pesos      = (1000, 100, 1, 2)

    println("\nCursos:")
    mostrarCursos(c1)
    println("Aulas:")
    mostrarAulas(a1)

    println("\nAsignación α1 = [0,0,1]:")
    mostrarAsignacion(c1, a1, Vector(0, 0, 1))
    mostrarMetricas(c1, a1, d1, Vector(0, 0, 1), w)

    println("\nAsignación α2 = [0,1,0]:")
    mostrarAsignacion(c1, a1, Vector(0, 1, 0))
    mostrarMetricas(c1, a1, d1, Vector(0, 1, 0), w)

    val (opt1, ct1) = asignacionOptima(c1, a1, d1, w)
    println(s"\nAsignación óptima (secuencial): $opt1  CT=$ct1")
    val (opt1p, ct1p) = asignacionOptimaPar(c1, a1, d1, w)
    println(s"Asignación óptima (paralela):   $opt1p  CT=$ct1p")

    println("\n" + "=" * 60)
    println("EJEMPLO 2 DEL ENUNCIADO")
    println("=" * 60)

    val c2: Cursos     = Vector(("F01", 0, 4, 40), ("F02", 4, 8, 25),
      ("F03", 8, 12, 50), ("F04", 12, 16, 15))
    val a2: Aulas      = Vector(("S201", 45), ("S202", 30))
    val d2: Distancias = Vector(Vector(0, 5), Vector(5, 0))

    println("\nCursos:")
    mostrarCursos(c2)
    println("Aulas:")
    mostrarAulas(a2)

    println("\nAsignación α1 = [0,1,0,1]:")
    mostrarAsignacion(c2, a2, Vector(0, 1, 0, 1))
    mostrarMetricas(c2, a2, d2, Vector(0, 1, 0, 1), w)

    println("\nAsignación α2 = [0,1,1,0]:")
    mostrarAsignacion(c2, a2, Vector(0, 1, 1, 0))
    mostrarMetricas(c2, a2, d2, Vector(0, 1, 1, 0), w)

    val (opt2, ct2) = asignacionOptima(c2, a2, d2, w)
    println(s"\nAsignación óptima (secuencial): $opt2  CT=$ct2")
    val (opt2p, ct2p) = asignacionOptimaPar(c2, a2, d2, w)
    println(s"Asignación óptima (paralela):   $opt2p  CT=$ct2p")
  }

  // ---------------------------------------------------------------------------
  // Benchmarks
  // ---------------------------------------------------------------------------

  val pares: Vector[(Int, Int)] = Vector(
    (4, 3), (5, 3), (6, 3), (6, 4), (7, 4), (7, 5), (8, 4), (8, 5)
  )

  val wBench: Pesos = (1000, 100, 1, 2)

  def medirMs(bloque: => Any): Double =
    measure { bloque }.value

  def aceleracion(seq: Double, par: Double): Double =
    if (seq == 0.0) 0.0 else (seq - par) / seq * 100.0

  def encabezadoTabla(): Unit = {
    println(f"\n${"n"}%8s  ${"m"}%6s  ${"Seq (ms)"}%14s  ${"Par (ms)"}%14s  ${"Aceleración (%)"}%16s")
    println("-" * 65)
  }

  def benchmarkConteos(): Unit = {
    println("\n" + "=" * 65)
    println("BENCHMARK: choques / desperdicio / movilidad  (n=8, m=5)")
    println("=" * 65)

    val n      = 8
    val m      = 5
    val cursos = cursosAlAzar(n)
    val aulas  = aulasAlAzar(m)
    val dist   = distanciasAlAzar(m)
    // Asignación fija: todos los cursos en el aula 0, evita generar 5^8 asignaciones
    val asig: Asignacion = Vector.fill(n)(0)

    println(f"\n${"Función"}%-22s  ${"Seq (ms)"}%14s  ${"Par (ms)"}%14s  ${"Aceleración (%)"}%16s")
    println("-" * 72)

    val tChSeq = medirMs(choques(cursos, asig))
    val tChPar = medirMs(choquesPar(cursos, asig))
    println(f"${"choques"}%-22s  $tChSeq%14.2f  $tChPar%14.2f  ${aceleracion(tChSeq, tChPar)}%+16.2f")

    val tDeSeq = medirMs(desperdicio(cursos, aulas, asig))
    val tDePar = medirMs(desperdicioPar(cursos, aulas, asig))
    println(f"${"desperdicio"}%-22s  $tDeSeq%14.2f  $tDePar%14.2f  ${aceleracion(tDeSeq, tDePar)}%+16.2f")

    val tMvSeq = medirMs(movilidad(cursos, aulas, dist, asig))
    val tMvPar = medirMs(movilidadPar(cursos, aulas, dist, asig))
    println(f"${"movilidad"}%-22s  $tMvSeq%14.2f  $tMvPar%14.2f  ${aceleracion(tMvSeq, tMvPar)}%+16.2f")
  }

  def benchmarkGeneracion(): Unit = {
    println("\n" + "=" * 65)
    println("BENCHMARK: generarAsignaciones vs generarAsignacionesPar")
    println("=" * 65)
    encabezadoTabla()

    pares.map { case (n, m) =>
      val tSeq = medirMs(generarAsignaciones(n, m))
      val tPar = medirMs(generarAsignacionesPar(n, m))
      val acel = aceleracion(tSeq, tPar)
      f"$n%8d  $m%6d  $tSeq%14.2f  $tPar%14.2f  $acel%+16.2f"
    }.foreach(println)
  }

  def benchmarkOptima(): Unit = {
    println("\n" + "=" * 65)
    println("BENCHMARK: asignacionOptima vs asignacionOptimaPar")
    println("=" * 65)
    encabezadoTabla()

    pares.map { case (n, m) =>
      val cursos = cursosAlAzar(n)
      val aulas  = aulasAlAzar(m)
      val dist   = distanciasAlAzar(m)
      val tSeq   = medirMs(asignacionOptima(cursos, aulas, dist, wBench))
      val tPar   = medirMs(asignacionOptimaPar(cursos, aulas, dist, wBench))
      val acel   = aceleracion(tSeq, tPar)
      f"$n%8d  $m%6d  $tSeq%14.2f  $tPar%14.2f  $acel%+16.2f"
    }.foreach(println)
  }

  // ---------------------------------------------------------------------------
  // Punto de entrada
  // ---------------------------------------------------------------------------

  def main(args: Array[String]): Unit = {
    ejemploEnunciado()
    benchmarkConteos()
    benchmarkGeneracion()
    benchmarkOptima()
    println("\nFin.")
  }
}