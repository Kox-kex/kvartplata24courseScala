import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.cluster.typed.Cluster
import com.typesafe.config.ConfigFactory

object App {

  def main(args: Array[String]): Unit = {
    require(args.length == 4, "Usage: role nameChat nameUser port")
    startup(args(0), args(1), args(2), args(3).toInt)
  }

  def startup(role: String, nameChat: String, name: String, port: Int): Unit = {
    val config = ConfigFactory
      .parseString(s"""
        akka.remote.artery.canonical.port=$port
        akka.cluster.roles = [$role]
        """)
      .withFallback(ConfigFactory.load())

    ActorSystem[Nothing](RootBehavior(name, nameChat), "ClusterSystem", config)

  }

  object RootBehavior {
    def apply(name: String, nameChat: String): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>

      //val cluster = Cluster(ctx.system)

      ctx.spawn(User(name, nameChat),name)

      Behaviors.empty
    }
  }



  }
