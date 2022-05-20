package com.evolutiongaming.chaosmesh.model.spec

import cats.Id
import cats.data.NonEmptyList
import cats.syntax.all._

import scala.concurrent.duration.FiniteDuration

sealed trait Action

object Action {

  sealed trait PodChaos extends Action

  object PodChaos {

    /**
      * Injects fault into a specified Pod to make the Pod unavailable for a period of time
      */
    case object PodFailure extends PodChaos

    /**
      * Kills a specified Pod
      *
      * @param gracePeriod - Duration before deleting Pod
      */
    case class PodKill(
      gracePeriod: FiniteDuration,
    ) extends PodChaos

    /**
      * Kills the specified containers in the target Pod
      *
      * @param containerNames - Target containers
      */
    case class ContainerKill(
      containerNames: NonEmptyList[String],
    ) extends PodChaos
        with Attributes.HasTargetContainers[Id]

    object ContainerKill {

      /**
        * Specifies target container names
        *
        */
      def apply(first: String, rest: String*): ContainerKill =
        ContainerKill(NonEmptyList.of(first, rest: _*))
    }

  }

  sealed trait NetChaos extends Action

  object NetChaos {

    /**
      * Network disconnection and partition
      */
    case object NetPartition extends NetChaos

    /**
      * Simulating bandwidth limit fault
      * https://chaos-mesh.org/docs/simulate-network-chaos-on-kubernetes/#bandwidth
      *
      * @param bandwidth - Indicates bandwidth limit rules 
      */
    final case class BandwidthLimit private (
      bandwidth: BandwidthLimitRules,
    ) extends NetChaos {

      /**
        * Specifies the maximum consumption of bucket
        *
        */
      def withPeakRate(peakRate: Long) =
        updateRules(_.copy(peakrate = peakRate.some))

      /**
        * Specifies the size of peakrate bucket
        *
        */
      def withPeakRateBucketSize(size: Int) =
        updateRules(_.copy(minburst = size.some))

      private def updateRules(f: BandwidthLimitRules => BandwidthLimitRules) =
        copy(bandwidth = f(bandwidth))

    }

    object BandwidthLimit {

      /**
        * Simulating bandwidth limit fault
        *
        * @param rate - Specifies the rate of bandwidth limit in bytes per second
        * @param limit - Specifies the number of bytes waiting in queue
        * @param buffer - Specifies the maximum number of bytes that can be sent instantaneously
        */
      def apply(
        rate:   Long,
        limit:  Long,
        buffer: Int,
      ): BandwidthLimit =
        BandwidthLimit(BandwidthLimitRules(s"${rate}bps", limit, buffer))
    }

    /**
      * Indicates bandwidth limit rules
      *
      * @param rate - Indicates the rate of bandwidth limit in bytes per second
      * @param limit - Indicates the number of bytes waiting in queue
      * @param buffer - Indicates the maximum number of bytes that can be sent instantaneously
      * @param peakrate - Indicates the maximum consumption of bucket
      * @param minburst - Indicates the size of peakrate bucket
      */
    final case class BandwidthLimitRules private[spec] (
      rate:     String,
      limit:    Long,
      buffer:   Int,
      peakrate: Option[Long] = None,
      minburst: Option[Int] = None,
    )

    /**
      * Simulating packet loss fault
      * https://chaos-mesh.org/docs/simulate-network-chaos-on-kubernetes/#loss
      * 
      * @param loss - Indicates packet lost fault rules
      */
    final case class PacketLoss(
      loss: PacketLossRules = PacketLossRules(),
    ) extends NetChaos {

      /**
        * Specifies the probability of packet loss. Should be 0..100
        *
        */
      def withProbability(probability: Int) =
        updateRules(_.copy(loss = probability.toString().some))

      /**
        * Specifies the correlation between the probability of current packet loss
        * and the previous time's packet loss. Should be 0..100
        *
        */
      def withCorrelation(correlation: Int) =
        updateRules(_.copy(correlation = correlation.toString().some))

      private def updateRules(f: PacketLossRules => PacketLossRules) =
        copy(loss = f(loss))
    }

