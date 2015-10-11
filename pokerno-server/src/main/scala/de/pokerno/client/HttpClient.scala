package de.pokerno.client

trait HttpClient {
  def baseUrl: String
  
  import com.fasterxml.jackson.databind.ObjectMapper

  import org.apache.http.entity.ByteArrayEntity
  import org.apache.http.client.methods.HttpEntityEnclosingRequestBase
  import org.apache.http.client.methods.HttpRequestBase
  import org.apache.http.impl.client.HttpClients
  import org.apache.http.client.methods.{HttpGet, HttpPost, HttpPut, HttpDelete}
  import org.apache.http.client.utils.URIBuilder

  private val mapper = new ObjectMapper

  private val client = HttpClients.createDefault

  protected def get(url: String, options: Options) = {
    val req = new HttpGet(baseUrl+url)
    val resp = request(req, options)
    resp.getEntity.getContent
  }

  type Data = Either[Option[Any], String]
  case class Options(
      data: Data = Left(None),
      params: Map[String, Any] = Map.empty
      )

  protected def post(url: String, options: Options) {
    val req = new HttpPost(baseUrl+url)
    requestWithEntity(req, options)
  }

  protected def put(url: String, options: Options) {
    val req = new HttpPut(baseUrl+url)
    requestWithEntity(req, options)
  }

  protected def delete(url: String, options: Options) {
    val req = new HttpDelete(baseUrl+url)
    request(req, options)
  }

  private def requestWithEntity(req: HttpEntityEnclosingRequestBase, options: Options) = {
    options.data match {
      case Left(Some(data)) =>
        val entity = new ByteArrayEntity(mapper.writeValueAsBytes(data))
        req.setEntity(entity)
      case Right(str) =>
        val entity = new ByteArrayEntity(str.getBytes("UTF-8"))
        req.setEntity(entity)
      case _ =>
    }
    request(req, options)
  }

  private def request(req: HttpRequestBase, options: Options) = {
    val params = req.getParams()
    options.params.foreach { case (k, v) =>
      params.setParameter(k, v)
    }
    client.execute(req)
  }

}

trait RestClient {
  final val defaultContentType = "application/json"
}
