import akka.actor.{ ActorRef, ActorSystem, Props, Actor, Inbox }
import akka.event.Logging
import scala.concurrent.duration._
import akka.dispatch.RequiresMessageQueue
import akka.dispatch.BoundedMessageQueueSemantics




class ActorTest extends Actor{
  val log = Logging(context.system, this)
  override def receive = {
    case "test" => log.info("received test");
    case _ => log.info("unknown info")
  }
}

object Main extends App{
  val system = ActorSystem("testSystem")
  val actorTest = system.actorOf(Props[ActorTest].withMailbox("bounded-mailbox"), name = "actorTest")
  import system.dispatcher
  system.scheduler.schedule(100 milliseconds, 100 milliseconds, actorTest, "test")
  system.scheduler.schedule(50 milliseconds, 100 milliseconds, actorTest, "te")
}
