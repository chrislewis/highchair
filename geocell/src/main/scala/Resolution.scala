package highchair.geocell

sealed abstract class Resolution(
  val res: Int,
  val next: Option[Resolution]
) extends math.Ordered[Resolution] {
  
  /** Create a List of Resolutions (similar Int#to). */
  def to(that: Resolution) = {
    def _to(start: Resolution, end: Resolution, l: List[Resolution]): List[Resolution] = (start, l) match {
      case _ if start == end => l
      case (Resolution(_, None), _) => l
      case (Resolution(_, Some(next)), _) => _to(next, end, next :: l)
    }
    
    if(this > that)
      Nil
    else
      _to(this, that, this :: Nil) reverse
  }
  
  def compare(that: Resolution) =
    if(this.res < that.res) -1
    else if(this.res == that.res) 0
    else 1
}

/* Resolution instances form a sequential singly-linked structure. */
case object One extends Resolution(1, Some(Two))
case object Two extends Resolution(2, Some(Three))
case object Three extends Resolution(3, Some(Four))
case object Four extends Resolution(4, Some(Five))
case object Five extends Resolution(5, Some(Six))
case object Six extends Resolution(6, Some(Seven))
case object Seven extends Resolution(7, Some(Eight))
case object Eight extends Resolution(8, Some(Nine))
case object Nine extends Resolution(9, Some(Ten))
case object Ten extends Resolution(10, Some(Eleven))
case object Eleven extends Resolution(11, Some(Twelve))
case object Twelve extends Resolution(12, Some(Thirteen))
case object Thirteen extends Resolution(13, None)

object Resolution {
  
  val Min = One
  val Max = Thirteen
  
  def unapply(r: Resolution) = Some(r.res, r.next)
}