    /**
      * Indicates packet lost fault rules
      *
      * @param loss - Indicates the probability of packet loss 0..100
      * @param correlation - Indicates the correlation between the probability
      * of current packet loss and the previous time's packet loss 0..100
      */
    final case class PacketLossRules private[spec] (
      loss:        Option[String] = None,
      correlation: Option[String] = None,
    )

    /**
      * Simulating package corruption fault
      * https://chaos-mesh.org/docs/simulate-network-chaos-on-kubernetes/#corrupt
      *
      * @param corrupt - Indicates packet corrupt rules 
      */
    final case class PacketCorrupt(
      corrupt: PacketCorruptRules = PacketCorruptRules(),
    ) extends NetChaos {

      /**
        * Specifies the probability of packet corrupt. Should be 0..100
        *
        */
      def withProbability(probability: Int) =
        updateRules(_.copy(corrupt = probability.toString().some))

      /**
        * Specifies the correlation between the current corrupt and the previous one. Should be 0..100
        *
        */
      def withCorrelation(correlation: Int) =
        updateRules(_.copy(correlation = correlation.toString().some))

      private def updateRules(f: PacketCorruptRules => PacketCorruptRules) =
        copy(corrupt = f(corrupt))

    }

    /**
      * Indicates packet corrupt rules
      *
      * @param corrupt - Indicates the probability of packet corruption 0..100
      * @param correlation - Indicates the correlation between the probability
      * of current packet corruption and the previous time's packet corruption 0..100
      */
    final case class PacketCorruptRules private[spec] (
      corrupt:     Option[String] = None,
      correlation: Option[String] = None,
    )

    /**
      * Simulating network delay fault
      * https://chaos-mesh.org/docs/simulate-network-chaos-on-kubernetes/#delay
      *
      * @param delay - Indicates delay rules
      */
    final case class Delay(
      delay: DelayRules = DelayRules(),
    ) extends NetChaos {

      /**
        * Specifies the network latency
        *
        */
      def withLatency(duration: FiniteDuration) =
        Delay(delay.copy(latency = duration.some))

      /**
        * Specifies the correlation between the current latency and the previous one. Should be 0..100
        *
        */
      def withCorrelation(correlation: Int) =
        Delay(delay.copy(correlation = correlation.toString.some))

      /**
        * Specifies the range of the network latency
        *
        */
      def withJitter(jitter: FiniteDuration) =
        Delay(delay.copy(jitter = jitter.some))

      /**
        * Specifies the probability to reorder. Should be 0..100
        *
        */
      def withReorderingProbability(probability: Int) =
        updateReordering(_.copy(reorder = probability.toString.some))

      /**
        * Specifies the correlation between this time's length of delay time
        * and the previous time's length of delay time. Should be 0..100
        *
        */
      def withReorderingCorrelation(corr: Int) =
        updateReordering(_.copy(correlation = corr.toString.some))

      /**
        * Specifies the gap before and after packet reordering	
        *
        */
      def withReorderingGap(gap: Int) =
        updateReordering(_.copy(gap = gap.some))

      private def updateReordering(f: PacketReorder => PacketReorder) = {
        val updated = delay.reorder.fold(PacketReorder())(f)
        Delay(delay.copy(reorder = updated.some))
      }

    }

    /**
      * Indicates network delay fault
      *
      * @param latency - Indicates the network latency
      * @param correlation - Indicates the correlation between the current latency and the previous one 0..100
      * @param jitter - Indicates the range of the network latency
      * @param reorder - Indicates packet reordering fault rules
      */
    final case class DelayRules private[spec] (
      latency:     Option[FiniteDuration] = None,
      correlation: Option[String] = None,
      jitter:      Option[FiniteDuration] = None,
      reorder:     Option[PacketReorder] = None,
    )

