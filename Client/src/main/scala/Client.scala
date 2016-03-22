
import akka.actor.{ Actor, ActorRef, Props, ActorSystem}
import akka.util.Timeout
import spray.client.pipelining.sendReceive
import spray.httpx.RequestBuilding
import spray.http.HttpResponse
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.collection.mutable.ListBuffer
import util.Random




class Client(name:String, dob: String, behavior: String) extends Actor with RequestBuilding {
  implicit val timeout = Timeout(60 seconds)
  implicit val executionContext = context.dispatcher

  val clientPipeline = sendReceive
  var id = -1
  var album = -1

  def receive = {

    case Register => {
      val respond:ActorRef=sender
      val url = "http://127.0.0.1:8080/profiles?name=" + name + "&bod="+ dob
      println("URL : " + url)
      val fResponse: Future[HttpResponse] = clientPipeline {
        Post(url)
      }
      //in response package, it should encapsulate user id late(access token)
      fResponse onComplete {
        case Success(response) => {
          println(response.status.value)
          val s = response.entity.asString.split(" ")
          id = s(s.size-1).trim().toInt
          respond!FinishRegister
        }
        case Failure(error) => {
          self!Register
        }
      }

    }

    case StartMakingFriends => {
      //println("Enter Finishi ALL")
      val respond:ActorRef=sender
      val fResponse: Future[HttpResponse] = clientPipeline {
        Get("http://127.0.0.1:8080/makingfriends")
      }
      fResponse onComplete {
        case Success(response) =>
          println(response.status.value)
          respond ! FinishMakingFriends
        case Failure(error) =>
          println(error.getMessage)
          self ! StartMakingFriends
      }
    }


    case MakingAlbums =>{
      val respond:ActorRef=sender

      val url = "http://127.0.0.1:8080/" + id + "/albums?name=myalbum&intro=this_is_my_album"
      println("URL: " + url)
      val fResponse:Future[HttpResponse]  = clientPipeline {
        Post(url)
      }
      fResponse onComplete {
        case Success(response) => {
          println(response.status.value)
          val s = response.entity.asString.split(" ")
          album = s(s.size-1).trim().toInt
          respond!FinishMakingAlbums
        }
        case Failure(error) => self ! MakingAlbums
      }
    }

    case StartSimulation =>{
      var postInterval= Duration(10, SECONDS)
      var getInterval= Duration(10, SECONDS)

      if (behavior.equals("Crazy_Poster")){
        postInterval= Duration(5, SECONDS)
      }
      else if (behavior.equals("Crazy_Getter")){
        getInterval= Duration(5, SECONDS)
      }
      else if (behavior.equals("Slacker")){
        postInterval= Duration(30, SECONDS)
        getInterval= Duration(30, SECONDS)
      }
      context.system.scheduler.schedule(Duration(0,SECONDS), postInterval, self, ClientPost)
      context.system.scheduler.schedule(Duration(20,SECONDS), getInterval, self, ClientGet)
    }


    case ClientPost => {
      val PostIntructionPool = Seq[String]("/posts?title=GO&content=Gator", "/albums?name=Gator&intro=UF", "/albums/"+album+"/photos?name=nice")
      val url="http://127.0.0.1:8080/" + id + PostIntructionPool(Random.nextInt(PostIntructionPool.length))
      val fResponse = clientPipeline {
        Post(url)
      }
      fResponse onComplete {
        case Success(response) =>
          println("URL is " + url)
          println(response.entity.asString)
        case Failure(error) => println(error.getMessage)
      }
    }

    case ClientGet => {
      val GetIntructionPool = Seq[String]("", "?id&name&bod", "/posts", "/posts?id&title&content", "/albums", "/albums?id&name&intro", "/albums/photos", "/albums/photos?id&name", "/friends")
      val url="http://127.0.0.1:8080/" + id + GetIntructionPool(Random.nextInt(GetIntructionPool.length))
      val fResponse = clientPipeline {
        Get(url)
      }
      fResponse onComplete {
        case Success(response) =>
          println("URL is " + url)
          println(response.entity.asString)
        case Failure(error) => println(error.getMessage)
      }
    }


  }
}