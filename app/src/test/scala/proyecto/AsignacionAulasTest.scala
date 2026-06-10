package proyecto

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import AsignacionAulas._

@RunWith(classOf[JUnitRunner])
class AsignacionAulasTest extends AnyFunSuite {

  // Ejemplo 1 del enunciado
  val c1: Cursos    = Vector(("M01", 4, 8, 25), ("M02", 6, 10, 30), ("M03", 12, 16, 20))
  val a1: Aulas     = Vector(("E101", 30), ("E102", 40))
  val d1: Distancias = Vector(Vector(0, 3), Vector(3, 0))
  val w: Pesos      = (1000, 100, 1, 2)

  // solapan
  test("solapan: M01[4,8) y M02[6,10) se solapan") {
    assert(solapan(("M01", 4, 8, 25), ("M02", 6, 10, 30)))
  }

  test("solapan: M01[4,8) y M03[12,16) no se solapan") {
    assert(!solapan(("M01", 4, 8, 25), ("M03", 12, 16, 20)))
  }

  test("solapan: cursos adyacentes [0,4) y [4,8) no se solapan") {
    assert(!solapan(("A", 0, 4, 10), ("B", 4, 8, 10)))
  }
  test("solapan: cursos identicos [4,8) y [4,8) se solapan") {
    assert(solapan(("X", 4, 8, 10), ("Y", 4, 8, 10)))
  }

  test("solapan: un curso contenido dentro de otro [2,10) y [4,6) se solapan") {
    assert(solapan(("X", 2, 10, 10), ("Y", 4, 6, 10)))
  }

  // choques
  test("choques: asignacion [0,0,1] tiene 1 choque (M01 y M02 en E101)") {
    assert(choques(c1, Vector(0, 0, 1)) == 1)
  }

  test("choques: asignacion [0,1,0] no tiene choques") {
    assert(choques(c1, Vector(0, 1, 0)) == 0)
  }
  test("choques: tres cursos todos en la misma aula con solapamientos produce 2 choques") {
    // M01[4,8), M02[6,10) y M03[7,12) en aula 0 → pares (M01,M02), (M01,M03), (M02,M03) = 3 choques
    val cursos = Vector(("M01", 4, 8, 25), ("M02", 6, 10, 30), ("M03", 7, 12, 20))
    assert(choques(cursos, Vector(0, 0, 0)) == 3)
  }


  // capacidadFallida
  test("capacidadFallida: asignacion [0,0,1] no falla capacidad") {
    assert(capacidadFallida(c1, a1, Vector(0, 0, 1)) == 0)
  }
  test("capacidadFallida: curso con mas estudiantes que capacidad del aula cuenta como fallo") {
    // F03 tiene 50 estudiantes, S201 tiene capacidad 45 → 1 fallo
    val cursos2 = Vector(("F01", 0, 4, 40), ("F02", 4, 8, 25), ("F03", 8, 12, 50), ("F04", 12, 16, 15))
    val aulas2  = Vector(("S201", 45), ("S202", 30))
    assert(capacidadFallida(cursos2, aulas2, Vector(0, 1, 0, 1)) == 1)
  }

  // desperdicio
  test("desperdicio: asignacion [0,0,1] tiene desperdicio 25") {
    // E101(30)-M01(25)=5, E101(30)-M02(30)=0, E102(40)-M03(20)=20 → 25
    assert(desperdicio(c1, a1, Vector(0, 0, 1)) == 25)
  }

  test("desperdicio: asignacion [0,1,0] tiene desperdicio 25") {
    // E101(30)-M01(25)=5, E102(40)-M02(30)=10, E101(30)-M03(20)=10 → 25
    assert(desperdicio(c1, a1, Vector(0, 1, 0)) == 25)
  }


  // movilidad
  test("movilidad: asignacion [0,0,1] ejemplo 1 da MV=3") {
    assert(movilidad(c1, a1, d1, Vector(0, 0, 1)) == 3)
  }

  test("movilidad: asignacion [0,1,0] ejemplo 1 da MV=6") {
    assert(movilidad(c1, a1, d1, Vector(0, 1, 0)) == 6)
  }

  test("movilidad: asignacion [0,1,0,1] ejemplo 2 da MV=15") {
    val cursos2 = Vector(("F01", 0, 4, 40), ("F02", 4, 8, 25), ("F03", 8, 12, 50), ("F04", 12, 16, 15))
    val aulas2  = Vector(("S201", 45), ("S202", 30))
    val dist2   = Vector(Vector(0, 5), Vector(5, 0))
    assert(movilidad(cursos2, aulas2, dist2, Vector(0, 1, 0, 1)) == 15)
  }