    /**
      * Indicates network packet reordering fault
      *
      * @param reorder - Indicates the probability to reorder 0..100
      * @param correlation - Indicates the correlation between this time's length of delay time
      * and the previous time's length of delay time 0..100
      * @param gap - Indicates the gap before and after packet reordering
      */
    final case class PacketReorder private[spec] (
      reorder:     Option[String] = None,
      correlation: Option[String] = None,
      gap:         Option[Int] = None,
    )

    /**
      * Simulating package duplication
      * https://chaos-mesh.org/docs/simulate-network-chaos-on-kubernetes/#duplicate
      *
      * @param duplicate - Indicates packet duplicate rules 
      */
    final case class PacketDuplicate(
      duplicate: PacketDuplicateRules = PacketDuplicateRules(),
    ) extends NetChaos {

      /**
        * Specifies the probability of packet duplicating. Should be 0..100
        *
        */
      def withProbability(probability: Int) =
        updateRules(_.copy(duplicate = probability.toString().some))

      /**
        * Specifies the correlation between the probability of current packet duplicating
        * and the previous time's packet duplicating. Should be 0..100
        *
        */
      def withCorrelation(correlation: Int) =
        updateRules(_.copy(correlation = correlation.toString().some))

      private def updateRules(f: PacketDuplicateRules => PacketDuplicateRules) =
        copy(duplicate = f(duplicate))

    }

    /**
      * Indicates packet duplicate rules
      *
      * @param duplicate - Indicates the probability of packet duplicating 0..100
      * @param correlation - Indicates the correlation between the probability 
      * of current packet duplicating and the previous time's packet duplicating 0..100
      */
    final case class PacketDuplicateRules private[spec] (
      duplicate:   Option[String] = None,
      correlation: Option[String] = None,
    )

  }

  sealed trait IoChaos extends Action

  object IoChaos {

    /**
      * Simulating delays file system calls
      * 
      * @param delay - Specific delay time
      */
    final case class Latency(
      delay: FiniteDuration,
    ) extends IoChaos

    /**
      * Filesystem calls returns an error
      *
      * @param errno - Returned error number
      * see https://chaos-mesh.org/docs/simulate-io-chaos-on-kubernetes/#appendix-b-common-error-numbers
      */
    final case class Fault(
      errno: Int,
    ) extends IoChaos

    /**
      * Modifies file properties
      *
      * @param attr - Specific property override rules
      */
    final case class AttrOverride(
      attr: AttrOverrideRules = AttrOverrideRules(),
    ) extends IoChaos {

      /**
        * Specifies Inode number overriding rule
        *
        */
      def withOverrideIno(ino: Int) =
        updateRules(_.copy(ino = ino.some))

      /**
        * Specifies file size overriding rule
        *
        */
      def withOverrideSize(size: Int) =
        updateRules(_.copy(size = size.some))

      /**
        * Specifies overriding rule for number of block the file uses
        *
        */
      def withOverrideBlocksNumber(number: Int) =
        updateRules(_.copy(blocks = number.some))

      /**
        * Specifies overriding rule for last access time of file in nanoseconds
        *
        */
      def withLastAccessTimeNano(nanos: Long) =
        updateRules(_.atimeUpdate(_.copy(nsec = nanos.some)))

      /**
        * Specifies overriding rule for last modified time of file in nanoseconds
        *
        */
      def withLastModifiedTimeNano(nanos: Long) =
        updateRules(_.mtimeUpdate(_.copy(nsec = nanos.some)))

      /**
        * Specifies overriding rule for last status change time of file in nanoseconds
        *
        */
      def withLastStatusChangeTimeNano(nanos: Long) =
        updateRules(_.ctimeUpdate(_.copy(nsec = nanos.some)))

      /**
        * Specifies overriding rule for file kind
        * see https://docs.rs/fuser/0.7.0/fuser/enum.FileType.html
        *
        */
      def withKind(kind: String) =
        updateRules(_.copy(kind = kind.some))

      /**
        * Specifies overriding rule for file permissions in decimal
        *
        */
      def withPermission(permission: Int) =
        updateRules(_.copy(perm = permission.some))

      /**
        * Specifies overriding rule for number of hard links
        *
        */
      def withNumberOfHardLinks(num: Int) =
        updateRules(_.copy(nlink = num.some))

      /**
        * Specifies overriding rule for owner ID
        *
        */
      def withOwnerId(id: Int) =
        updateRules(_.copy(uid = id.some))

      /**
        * Specifies overriding rule for group ID
        *
        */
      def withGroupId(id: Int) =
        updateRules(_.copy(gid = id.some))

      /**
        * Specifies overriding rule for device ID
        *
        */
      def withDeviceId(id: Int) =
        updateRules(_.copy(rdev = id.some))

      private def updateRules(f: AttrOverrideRules => AttrOverrideRules) =
        copy(attr = f(attr))
    }

