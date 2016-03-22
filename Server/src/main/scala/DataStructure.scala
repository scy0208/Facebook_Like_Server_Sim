package main.scala

import akka.actor.ActorRef
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer,HashMap,HashSet}



/**
  * Created by chunyangshen on 11/19/15.
  */





abstract class Member

class KeyValue(key:String,var value:String) extends Member{
  def getKey(): String ={
    return key
  }
  def getValue():String={
    return value
  }
  def setValue(value:String): Unit ={
    this.value=value
  }
  override def toString:String={
    return key+" : "+value
  }


}

class Object(members: Array[Member]){
  override def toString:String={
      var result="{"
      members.foreach(member=>{result+=(" "+member.toString+",")})
      result+=" }"
      return result
  }
}


class KeyArray(key:String, value:Array[Object]) extends Member{
  override def toString:String={
    var result=key+" : ["
    value.foreach(attribute=>{result+=(" "+attribute.toString+",")})
    result+="]"
    return result
  }
}


class KeyHash(key: String, value:mutable.HashMap[String, ActorRef]) extends Member{
  def getKey(): String ={
    return key
  }
  def findHash(id: String): Boolean ={
    return value.contains(id)
  }
  def addHash(id:String, link:ActorRef): Unit ={
    value+=(id->link)
  }
  def getValue():HashMap[String, ActorRef]={
    return value
  }
}

class KeySet(key:String, value:mutable.HashSet[String])extends Member{
  override def toString():String={
    var result=key+" : ["
    value.foreach(attribute=>{result+=(" "+attribute.toString+",")})
    result+="]"
    return result
  }
  def getKey(): String ={
    return key
  }
  def getValue():mutable.HashSet[String]={
    return value
  }
  def add(id:String):Unit={
    value+=id
  }
  def find(id:String):Boolean={
    return value.contains(id)

  }
}

class Fields(members:Array[KeyValue]){
  def find(k:String):Int={
    (0 until members.size).foreach(i=> {if(members(i).getKey().equals(k)) return i})
    return -1
  }
  def getMembers():Array[KeyValue]={
    return members
  }
  def getMember(i:Int):KeyValue={
    return members(i)
  }
  def setMember(key:String,value:String)={
    (0 until members.size).foreach(i=>{
      if(members(i).getKey().equals(key)){
        members(i).setValue(value)
      }
    })
  }
  def addId(id:String):Unit={
    (0 until members.size).foreach(i=> {if(members(i).getKey().equals("id")){members(i).setValue(id)}})
  }

  def update(key: String, value: String) = {
    for (i <- 0 until members.size){
      if (members(i).getKey().equals(key)){
        members(i).setValue(value)
      }
    }
  }

  override def toString():String={
    var result:String="Fields: \n"
    members.foreach(member=>result+=(" "+member.toString+"\n"))
    return result
  }
}

class Edges(edgeNames:Array[String]){
  var members=edgeNames.map(edgeName=>new KeySet(edgeName,new HashSet[String]()))
  def find(k:String):Int={
    (0 until members.size).foreach(i=> {if(members(i).getKey().equals(k)) return i})
    return -1
  }
  def findEdge(k:String,id:String):Boolean={
    val index=find(k)
    if(index!=(-1)){
      return members(index).find(id)
    }
    return false
  }
  def getKeySet(i:Int):KeySet={
    return members(i)
  }
  def addEdge(k:String,id:String): Unit ={
    (0 until members.size).foreach(i=> {
      if(members(i).getKey()==k){
        members(i).add(id)
      }
    })
  }
  def getMembers():Array[KeySet]={
    return members
  }
  override def toString():String={
    var result:String="Edges: \n"
    members.foreach(member=>result+=(" "+member.toString+"\n"))
    return result
  }


}

class Node(fields:Fields, edges:Edges){
  def getFields:Fields={
    return fields
  }
  def getEdges:Edges={
    return edges
  }
  override def toString: String ={
    var result="{ \n"
    result+=fields.toString()
    result+=edges.toString()
    result+="} \n"
    return result
  }
}
