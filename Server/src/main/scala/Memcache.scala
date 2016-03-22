/**
  * Created by chunyangshen on 11/27/15.
  */
package main.scala


import akka.actor.{ Actor, ActorRef, Props, ActorSystem}
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import util.Random

class Memcache extends Actor{
  println("Memory Initialize ...")
  var memory = new HashMap[String, Node]()

  var id  = -1

  def addEdge(id1:String, edgeName:String, id2:String): Unit ={
    val futureNode=memory.get(id1)
    futureNode match {
      case Some(node)=>node.getEdges.addEdge(edgeName,id2)
      case _=>
    }
  }

  def receive = {
    case AddEdge(nodeId: String, edgeName: String, id: String) => {
      addEdge(nodeId,edgeName,id)

    }
    case Get(nodeId: String) => {
      val node= memory.get(nodeId)
      node match{
        case Some(s)=>sender!s
        case _ =>
      }
    }
    case AddNode(fields: Fields, edges: Edges) => {
      id += 1
      fields.addId(id.toString)
      memory += (id.toString() -> new Node(fields, edges))
      //println("Add one new node, Show current memory unit")
      memory.foreach({ case (s, n) => println(s.toString + "=" + n.toString) })
      sender ! id
    }

    case Update(nodeId: String, para: Seq[(String,String)]) => {
      val futureNode = memory.get(nodeId)

      futureNode match{
        case Some(node)=>{
          val array = node.getFields.getMembers()
          val set = new HashSet[String]()
          array.foreach ({ case kv => set.add(kv.getKey()) })
          for (i<- 0 to para.size-1){
            if (set.contains(para(i)._1))
              node.getFields.update(para(i)._1, para(i)._2)
          }
        }
        case _ =>
      }

    }

    case AutoFriendList => {
      val userAmount = id
      println("ENter CreateFriends .. user Amoutn : " + userAmount )
      for (i <- 1 to id){
        for (j <- 1 to 10){
          val fid = Random.nextInt(userAmount)
          addEdge(id.toString,"friends",fid.toString)
          addEdge(fid.toString,"friends",id.toString)
        }
      }
      sender ! "Finish"
    }

  }
}