    /**
      * Contains file properties override rules
      * 
      * @param ino - ino number
      * @param size - File size
      * @param blocks - Number of blocks that the file uses
      * @param atime - Last access time
      * @param mtime - Last modified time
      * @param ctime - Last status change time
      * @param kind - File type, see https://docs.rs/fuser/0.7.0/fuser/enum.FileType.html
      * @param perm - File permissions in decimal
      * @param nlink - Number of hard links
      * @param uid - User ID of the owner
      * @param gid - Group ID of the owner
      * @param rdev - Device ID
      */
    final case class AttrOverrideRules private[spec] (
      ino:    Option[Int] = None,
      size:   Option[Int] = None,
      blocks: Option[Int] = None,
      atime:  Option[TimeSpec] = None,
      mtime:  Option[TimeSpec] = None,
      ctime:  Option[TimeSpec] = None,
      kind:   Option[String] = None,
      perm:   Option[Int] = None,
      nlink:  Option[Int] = None,
      uid:    Option[Int] = None,
      gid:    Option[Int] = None,
      rdev:   Option[Int] = None,
    ) {
      private[spec] def atimeUpdate(f: TimeSpec => TimeSpec) = {
        val updated = atime.fold(TimeSpec())(f)
        copy(atime = updated.some)
      }
      private[spec] def mtimeUpdate(f: TimeSpec => TimeSpec) = {
        val updated = mtime.fold(TimeSpec())(f)
        copy(mtime = updated.some)
      }
      private[spec] def ctimeUpdate(f: TimeSpec => TimeSpec) = {
        val updated = ctime.fold(TimeSpec())(f)
        copy(ctime = updated.some)
      }
    }

    /**
      * Contains time data for file properties override
      * 
      * @param sec - timestamp in seconds
      * @param nsec - timestamp in nanoseconds
      * For the specific meaning of parameters, you can refer to man stat
      */
    final case class TimeSpec(
      sec:  Option[Long] = None,
      nsec: Option[Long] = None,
    )

    /**
      * Makes the file read or write a wrong value
      * 
      * @param mistake - Specific error rules
      */
    final case class Mistake(
      mistake: MistakeRules,
    ) extends IoChaos

    object Mistake {

      /**
        * Specifies read or write mistake faults 
        *
        * @param filling - Specifies wrong data to be filled
        * @param maxOccurrences - Specifies maximum number of errors in each operation
        * @param maxLength - Specifies maximum length of each error (in bytes)
        */
      def apply(
        filling:        MistakeFillings,
        maxOccurrences: Int,
        maxLength:      Int,
      ): Mistake = Mistake(MistakeRules(filling, maxOccurrences, maxLength))

    }

