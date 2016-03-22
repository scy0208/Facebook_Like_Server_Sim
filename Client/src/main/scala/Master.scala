

import util.Random
import akka.actor.{ Actor, ActorRef, Props, ActorSystem }
import akka.util.Timeout
//inform server finish

class Master(num: Int) extends Actor{

  val behaviorPool = List("Normal", "Crazy_Poster", "Crazy_Getter", "Slacker")

  var registerNum=0
  //var albumNum=0


  val clientPool=new Array[ActorRef](num)

  var index = 0
  def receive = {
    case Initialize => {  //let all client to register itself to server
      println("Intializing")
      for (i <- 0 until num){
        clientPool(i)=context.actorOf(Props(classOf[Client],Random.alphanumeric.take(10).mkString,"1992-02-10",behaviorPool(Random.nextInt(behaviorPool.length))))
        clientPool(i) ! Register
      }
    }

    case FinishRegister =>{
      registerNum+=1;
      if(registerNum==num){
        println("Finish Registering 1")
        clientPool(0)!StartMakingFriends
      }
    }

    case FinishMakingFriends=>{
      println("Finish Registering 2")
      clientPool.foreach(client=>{
        client!MakingAlbums
      })
    }

    case FinishMakingAlbums=>{
      sender!StartSimulation
    }

  }
}