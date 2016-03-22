/**
  * Created by chunyangshen on 11/27/15.
  */
package main.scala

import akka.actor.{ Actor, ActorRef, Props, ActorSystem}
import spray.routing.{Route, HttpService, RequestContext}
import spray.http.MediaTypes._
import spray.httpx.Json4sSupport
import org.json4s.DefaultFormats



class Server extends Actor with Service {
  val memcache=context.system.actorOf(Props[Memcache],"Memcache")
  implicit def actorRefFactory = context
  val rout = {
    get {
      path(Segments) { segments =>
        validate(segments.exists(s=>s.matches("[0-9]*")), "unmatched path"){
          parameterSeq{ params =>
            val op: ActorRef = actorRefFactory.actorOf(Props(classOf[Operator],memcache))
            ctx => op ! GetRequest(segments, params, ctx)
          }
        }~
          validate(segments.exists(_.matches("makingfriends")), "unmatched path"){
            parameterSeq{ params =>
              val op: ActorRef = actorRefFactory.actorOf(Props(classOf[Operator],memcache))
              ctx => op ! MakingFriends(ctx)
              //println("op dieing...")
            }
          }
      }
    }~
      post{
        path(Segments) { segments =>
            validate(segments.exists(s=>{s.matches("[0-9]*")||s.matches("profiles")||s.matches("pages")}), "unmatched path"){
              parameterSeq{ params =>
                val op: ActorRef = actorRefFactory.actorOf(Props(classOf[Operator],memcache))
                ctx => op ! PostRequest(segments, params, ctx)
              }
            }

        }
      }
  }
  def receive:Receive = runRoute(rout)
}

trait Service extends HttpService with Json4sSupport {
  //actorRefFactory.actorOf(Pros(classOf[]))
  val json4sFormats = DefaultFormats

}