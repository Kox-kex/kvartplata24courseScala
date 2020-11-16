import Chat.{Command, Message, Subscribe, subscribers}
import akka.actor.typed.Terminated
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object Chat {

  sealed trait Command
  case class Message(msg: String, name: String) extends Command
  case class Terminated() extends Command
  case class Subscribe(replyTo: ActorRef[User.Command])  extends Command

  def apply(nameChat: String): Behavior[Command] =
    Behaviors.setup(context => new Chat(context, nameChat))

  //коллекция с подписчаками чата, которым рассылается отправленное сообщение
  var subscribers: Seq[ActorRef[User.Command]] = Seq.empty
}

class Chat(context: ActorContext[Chat.Command], nameChat: String) extends AbstractBehavior[Chat.Command](context) {

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {

      case Message(msg, name) =>
        //здесь сообщение от юзера рассылается всем подписчикам данного чата
        println(name)
        println("---------------")
        println(msg)
        subscribers.foreach(x =>
          x ! User.Message(msg, name))
        Behaviors.same

      case Subscribe(replyTo) =>
        //Подписка участника
        println("Подписка участника")
        subscribers = subscribers :+ replyTo
        //watch - следим за жизненым цикдлом подписчика если он умрет то мы получим Terminated
        context.watch(replyTo)
        Behaviors.same

      case Terminated(subscriber) =>
        //Подписчик вышел из чата
        println("удаление участника")
        subscribers = subscribers.filterNot(_ == subscriber)
        Behaviors.same

    }
  }
}
