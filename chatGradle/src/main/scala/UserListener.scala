import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.pubsub.Topic
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent.MemberEvent
import akka.cluster.ClusterEvent.MemberRemoved
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.ClusterEvent.ReachabilityEvent
import akka.cluster.ClusterEvent.ReachableMember
import akka.cluster.ClusterEvent.UnreachableMember
import akka.cluster.typed.Cluster
import akka.cluster.typed.Subscribe


//данный актор подписывается на события членства кластера, рассылает всем юзерам ActorRef нового участника
//для того что бы можно было наладить приватное общение с юзером
object UserListener {

  sealed trait Event
  private final case class ReachabilityChange(reachabilityEvent: ReachabilityEvent) extends Event
  private final case class MemberChange(event: MemberEvent) extends Event
  case class BroadcastRefUser(actorRef: ActorRef[User.Command], name: String) extends Event with CborSerializable
  case class RefUser(actorRef: ActorRef[User.Command], name: String) extends Event with CborSerializable
  case class MembersChat(membersChat: java.util.Map[java.lang.String, ActorRef[User.Command]]) extends Event with CborSerializable

  def apply(name: String): Behavior[Event] = Behaviors.setup { ctx =>
    //создаем тему с помощью которой рассылаем всем участникам чата ActorRef нового участника
    val topicRef: ActorRef[Topic.Command[RefUser]] = ctx.spawn(Topic[RefUser]("chatRef"), "chatRef");
    topicRef ! Topic.Subscribe(ctx.self)

    //создаем тему с помощью которой добиваемся что бы у нового юзера
    //появились ActorRef всех участников прибывших до него
    val topicMember: ActorRef[Topic.Command[MembersChat]] = ctx.spawn(Topic[MembersChat]("member"), "member");
    topicMember ! Topic.Subscribe(ctx.self)

    //подписываемся на события членства кластера
    val memberEventAdapter: ActorRef[MemberEvent] = ctx.messageAdapter(MemberChange)
    Cluster(ctx.system).subscriptions ! Subscribe(memberEventAdapter, classOf[MemberEvent])

    ////подписываемся на события доступности членов кластера
    val reachabilityAdapter = ctx.messageAdapter(ReachabilityChange)
    Cluster(ctx.system).subscriptions ! Subscribe(reachabilityAdapter, classOf[ReachabilityEvent])

    Behaviors.receiveMessage { message =>
      message match {
          //рассылаем ActorRef вновь созданного юзера
        case BroadcastRefUser(actorRef, name) =>
          Thread.sleep(3000);
          topicRef ! Topic.Publish(RefUser(actorRef, name))
          Behaviors.same

          //добавляем нового юзера в коллекцию членов чата
        case RefUser(actorRef, name) =>
          Main.membersChat.put(name, actorRef)
          Behaviors.same

          //новым участникам чата присылаем коллекцию ActorRef со всеми
          //участниками чата присоединившемся до него
        case MembersChat(member) =>
          if(Main.membersChat.size() < member.size()) {
            Main.membersChat = member
          }

        case ReachabilityChange(reachabilityEvent) =>
          reachabilityEvent match {
            case UnreachableMember(member) =>
              ctx.log.info("Member detected as unreachable: {}", member)
            case ReachableMember(member) =>
              ctx.log.info("Member back to reachable: {}", member)
          }

        case MemberChange(changeEvent) =>
          changeEvent match {
            case MemberUp(member) =>
              Thread.sleep(2000);
              //рассылаем коллекцию со всеми участниками чата
              topicMember ! Topic.Publish(MembersChat(Main.membersChat))
              ctx.log.info("Member is Up: {}", member.address)

            case MemberRemoved(member, previousStatus) =>
              ctx.log.info("Member is Removed: {} after {}",
                member.address, previousStatus)
            case _: MemberEvent => // ignore
          }
      }
      Behaviors.same
    }
  }
}