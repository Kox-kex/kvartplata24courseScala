import Chat.{BroadcastMessage, Command, Message}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.pubsub.Topic
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object Chat {

  sealed trait Command
  case class BroadcastMessage(msg: String, name: String) extends Command with CborSerializable
  case class Message(msg: String, name: String) extends Command with CborSerializable

  def apply(nameChat: String): Behavior[Command] =
    Behaviors.setup { context =>
      new Chat(context, nameChat)
    }
}

  class Chat(ctx: ActorContext[Chat.Command], nameChat: String) extends AbstractBehavior[Chat.Command](ctx) {

    var topic: ActorRef[Topic.Command[Message]] = ctx.spawn(Topic[Message]("chat"), "chat");

    topic ! Topic.Subscribe(ctx.self)

    override def onMessage(msg: Command): Behavior[Command] = {
      msg match {
          //обрабатываем сообщение т.е. рассылаем всем подписчикам topic(чата)
        case BroadcastMessage(msg, name) =>
          topic ! Topic.Publish(Message(msg, name))
          Behaviors.same

        case Message(msg, name) =>
          println(msg)
          println(name)
          Behaviors.same
      }
    }
      }

