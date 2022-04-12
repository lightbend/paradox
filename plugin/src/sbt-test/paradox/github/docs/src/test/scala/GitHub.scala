//#multiple
object GitHub {
  // #single
  type IntPair = (Int, Int)
  // #single

  // #multiple
  val embedded = "should be ignored"
  // #multiple

  // #nested
  sealed trait DateInterval
  case class Year(value: Int) extends DateInterval
  case class Quarter(year: Year, value: Int) extends DateInterval
  case class Month(year: Year, value: Int) extends DateInterval
  case class Week(year: Year, value: Int) extends DateInterval
  case class Day(year: Year, month: Month, value: Int) extends DateInterval
  // #nested
}
//#multiple
