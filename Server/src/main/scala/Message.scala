/**
  * Created by chunyangshen on 11/27/15.
  */
package main.scala

import akka.actor.{ Actor, ActorRef, Props, ActorSystem}
import spray.routing.{Route, HttpService, RequestContext}


//case class MyGetRequest(path: List[String], para: Seq[(String,String)], ctx: RequestContext)
case class GetRequest(path: List[String], para: Seq[(String,String)], ctx: RequestContext)

case class PostRequest(path: List[String], para: Seq[(String,String)], ctx: RequestContext)
case class CreateUser(para: Seq[(String,String)], ctx: RequestContext)




case class AddFriend(friendId:String, friendLink:ActorRef)

case class AddEdge(nodeId:String, edgeName:String, id:String)
case class Get(nodeId:String)
case class AddNode(fielfs:Fields,edges:Edges)

case class ToFind(path: Seq[String], para: Seq[(String,String)])


case class Update(nodeId: String, para: Seq[(String,String)])

case class MakingFriends(ctx: RequestContext)
case object AutoFriendList