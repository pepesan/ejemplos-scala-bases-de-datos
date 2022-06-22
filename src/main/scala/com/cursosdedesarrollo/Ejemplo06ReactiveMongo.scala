package com.cursosdedesarrollo

import reactivemongo.api.bson.collection.BSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.api.{AsyncDriver, Cursor, DB, MongoConnection}
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, Macros, document}
import reactivemongo.api.commands.WriteResult

import scala.util.{Failure, Success}
object Ejemplo06ReactiveMongo {
  def main(args: Array[String]): Unit = {
    val mongoUri = "mongodb://localhost:27017/test"

    val driver = new AsyncDriver

    val database = for {
      uri <- MongoConnection.fromString(mongoUri)
      con <- driver.connect(uri)
      dn <- Future(uri.db.get)
      db <- con.database(dn)
    } yield db

    database.onComplete {
      case resolution =>
        println(s"DB resolution: $resolution")
        val collection: Future[BSONCollection] = database.map(_.collection("test"))
        collection onComplete {
          case Success(coll) => {
            println(collection)
            // tenemos la collección dentro de value
            val document1 = BSONDocument(
              "firstName" -> "Stephane",
              "lastName" -> "Godbillon",
              "age" -> 29)
            val writeRes: Future[WriteResult] = coll.insert.one(document1)
            writeRes.onComplete { // Dummy callbacks
              case Failure(e) => e.printStackTrace()
              case Success(writeResult) =>
                println(s"successfully inserted document with result: $writeResult")
            }

          }
          case Failure(exception) => println(exception.getMessage)
        }
        Thread.sleep(1000)
        driver.close()
    }
  }

}
