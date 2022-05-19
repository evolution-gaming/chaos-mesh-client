package com.evolutiongaming.chaosmesh.circe.spec

import io.circe._
import com.evolutiongaming.chaosmesh.model.spec.Mode
import scala.util.Try

trait ModeInstances {

  protected val ModeField = "mode"

  protected val ModeValueField = "value"

  implicit val modeEnc: Encoder.AsObject[Mode] =
    Encoder.forProduct2(ModeField, ModeValueField) { mode =>
      (mode.mode, mode.value)
    }

  implicit val modeDec: Decoder[Mode] =
    Decoder.instanceTry { c =>
      for {
        modeField  <- c.get[String](ModeField).toTry
        valueField <- c.get[Option[String]](ModeValueField).toTry
        value = valueField.flatMap(v => Try(v.toInt).toOption)
        mode <- Mode.from[Try](modeField, value)
      } yield mode
    }
}
