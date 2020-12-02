import Chat.{BroadcastMessage, Command, Message}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.pubsub.Topic
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import javafx.application.Platform

object Chat {

  sealed trait Command
  case class BroadcastMessage(msg: String, name: String) extends Command with CborSerializable
  case class Message(msg: String, name: String) extends Command with CborSerializable

  def apply(name: String): Behavior[Command] =
    Behaviors.setup { context =>
      new Chat(context)
    }
}

class Chat(ctx: ActorContext[Command]) extends AbstractBehavior[Chat.Command](ctx) {

  //создаем topic и далее подписываем наш чат на данную тему
  var topic: ActorRef[Topic.Command[Message]] = ctx.spawn(Topic[Message]("chat"), "chat");
  topic ! Topic.Subscribe(ctx.self)

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      //обрабатываем сообщение т.е. рассылаем всем подписчикам topic
      case BroadcastMessage(msg, name) =>
        topic ! Topic.Publish(Message(msg, name))
        Behaviors.same

      //с помощью Platform.runLater выводим сообщение в UI
      case Message(msg, name) =>
      Platform.runLater(new Runnable {
          override def run(): Unit = Main.txtAreaDisplay.appendText(s"[$name]: $msg\n")
        })
        Behaviors.same
    }
  }
}

