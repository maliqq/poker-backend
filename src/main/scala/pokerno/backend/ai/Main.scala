package pokerno.backend.ai

case class Config(roomId: String)

object Main {
  
  val parser = new scopt.OptionParser[Config]("poker-bot") {
    opt[String]("room-id") action { (value, c) => c.copy(roomId = value) } text("room id")
  }
  
  def main(args: Array[String]) {
    
  }

}
