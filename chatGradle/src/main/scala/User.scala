import User.{Command, Message, PrivetMessage}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import javafx.application.Platform


object User {

  sealed trait Command
  case class Message(msg: String, name: String) extends Command with CborSerializable
  case class PrivetMessage(msg: String, name: String) extends Command with CborSerializable

  def apply(name: String): Behavior[Command] =
    Behaviors.setup{ context =>
      val user = new User(context)
      //создаем userListerner который следит за событиями чата и тут же отправляем ему ActorRef юзера
      val userListerner: ActorRef[UserListener.Event] = context.spawn(UserListener(name = ""), "userListerner")
      userListerner ! UserListener.BroadcastRefUser(context.self, name)

      user
    }
}

class User(context: ActorContext[Command])
  extends AbstractBehavior[User.Command](context) {

  //при инициализации актера юзера так же иниацилизируется актер чат
  var chat: ActorRef[Chat.Command] = context.spawn(Chat(name = ""), "chat")

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      //отправляем в чат на рассылку участникам
      case Message(msg, name) =>
        chat ! Chat.BroadcastMessage(msg, name)
        Behaviors.same

      //с помощью Platform.runLater выводим сообщение в UI
      case PrivetMessage(msg, name) =>
        Platform.runLater(new Runnable {
          override def run(): Unit = Main.txtAreaDisplay.appendText(s"[PrivetMessage]: [$name]: $msg\n")
        })
        Behaviors.same
    }
  }
}
