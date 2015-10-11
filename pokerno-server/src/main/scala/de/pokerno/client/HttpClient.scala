package de.pokerno.client

trait HttpClient {
  def baseUrl: String

  import com.fasterxml.jackson.databind.ObjectMapper

  import org.apache.http.entity.{ByteArrayEntity, StringEntity}
  import org.apache.http.client.methods.HttpEntityEnclosingRequestBase
  import org.apache.http.client.methods.HttpRequestBase
  import org.apache.http.impl.client.HttpClients
  import org.apache.http.client.methods.{HttpGet, HttpPost, HttpPut, HttpDelete}
  import org.apache.http.client.utils.URIBuilder

  private val mapper = new ObjectMapper
  final val defaultContentType = "application/json"

  type Data = Either[Option[Any], String]
  case class Builder(
      url: String,
      data: Data = Left(None),
      params: Map[String, Any] = Map.empty,
      headers: Map[String, String] = Map(
          "Content-Type" -> defaultContentType
          )
      ) {

    private def client = HttpClients.createDefault

    def data(s: String): Builder = copy(data = Right(s))
    def data(s: Any): Builder = copy(data = Left(Some(s)))
    def params(d: Map[String, Any]) = copy(params = d)

    def get() = {
      val req = new HttpGet(url)
      val resp = request(req)
      resp.getEntity().getContent()
    }

    def post() {
      val req = new HttpPost(url)
      requestWithEntity(req)
    }

    def put() {
      val req = new HttpPut(url)
      requestWithEntity(req)
    }

    def delete() {
      val req = new HttpDelete(url)
      request(req)
    }

    private def requestWithEntity(req: HttpEntityEnclosingRequestBase) = {
      data match {
        case Left(Some(data)) =>
          val entity = new StringEntity(mapper.writeValueAsString(data))
          req.setEntity(entity)
        case Right(str) =>
          val entity = new StringEntity(str)
          req.setEntity(entity)
        case _ =>
      }
      request(req)
    }

    private def request(req: HttpRequestBase) = {
      params.foreach { case (k, v) =>
        req.getParams().setParameter(k, v)
      }
      headers.foreach { case (k, v) =>
        req.setHeader(k, v)
      }
      client.execute(req)
    }
  }

  def path(p: String) = Builder(baseUrl+p)

}
