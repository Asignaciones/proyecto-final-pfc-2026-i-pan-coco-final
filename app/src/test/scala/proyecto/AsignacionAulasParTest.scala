package proyecto

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import AsignacionAulas._
import AsignacionAulasPar._

@RunWith(classOf[JUnitRunner])
class AsignacionAulasParTest extends AnyFunSuite {

  val c1: Cursos     = Vector(("M01", 4, 8, 25), ("M02", 6, 10, 30), ("M03", 12, 16, 20))
  val a1: Aulas      = Vector(("E101", 30), ("E102", 40))
  val d1: Distancias = Vector(Vector(0, 3), Vector(3, 0))
  val w: Pesos       = (1000, 100, 1, 2)

  // ── Tests originales ────────────────────────────────────────────────────────

  test("choquesPar: asignacion [0,0,1] tiene 1 choque") {
    assert(choquesPar(c1, Vector(0, 0, 1)) == 1)
  }

  test("choquesPar: asignacion [0,1,0] no tiene choques") {
    assert(choquesPar(c1, Vector(0, 1, 0)) == 0)
  }

  test("desperdicioPar: asignacion [0,0,1] tiene desperdicio 25") {
    assert(desperdicioPar(c1, a1, Vector(0, 0, 1)) == 25)
  }

  test("movilidadPar: asignacion [0,0,1] tiene movilidad 3") {
    assert(movilidadPar(c1, a1, d1, Vector(0, 0, 1)) == 3)
  }

  test("generarAsignacionesPar: 2 cursos y 2 aulas produce 4 asignaciones") {
    assert(generarAsignacionesPar(2, 2).length == 4)
  }

  test("asignacionOptimaPar: el costo de la optima no supera el de [0,1,0] (37)") {
    val (_, costo) = asignacionOptimaPar(c1, a1, d1, w)
    assert(costo <= 37)
  }

  // ── Fixtures adicionales ────────────────────────────────────────────────────

  // Cuatro cursos todos solapados entre sí (mismo bloque horario)
  val cTodosSolapados: Cursos = Vector(
    ("X1", 0, 4, 10), ("X2", 1, 5, 10), ("X3", 2, 6, 10), ("X4", 3, 7, 10)
  )

  // Cuatro cursos sin ningún solapamiento (horarios consecutivos)
  val cSinSolapamiento: Cursos = Vector(
    ("Y1", 0, 2, 10), ("Y2", 2, 4, 10), ("Y3", 4, 6, 10), ("Y4", 6, 8, 10)
  )

  val a2: Aulas      = Vector(("S1", 20), ("S2", 20))
  val d2: Distancias = Vector(Vector(0, 5), Vector(5, 0))

  // ── 5 tests adicionales: choquesPar ────────────────────────────────────────

  test("choquesPar: cursos sin solapamiento en misma aula producen 0 choques") {
    assert(choquesPar(cSinSolapamiento, Vector(0, 0, 0, 0)) == 0)
  }

  test("choquesPar: 4 cursos solapados en misma aula producen 6 choques") {
    assert(choquesPar(cTodosSolapados, Vector(0, 0, 0, 0)) == 6)
  }

  test("choquesPar: cursos solapados en aulas distintas producen 0 choques") {
    val cSol: Cursos = Vector(
      ("A", 0, 6, 10), ("B", 1, 7, 10), ("C", 2, 8, 10), ("D", 3, 9, 10)
    )
    assert(choquesPar(cSol, Vector(0, 1, 2, 3)) == 0)
  }

  test("choquesPar: un unico curso siempre produce 0 choques") {
    val cUno: Cursos = Vector(("Z1", 0, 4, 15))
    assert(choquesPar(cUno, Vector(0)) == 0)
  }

  test("choquesPar: resultado coincide con choques secuencial en asignacion [0,0,1]") {
    val asig = Vector(0, 0, 1)
    assert(choquesPar(c1, asig) == choques(c1, asig))
  }

  // ── 5 tests adicionales: desperdicioPar ────────────────────────────────────

  test("desperdicioPar: aula con capacidad exacta produce desperdicio 0") {
    val cExacto: Cursos = Vector(("E1", 0, 4, 20))
    val aExacto: Aulas  = Vector(("R1", 20))
    assert(desperdicioPar(cExacto, aExacto, Vector(0)) == 0)
  }

  test("desperdicioPar: capacidad insuficiente contribuye 0 al desperdicio") {
    val cGrande: Cursos = Vector(("G1", 0, 4, 50))
    val aPequena: Aulas = Vector(("P1", 20))
    assert(desperdicioPar(cGrande, aPequena, Vector(0)) == 0)
  }

  test("desperdicioPar: asignacion [0,0,0] sobre c1/a1 produce desperdicio 15") {
    assert(desperdicioPar(c1, a1, Vector(0, 0, 0)) == 15)
  }

  test("desperdicioPar: asignacion [1,1,1] sobre c1/a1 produce desperdicio 45") {
    assert(desperdicioPar(c1, a1, Vector(1, 1, 1)) == 45)
  }

  test("desperdicioPar: resultado coincide con desperdicio secuencial en asignacion [0,1,0]") {
    val asig = Vector(0, 1, 0)
    assert(desperdicioPar(c1, a1, asig) == desperdicio(c1, a1, asig))
  }

  // ── 5 tests adicionales: movilidadPar ──────────────────────────────────────

  test("movilidadPar: un unico curso asignado produce movilidad 0") {
    val cUno: Cursos     = Vector(("Z1", 0, 4, 15))
    val aUno: Aulas      = Vector(("R1", 20))
    val dUno: Distancias = Vector(Vector(0))
    assert(movilidadPar(cUno, aUno, dUno, Vector(0)) == 0)
  }

  test("movilidadPar: cursos en la misma aula tienen movilidad 0") {
    assert(movilidadPar(c1, a1, d1, Vector(0, 0, 0)) == 0)
  }

  test("movilidadPar: asignacion [1,0,1] sobre c1 produce movilidad 6") {
    assert(movilidadPar(c1, a1, d1, Vector(1, 0, 1)) == 6)
  }

  test("movilidadPar: asignacion alternada [0,1,0,1] con d2 produce movilidad 15") {
    assert(movilidadPar(cSinSolapamiento, a2, d2, Vector(0, 1, 0, 1)) == 15)
  }

  test("movilidadPar: resultado coincide con movilidad secuencial en asignacion [0,1,0]") {
    val asig = Vector(0, 1, 0)
    assert(movilidadPar(c1, a1, d1, asig) == movilidad(c1, a1, d1, asig))
  }
}