  test("movilidad: todos en la misma aula da MV=0") {
    assert(movilidad(c1, a1, d1, Vector(0, 0, 0)) == 0)
  }

  test("movilidad: un solo curso da MV=0") {
    val unCurso = Vector(("C0", 4, 8, 20))
    val unAula  = Vector(("E0", 30))
    val dist    = Vector(Vector(0))
    assert(movilidad(unCurso, unAula, dist, Vector(0)) == 0)
  }


  // costoAsignacion
  test("costoAsignacion: asignacion [0,0,1] cuesta 1031") {
    assert(costoAsignacion(c1, a1, d1, Vector(0, 0, 1), w) == 1031)
  }

  test("costoAsignacion: asignacion [0,1,0] cuesta 37") {
    assert(costoAsignacion(c1, a1, d1, Vector(0, 1, 0), w) == 37)
  }

  test("costoAsignacion: asignacion [0,1,0,1] ejemplo 2 cuesta 155") {
    val cursos2 = Vector(("F01", 0, 4, 40), ("F02", 4, 8, 25), ("F03", 8, 12, 50), ("F04", 12, 16, 15))
    val aulas2  = Vector(("S201", 45), ("S202", 30))
    val dist2   = Vector(Vector(0, 5), Vector(5, 0))
    assert(costoAsignacion(cursos2, aulas2, dist2, Vector(0, 1, 0, 1), w) == 155)
  }

  test("costoAsignacion: asignacion [0,1,1,0] ejemplo 2 cuesta 160") {
    val cursos2 = Vector(("F01", 0, 4, 40), ("F02", 4, 8, 25), ("F03", 8, 12, 50), ("F04", 12, 16, 15))
    val aulas2  = Vector(("S201", 45), ("S202", 30))
    val dist2   = Vector(Vector(0, 5), Vector(5, 0))
    assert(costoAsignacion(cursos2, aulas2, dist2, Vector(0, 1, 1, 0), w) == 160)
  }

  test("costoAsignacion: costo con choque es mayor que sin choque") {
    val conChoque = costoAsignacion(c1, a1, d1, Vector(0, 0, 1), w)
    val sinChoque = costoAsignacion(c1, a1, d1, Vector(0, 1, 0), w)
    assert(conChoque > sinChoque)
  }


  // generarAsignaciones
  test("generarAsignaciones: 2 cursos y 2 aulas produce 4 asignaciones") {
    assert(generarAsignaciones(2, 2).length == 4)
  }

  test("generarAsignaciones: 3 cursos y 3 aulas produce 27 asignaciones") {
    assert(generarAsignaciones(3, 3).length == 27)
  }

  test("generarAsignaciones: 1 curso y 3 aulas produce 3 asignaciones") {
    assert(generarAsignaciones(1, 3).length == 3)
  }

  test("generarAsignaciones: 0 cursos produce exactamente 1 asignacion vacia") {
    val result = generarAsignaciones(0, 3)
    assert(result.length == 1 && result.head.isEmpty)
  }

  test("generarAsignaciones: todos los valores estan en rango 0 hasta m-1") {
    val asigs = generarAsignaciones(3, 2)
    assert(asigs.forall(a => a.forall(v => v >= 0 && v <= 1)))
  }


  // asignacionOptima
  test("asignacionOptima: el costo de la optima no supera el de [0,1,0] (37)") {
    val (_, costo) = asignacionOptima(c1, a1, d1, w)
    assert(costo <= 37)
  }

  test("asignacionOptima: la asignacion optima tiene longitud igual al numero de cursos") {
    val (asig, _) = asignacionOptima(c1, a1, d1, w)
    assert(asig.length == c1.length)
  }

  test("asignacionOptima: todos los indices de la asignacion optima son validos") {
    val (asig, _) = asignacionOptima(c1, a1, d1, w)
    assert(asig.forall(j => j >= 0 && j < a1.length))
  }

  test("asignacionOptima: ejemplo 2 costo optimo no supera 155") {
    val cursos2 = Vector(("F01", 0, 4, 40), ("F02", 4, 8, 25), ("F03", 8, 12, 50), ("F04", 12, 16, 15))
    val aulas2  = Vector(("S201", 45), ("S202", 30))
    val dist2   = Vector(Vector(0, 5), Vector(5, 0))
    val (_, costo) = asignacionOptima(cursos2, aulas2, dist2, w)
    assert(costo <= 155)
  }

  test("asignacionOptima: el costo calculado coincide con costoAsignacion aplicado a la optima") {
    val (asig, costo) = asignacionOptima(c1, a1, d1, w)
    assert(costoAsignacion(c1, a1, d1, asig, w) == costo)
  }
}