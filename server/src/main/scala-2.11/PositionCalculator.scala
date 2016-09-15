import domain.Position

/**
  * Created by kuba on 17.09.16.
  */
object PositionCalculator {
  def findClosestAvailable(center: Position, takenPositions:Seq[Position]) = {
    val positionsAroundPoint = for {
      range <- Stream.from(0)
      x <- Seq(range,range+1,range-1)
      y <- Seq(range,range+1,range-1)
    } yield center + Position(x,y)
    positionsAroundPoint.find(!takenPositions.contains(_)).get
  }

}
