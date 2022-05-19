package com.evolutiongaming.chaosmesh.circe.common

import io.circe._
import cats.syntax.all._
import scala.reflect.ClassTag

private[circe] object CirceOps {

  implicit class JsonObjOps(val json: JsonObject) extends AnyVal {

    def addType(typeFiled: String, typeFieldValue: String): JsonObject =
      json.add(typeFiled, Json.fromString(typeFieldValue))

    def deepMergeObjInField(field: String): JsonObject = {
      val objectToMerge = json(field).flatMap(_.asObject)
      objectToMerge.fold(json)(json.deepMerge)
    }

    def renameField(oldName: String, newName: String): JsonObject =
      json(oldName).fold(json) { value =>
        json.add(newName, value).remove(oldName)
      }
  }

  implicit class CursorOps(val cursor: ACursor) extends AnyVal {
    def replaceFieldValue(field: String, newValue: Json): ACursor =
      cursor.downField(field).set(newValue).up

    def renameField(oldName: String, newName: String): ACursor = {
      val value = cursor.downField(oldName).focus
      value.fold(cursor) { value =>
        cursor.withFocus(_.deepMerge(Json.obj(newName -> value))).downField(oldName).delete
      }
    }
  }

  implicit class DecOps[A](val decoder: Decoder[A]) extends AnyVal {
    def narrow[B <: A](implicit c: ClassTag[B]): Decoder[B] =
      decoder.emap {
        case b: B => b.asRight
        case _ => s"Not a $c type".asLeft
      }
  }

  implicit class EncOps[A](val encoder: Encoder[A]) extends AnyVal {
    def narrow[B <: A]: Encoder[B] =
      encoder.contramap(identity)
  }

}
