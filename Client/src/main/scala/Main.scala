import util.Random
import akka.actor.{ Actor, ActorRef, Props, ActorSystem }
import akka.util.Timeout

object Main {
  def main(args: Array[String]) {
    val system = ActorSystem("ClientSimulator")

    val num = 100
    val simulator = system.actorOf(Props(classOf[Master], num), "Master")
    simulator ! Initialize
  }

}