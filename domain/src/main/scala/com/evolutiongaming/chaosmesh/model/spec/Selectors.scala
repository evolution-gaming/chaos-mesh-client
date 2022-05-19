package com.evolutiongaming.chaosmesh.model.spec

import cats.syntax.all._
import cats.data._
import com.evolutiongaming.chaosmesh.model.k8s._
import Selectors._

/**
  * Different types of selectors correspond to different filtering rules.
  * You can specify one or more selectors in a Chaos experiment to define the scope of your experiment.
  * If multiple selectors are specified at the same time,
  * the current experiment target must meet the rules of all specified selectors at the same time.
  */
final case class Selectors[State <: Selectors.State] private (
  namespaces:          Option[NonEmptyList[String]],
  labelSelectors:      Option[NonEmptyMap[String, String]],
  expressionSelectors: Option[NonEmptyList[Expression]],
  annotationSelectors: Option[NonEmptyMap[String, String]],
  fieldSelectors:      Option[NonEmptyMap[String, String]],
  podPhaseSelectors:   Option[NonEmptyList[PodPhase]],
  nodeSelectors:       Option[NonEmptyMap[String, String]],
  nodes:               Option[NonEmptyList[String]],
  pods:                Option[NonEmptyMap[String, NonEmptyList[String]]],
) {

  /**
     * Specifies the namespaces of the experiment's target Pod
     * If this selector is empty or is not specified, Chaos Mesh will set it to the namespace of the current Chaos experiment.
     */
  def withByNamespaces(first: String, rest: String*): Selectors[Selectors.Filled] =
    copy(namespaces = NonEmptyList.of(first, rest: _*).some)

  /**
     * Specifies the labels that the experiment's target Pod must have
     * If multiple labels are specified, the experiment target must have all the labels specified by this selector
     */
  def withByLabels(first: (String, String), rest: (String, String)*): Selectors[Selectors.Filled] =
    copy(labelSelectors = NonEmptyMap.of(first, rest: _*).some)

  /**
     * Specifies a set of expressions
     * (see https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/#resources-that-support-set-based-requirements)
     * that define the label's rules to specify the experiment's target Pod
     * You can use this selector to set up the experiment's target Pod that does not meet some labels
     */
  def withByExpressions(first: Expression, rest: Expression*): Selectors[Selectors.Filled] =
    copy(expressionSelectors = NonEmptyList.of(first, rest: _*).some)

  /**
     * Specifies the annotations that the experiment's target Pod must have
     * If multiple annotations are specified, the experiment target must have all annotations specified by this selector
     */
  def withByAnnotations(first: (String, String), rest: (String, String)*): Selectors[Selectors.Filled] =
    copy(annotationSelectors = NonEmptyMap.of(first, rest: _*).some)

  /**
     * Specifies the fields of the experiment's target Pod
     * If multiple fields are specified, the experiment target must have all fields set by this selector
     */
  def withByFields(first: (String, String), rest: (String, String)*): Selectors[Selectors.Filled] =
    copy(fieldSelectors = NonEmptyMap.of(first, rest: _*).some)

  /**
    * Specifies the namespaces and list of the experiment's target Pods
    * If you have specified this selector, Chaos Mesh will **ignore** other configured selectors
    */
  def byPodPhases(first: PodPhase, rest: PodPhase*): Selectors[Selectors.Filled] =
    copy(podPhaseSelectors = NonEmptyList.of(first, rest: _*).some)

  /**
     * Specifies the node label to which the experiment's target Pod belongs
     * If multiple node labels are specified, the node to which the experiment's
     * target Pod belongs must have all labels specified by this selector
     */
  def withByNodeLabels(nodeLabels: NonEmptyMap[String, String]): Selectors[Selectors.Filled] =
    copy(nodeSelectors = nodeLabels.some)

  /**
     * Specifies the node to which the experiment's target Pod belongs
     * The target Pod can only belong to one node in the configured node list
     */
  def withByNodeNames(first: String, rest: String*): Selectors[Selectors.Filled] =
    copy(nodes = NonEmptyList.of(first, rest: _*).some)

  /**
     * Specifies the namespaces and list of the experiment's target Pods
     * If you have specified this selector, Chaos Mesh will **ignore** other configured selectors.
     */
  def byPodNames(pods: NonEmptyMap[String, NonEmptyList[String]]): Selectors[Selectors.Filled] =
    copy(pods = pods.some)

}

object Selectors {

  type Filled = State.Filled.type
  type Empty  = State.Empty.type

  sealed trait State

  object State {
    case object Filled extends State
    case object Empty  extends State
  }

  def apply(): Selectors[Selectors.Empty] =
    Selectors(None, None, None, None, None, None, None, None, None)
}
