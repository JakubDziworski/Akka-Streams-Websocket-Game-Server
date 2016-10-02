import com.jakubdziworski.actor.Position
import com.jakubdziworski.service.PositionCalculator
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{Matchers, PropSpec}

class PositionCalculatorSpec extends PropSpec with Matchers with TableDrivenPropertyChecks {

  val fractions =
    Table(
      ("center (desired position)", "taken positions", "expected position selected"),
      ((0, 0), Nil, (0, 0)),
      ((5, 5), Nil, (5, 5)),
      ((0, 0), Seq((1, 0)), (0, 0)),
      ((0, 0), Seq((0, 0)), (0, 1)),
      ((0, 0), Seq((0, 0)), (0, 1)),
      ((0, 0), Seq((0, 0), (0, 1)), (0, -1)),
      ((0, 0), Seq((0, 0), (0, -1), (0, 1)), (1, 0)),
      ((1, 1), Seq((0, 0), (0, -1), (0, 1)), (1, 1)),
      ((0, 1), Seq((0, 0), (0, -1), (0, 1)), (0, 2))
    )

  property("should get closest position available") {
    forAll(fractions) { case (center, takenPositions, expectedPosition) => {
      val actualPosition = PositionCalculator.findClosestAvailable(center, takenPositions)
      actualPosition should ===(expectedPosition: Position)
    }
    }
  }

  implicit def tupleToPosition(tupledPosition: (Int, Int)): Position = {
    Position(tupledPosition._1, tupledPosition._2)
  }

  implicit def tupleListToPositionList(tupledList: Seq[(Int, Int)]): Seq[Position] = {
    tupledList.map { case (x, y) => Position(x, y) }
  }

}