    /**
      * Indicates read or write mistake rules
      * 
      * @param filling - The wrong data to be filled. Only zero (fill 0) or random (fill random bytes) are supported
      * @param maxOccurrences - Maximum number of errors in each operation
      * @param maxLength - Maximum length of each error (in bytes)
      */
    final case class MistakeRules private[spec] (
      filling:        MistakeFillings,
      maxOccurrences: Int,
      maxLength:      Int,
    )

    sealed trait MistakeFillings

    object MistakeFillings {

      /**
        * All file read or write mistakes will be filled with zeros
        */
      case object Zeros extends MistakeFillings

      /**
        * All file read or write mistakes will be filled with random values
        */
      case object Random extends MistakeFillings
    }
  }

  sealed trait DnsChaos extends Action

  object DnsChaos {

    /**
      * DNS service returns a random IP address
      */
    object Random extends DnsChaos

    /**
      * DNS service returns an error
      */
    object Error extends DnsChaos

  }

  sealed trait JvmChaos extends Action

  object JvmChaos {

    /**
      * Increase method latency
      *
      * @param class - The name of the Java class
      * @param method - The name of the method
      * @param latency - The duration of increasing method latency in milliseconds
      */
    final case class Latency(
      `class`: String,
      method:  String,
      latency: Int,
    ) extends JvmChaos

    /**
      * Modify return values of a method
      *
      * @param class - The name of the Java class
      * @param method - The name of the method
      * @param value - Specifies the return value of the method.
      * Currently, the item can be numeric and string types.
      * If the item (return value) is string, double quotes are required, like "chaos".
      */
    final case class Return(
      `class`: String,
      method:  String,
      value:   String,
    ) extends JvmChaos

    /**
      * Throw custom exceptions
      *
      * @param class - The name of the Java class
      * @param method - The name of the method
      * @param exception - The thrown custom exception, such as 'java.io.IOException("BOOM")'
      */
    final case class Exception(
      `class`:   String,
      method:    String,
      exception: String,
    ) extends JvmChaos

    sealed trait Stress extends JvmChaos

    /**
      * Increase CPU usage of Java process
      *
      * @param cpuCount - The number of CPU cores used for increasing CPU stress
      */
    final case class CpuStress(
      cpuCount: Int,
    ) extends Stress

    /**
      * Cause memory overflow (support heap overflow and stack overflow)
      *
      * @param memType - stack or heap
      */
    final case class MemOverflow(
      memType: String,
    ) extends Stress

    /**
      * Trigger garbage collection
      */
    object GC extends JvmChaos

    /**
      * Trigger faults by setting Byteman configuration files
      *
      * @param ruleData - Specifies the Byteman configuration data.
      * see https://downloads.jboss.org/byteman/4.0.16/byteman-programmers-guide.html#the-byteman-rule-language
      * You need to escape the line breaks in the configuration file to the newline character "\n",
      * and use the escaped text as the value of "rule-data" as follows:
      * \nRULE modify return value\nCLASS Main\nMETHOD getnum\nAT ENTRY\nIF true\nDO return 9999\nENDRULE\n"
      */
    final case class RuleData(
      ruleData: String,
    ) extends JvmChaos
  }

  sealed trait AwsChaos extends Action

  object AwsChaos {

    /**
      * Stops the specified EC2 instance
      */
    object EC2Stop extends AwsChaos

    /**
      * Restarts the specified EC2 instance
      */
    object EC2Restart extends AwsChaos

    /**
      * Uninstalls the storage volume from the specified EC2 instance
      *
      * @param volumeID - This field specifies the EBS volume ID
      * @param deviceName - This field specifies the machine name
      */
    final case class DetainVolume(
      volumeID:   String,
      deviceName: String,
    ) extends AwsChaos
  }

  sealed trait GcpChaos extends Action

  object GcpChaos {

    object NodeStop extends GcpChaos

    object NodeReset extends GcpChaos

    final case class DiskLoss(
      deviceNames: NonEmptyList[String],
    ) extends GcpChaos

  }

}
