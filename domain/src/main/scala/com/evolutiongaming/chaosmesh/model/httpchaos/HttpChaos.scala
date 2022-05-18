package com.evolutiongaming.chaosmesh.model.httpchaos

import cats.data._
import cats.syntax.all._
import com.evolutiongaming.chaosmesh.model.k8s._
import com.evolutiongaming.chaosmesh.model.spec.Attributes._
import com.evolutiongaming.chaosmesh.model.spec._

import scala.collection.immutable.SortedMap
import scala.concurrent.duration._
import scala.util.control.NoStackTrace

final case class HttpChaos(
  metadata: ResourceMetadata,
  spec:     HttpChaos.Spec,
  kind:     ExperimentKind.HttpChaos.type = ExperimentKind.HttpChaos,
) extends CustomResource[HttpChaos.Spec, ExperimentKind.HttpChaos.type]

object HttpChaos {

  /**
    * Simulate the fault scenarios of the HTTP server during the HTTP request and response processing.
    * 
    * NOTES:
    * There is no control manager of Chaos Mesh running on the target Pod.
    * 
    * HTTPS accesses should be disabled, because injecting HTTPS connections is not supported currently.
    * 
    * For HTTPChaos injection to take effect, the client should avoid reusing TCP socket.
    * This is because HTTPChaos does not affect the HTTP requests
    * that are sent via TCP socket before the fault injection.
    * 
    * Use non-idempotent requests (such as most of the POST requests) with caution in production environments.
    * If such requests are used, the target service may not return to normal status by
    * repeating requests after the fault injection.
    *
    * @param mode - Specifies the mode of the experiment
    * @param selector - Specifies the target Pod
    * @param duration - Duration of experiment
    * @param target - Specifies whether the target of fault injection is Request or Response
    * @param port - The TCP port that the target service listens on
    * @param path - The URI path of the target request. Supports Matching wildcards
    * Takes effect on all paths by default
    * @param method - The HTTP method of the target request method.
    * Takes effect for all methods by default
    * @param requestHeaders - Matches request headers to the target service.
    * Takes effect for all requests by default.
    * @param abort - Indicates whether to inject the fault that interrupts server connection
    * @param delay - Specifies the time for a latency fault
    * @param replace - Specifies replaced values in request/response
    * @param patch - Specifies patch faults of the request/response values
    * @param scheduler - Specifies the scheduling rules for the time of a specific experiment
    */
  final case class Spec(
    mode:     Mode,
    selector: Selectors[Selectors.Filled],
    duration: FiniteDuration,
    target:   HttpChaos.Target,
    port:     Int,
    path:     Option[String] = None,
    method:   Option[String] = None,
    // Note: all of a sudden field should be encoded in snake case
    requestHeaders: Option[NonEmptyMap[String, String]] = None,
    abort:          Boolean = false,
    delay:          FiniteDuration = 0.seconds,
    replace:        Option[Replace] = None,
    patch:          Option[Patch] = None,
    scheduler:      Option[String] = None,
  ) extends HasMode
      with HasSelectors
      with HasDuration {

    def matchingPath(path: String) =
      copy(path = path.some)

    def matchingMethod(method: String) =
      copy(method = method.some)

    def matchingHeaders(headers: (String, String)*) =
      copy(requestHeaders = NonEmptyMap.fromMap(SortedMap(headers: _*)))

    def abortRequest(abort: Boolean) =
      copy(abort = abort)

    /**
      * Specifies the key pair used to replace the request headers or response headers
      *
      */
    def withReplaceHeaders(first: (String, String), rest: (String, String)*) =
      updateReplace(_.copy(headers = NonEmptyMap.of(first, rest: _*).some))

    /**
      * Specifies request body or response body to replace the fault (Base64 encoded)
      *
      */
    def withReplaceBodyBytes(first: Byte, rest: Byte*) =
      updateReplace(_.copy(body = NonEmptyList.of(first, rest: _*).some))

    private def updateReplace(f: Replace => Replace) = {
      val updatedReplace = replace.fold(Replace())(f)
      copy(replace = updatedReplace.some)
    }
  }

  sealed trait Target

  object Target {

    final case class Request(
      replace: Option[RequestReplace] = None,
      patch:   Option[RequestPatch] = None,
    ) extends Target {

      /**
        * Specifies the URI path used to replace content
        *
        */
      def withReplacedPath(path: String) =
        updateReplace(_.copy(path = path.some))

      /**
        * Specifies the replaced content of the HTTP request method
        *
        */
      def withReplacedMethod(method: String) =
        updateReplace(_.copy(method = method.some))

      /**
        * Specifies the replaced key pair of the URI query
        *
        */
      def withReplacedQueries(first: (String, String), rest: (String, String)*) =
        updateReplace(_.copy(queries = NonEmptyMap.of(first, rest: _*).some))

      /**
        * Specifies the attached key pair of the URI query with patch faults
        *
        */
      def withPatchedQueries(first: (String, String), rest: (String, String)*) = {
        val asNelFirst = NonEmptyList.of(first._1, first._2)
        val asNelRest = rest.map {
          case (first, second) => NonEmptyList.of(first, second)
        }
        updatePatch(_.copy(NonEmptyList.of(asNelFirst, asNelRest: _*).some))
      }

      private def updateReplace(f: RequestReplace => RequestReplace) = {
        val updated = replace.fold(RequestReplace())(f)
        copy(replace = updated.some)
      }

      private def updatePatch(f: RequestPatch => RequestPatch) = {
        val updated = patch.fold(RequestPatch())(f)
        copy(patch = updated.some)
      }
    }

    final case class Response(
      code: Option[Int],
      // Note: all of a sudden field should be encoded in snake case
      responseHeaders: Option[NonEmptyMap[String, String]],
      replace:         Option[ResponseReplace] = None,
    ) extends Target {

      /**
        * Specifies match by the status code responded by target. By default takes effect for all status codes
        *
        */
      def withMatchResponseCode(code: Int) =
        copy(code = code.some)

      /**
        * Matches request headers to target. Takes effect for all responses by default.
        *
        */
      def withMatchResponseHeaders(headers: (String, String)*) =
        copy(responseHeaders = NonEmptyMap.fromMap(SortedMap(headers: _*)))

      /**
        * Specifies the replaced content of the response status code.
        *
        */
      def withReplacedResponseCode(code: Int) =
        updateReplace(_.copy(code = code.some))

      private def updateReplace(f: ResponseReplace => ResponseReplace) = {
        val updated = replace.fold(ResponseReplace())(f)
        copy(replace = updated.some)
      }

    }

  }

  final case class Replace(
    headers: Option[NonEmptyMap[String, String]] = None,
    body:    Option[NonEmptyList[Byte]] = None,
  )

  final private[chaosmesh] case class RequestReplace(
    path:    Option[String] = None,
    method:  Option[String] = None,
    queries: Option[NonEmptyMap[String, String]] = None,
  )

  final private[chaosmesh] case class ResponseReplace(
    code: Option[Int] = None,
  )

  final case class Patch(
    headers: Option[NonEmptyList[NonEmptyList[String]]] = None,
    body:    Option[Body] = None,
  )

  final private[chaosmesh] case class Body(
    `type`: Option[String] = None,
    value:  Option[String] = None,
  )

  final private[chaosmesh] case class RequestPatch(
    queries: Option[NonEmptyList[NonEmptyList[String]]] = None,
  )

  final case class UnknownHttpChaosTarget(msg: String)
      extends RuntimeException(msg)
      with NoStackTrace

}
