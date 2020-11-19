import User.{Command, Tick}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

import scala.concurrent.duration.DurationInt
import scala.io.StdIn

object User {

  sealed trait Command
  private case object Tick extends Command

  def apply(name: String, nameChat: String): Behavior[Command] =
    Behaviors.setup{ context =>
      Behaviors.withTimers { timers =>

        val user = new User(context, name, nameChat)
        //user.chat = context.spawn(Chat(nameChat), "chat")

        //раз в секунду считываем с консоли юзера текст который он хочет отправить  в чат
        timers.startTimerWithFixedDelay(Tick, Tick, 1.seconds)
        user
      }
    }
}

class User (context: ActorContext[User.Command], name: String, nameChat: String)
  extends AbstractBehavior[User.Command](context) {

  var chat: ActorRef[Chat.Command] = context.spawn(Chat(nameChat), "chat")

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case Tick =>
        //раз в секунду считываем с консоли юзера текст который он хочет отправить  в чат
        val message = StdIn.readLine()
        if (message != "") {
          chat ! Chat.BroadcastMessage(message, name)
        }
        Behaviors.same
    }
  }
}
