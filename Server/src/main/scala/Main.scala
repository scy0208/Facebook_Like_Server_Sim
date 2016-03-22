package main.scala

/**
  * Created by chunyangshen on 11/16/15.
  */


import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.util.Timeout
import spray.can.Http

import scala.concurrent.duration._




object Main extends App{


  implicit val timeout = Timeout(5 seconds)
  implicit val system = ActorSystem("FB")
  val service = system.actorOf(Props[Server], "FB-Server")
  IO(Http) ! Http.Bind(service, interface = "127.0.0.1", port = 8080)


}



