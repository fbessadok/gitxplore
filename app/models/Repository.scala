package models

import scala.concurrent._
import ExecutionContext.Implicits.global

import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import utils.GithubAPI

case class Repository(fullname: String, stars: Int)

object Repository {

  implicit def parseRepository: Reads[Seq[Repository]] = {
    (__ \ "items").read(
      seq(
        (__ \ "full_name").read[String] and
        (__ \ "stargazers_count").read[Int]
        tupled
      )
    ).map(
      _.collect {
        case (full_name, stargazers_count) => Repository(full_name, stargazers_count)
      }
    )
  }

  def search(query: String): Future[Seq[Repository]] =
    WS.url(GithubAPI.search.format(query))
    .get().map(
      r => r.status match {
        case 200 => r.json.asOpt[Seq[Repository]].getOrElse(Nil)
        case e => sys.error(s"Bad response. Status $e")
      }
    )
}