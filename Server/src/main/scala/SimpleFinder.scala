package main.scala



/**
  * Created by chunyangshen on 11/24/15.
  */


import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._




class SimpleFinder(memcache: ActorRef) extends Actor{
  implicit val timeout = Timeout(5 seconds)
  override def receive: Actor.Receive = {
    case ToFind(path: Seq[String], para: Seq[(String,String)])=>{
      val respond=sender
      //println("I need to find %s and %s".format(path.toString(),para.toString()))
      var nodeId=""
      if(((!para.isEmpty)&&(!para(0)._2.equals(""))&&(para(0)._2.matches("[0-9]*")))||(path.last.matches("[0-9]*"))){

        if((!para.isEmpty)&&(!para(0)._2.equals(""))&&(para(0)._2.matches("[0-9]*"))) {
          nodeId=(para(0)._2)
        }else {
          nodeId=path.last
        }
        //println("to find %s".format(nodeId))
        val futureNode:Future[Node]=(memcache?Get(nodeId)).mapTo[Node]
        futureNode.onSuccess {
          case node:Node=> {
              val fields = node.getFields
              val edges = node.getEdges
              if (para.isEmpty) {
                respond ! new Object(fields.getMembers().asInstanceOf[Array[Member]])
                this.context.stop(self)
              } else {
                val members: Array[KeyValue] = para.toArray.map(p => {
                  val index = fields.find(p._1)
                  if (index != (-1))
                    fields.getMember(index)
                  else
                    new KeyValue(p._1, "Not Found")
                })
                //println("I GET RESULT")
                respond ! new Object(members.asInstanceOf[Array[Member]])
                this.context.stop(self)
              }

          }
        }
        futureNode.onFailure{
          case exc => {respond ! new Object(Array(new KeyValue(nodeId,"NOT FOUND")))}
            this.context.stop(self)
        }

      }else{
        var index=path.size-1
        while(!path(index).matches("[0-9]*"))
          index-=1
        nodeId=path(index)
        val futureNode:Future[Node]=(memcache?Get(nodeId)).mapTo[Node]
        futureNode.onSuccess {
          case node:Node=> {
              val fields = node.getFields
              val edges = node.getEdges
              val edge = path(index + 1)

              val listOfFuture: List[Future[Object]] = edges.getKeySet(edges.find(path(index + 1))).getValue().toList.map(id => {
                var newPath = Seq(id) ++ path.drop(index + 2)
                val subActor = context.system.actorOf(Props(classOf[SimpleFinder], memcache))
                (subActor ? ToFind(newPath, para)).mapTo[Object]
              })
              val futures: Future[List[Object]] = Future.sequence(listOfFuture).mapTo[List[Object]]
              futures.onSuccess {
                case result: List[Object] => respond ! new Object(Array(new KeyArray(path(index + 1), result.toArray).asInstanceOf[Member]))
                  this.context.stop(self)
              }
              futures.onFailure {
                case exc => respond ! new Object(Array(new KeyValue("Failure", "..").asInstanceOf[Member]))
                  this.context.stop(self)
              }


          }
        }
        futureNode.onFailure{
          case exc => {respond ! new Object(Array(new KeyValue(nodeId,"NOT FOUND")))}
            this.context.stop(self)

        }

      }

    }

  }

}

