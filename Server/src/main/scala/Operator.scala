

package main.scala


import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import spray.http.{HttpResponse, HttpEntity}
import spray.routing.RequestContext

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class Operator(memcache:ActorRef) extends Actor{
  implicit val timeout = Timeout(5 seconds)

  def receive = {
    case GetRequest(path, para, ctx) =>{
      val finder: ActorRef = context.actorOf(Props(classOf[SimpleFinder], memcache))
      val future = (finder ? ToFind(path, para)).mapTo[Object]
      future.onSuccess{
        case result => {
          ctx.complete(result.toString()+"\n")
          this.context.stop(self)
        }
      }
      future.onFailure{
        case error => {
          ctx.complete("404 Not Found\n")
          this.context.stop(self)
        }
      }

    }

    case PostRequest(path, para, ctx) =>{
      if (path.last.matches("[0-9]*")){
        memcache ! Update(path.last, para)
        ctx.complete("Update Finished \n")
        this.context.stop(self)
      }
      else{
        var edges: Edges = new Edges(Array())
        var t = ""
        if(path.last.equals("albums")) {
          edges = new Edges(Array("photos"))
        }else if(path.last.equals("groups")){
          edges = new Edges(Array("events","members"))
        }else if(path.last.equals("profiles")){
          edges = new Edges(Array("friends","posts","albums"))
        }else if(path.last.equals("pages")){
          edges = new Edges(Array("followers","posts","albums"))
        }
        val members:Array[KeyValue]=para.toArray.map(p=>{new KeyValue(p._1,p._2)})++Array(new KeyValue("id", ""))
        val fields: Fields = new Fields(members)
        val future = (memcache ? AddNode(fields, edges)).mapTo[Int]
        future.onSuccess{
          case id:Int=>{
            if(path.last.equals("pages")||path.last.equals("profiles")){
              //val result = Await.result(future, timeout.duration).asInstanceOf[Int]

              val entity = HttpEntity("Created " + id)
              val res = HttpResponse(201, entity = entity)
              ctx.complete(res)
              //ctx.complete("Created New %s , id %s \n".format(path.last,id))
            }
            if((path.size>1)&&(path(path.size-2).matches("[0-9]*"))){

              memcache!AddEdge(path(path.size-2), path.last, id.toString())

              val entity = HttpEntity("Created " + id)
              val res = HttpResponse(201, entity = entity)
              ctx.complete(res)
              //ctx.complete("Created New %s , id %s \n".format(path.last,id))
            }else{
              ctx.complete("unmatched path")
            }
            this.context.stop(self)
          }
        }
        future.onFailure{
          case error => {
            ctx.complete("Create New Node Failure")
            this.context.stop(self)
          }
        }
      }

    }

    case MakingFriends(ctx: RequestContext) =>{
      val future= (memcache ? AutoFriendList).mapTo[String]
      future.onSuccess{
        case s:String=>{
          ctx.complete("FriendList Done")
        }
      }

    }
  }
}