package observer

trait Subject[S] {
  private var observers: List[Observer[S]] = Nil
  def addObserver(observer: Observer[S]) = observers = observer :: observers

  def notifyObservers(subject: S) = observers.foreach(_.receiveUpdate(subject))
}
