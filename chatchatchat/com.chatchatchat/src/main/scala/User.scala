import User.{Command, Message, Tick, keys}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

import scala.concurrent.duration.DurationInt
import scala.io.StdIn

object User {

  sealed trait Command
  case class Message(msg: String, name: String) extends Command
  private case object Tick extends Command

  var keys: Map[String, ActorRef[Chat.Command]] = Map.empty

  def apply(name: String, nameChat: String): Behavior[Command] =
    Behaviors.setup{ context =>
      Behaviors.withTimers { timers =>

      val user = new User(context, name, nameChat)
        //если чат нет то его нужно создать
        if(!keys.contains(nameChat)) {
          user.chatAdd
        } else {
          //иначе берем чат из коллекции и подписываемся на него отправиви ему свою ссылку
          println("Данный чат существует, подписываемся на него")
          user.chatRef = keys(nameChat)
          user.chatRef ! Chat.Subscribe(context.self)
        }
        //раз в секунду считываем с консоли юзера текст который он хочет отправить  в чат
        timers.startTimerWithFixedDelay(Tick, Tick, 1.seconds)
        user
      }
}
}

  class User (context: ActorContext[User.Command], name: String, nameChat: String)
    extends AbstractBehavior[User.Command](context) {

    var chatRef: ActorRef[Chat.Command] = null

    def chatAdd: Unit = {
      chatRef = context.spawn(Chat(nameChat), nameChat)
      chatRef ! Chat.Subscribe(context.self)
      keys += (nameChat -> chatRef)
      println("Создался новый чат {}", nameChat)
    }

    override def onMessage(msg: Command): Behavior[Command] = {
      msg match {
        case Tick =>
          //раз в секунду считываем с консоли юзера текст который он хочет отправить  в чат
          //println("7777777777777")
          val message = StdIn.readLine()
          if(message != null) {
            chatRef ! Chat.Message(message, name)
          }
          Behaviors.same

        case Message(msg, name) =>
          //принимаем сообщения из чата
          println(name)
          println("---------------")
          println(msg)
          Behaviors.same
      }
  }
}
