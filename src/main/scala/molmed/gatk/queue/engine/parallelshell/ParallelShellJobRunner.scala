package molmed.gatk.queue.engine.parallelshell

import org.broadinstitute.gatk.queue.engine.CommandLineJobRunner
import org.broadinstitute.gatk.queue.function.CommandLineFunction
import org.broadinstitute.gatk.queue.engine.RunnerStatus
import molmed.shell.async.AsyncShellManager
import molmed.shell.async.AsyncShellJob
import molmed.shell.async.JobStatuses
import molmed.shell.async.JobStatuses._

object ParallelShellJobRunner {

  def apply(manager: AsyncShellManager, function: CommandLineFunction) =
    new ParallelShellJobRunner(manager, function)

  def molmedJobStatus2QueueRunnerStatus(s: JobStatus): RunnerStatus.Value = {
    s match {
      case JobStatuses.Running  => RunnerStatus.RUNNING
      case JobStatuses.Finished => RunnerStatus.DONE
      case JobStatuses.Failed   => RunnerStatus.FAILED
    }
  }

}

class ParallelShellJobRunner(
    manager: AsyncShellManager,
    val function: CommandLineFunction) extends CommandLineJobRunner {

  var job: Option[AsyncShellJob] = None

  def start(): Unit = {
    import scala.sys.process.ProcessBuilder
    val commandline: ProcessBuilder = "/bin/sh -c " + function.commandLine
    job = Some(manager.createAsyncShellJob(processToBuild = commandline))
    updateStatus(RunnerStatus.RUNNING)
  }

  def updateJobStatus(): Boolean = {

    println("Updating job status")

    val previousStatus = this.lastStatus

    val currentStatus =
      if (job.isDefined)
        ParallelShellJobRunner.
          molmedJobStatus2QueueRunnerStatus(job.get.status())
      else
        previousStatus

    println("Found previous status to be: " + previousStatus)
    println("Found current status to be: " + currentStatus)

    if (previousStatus == currentStatus)
      false
    else {
      updateStatus(currentStatus)
      true
    }
  }

  def tryStop(): Unit = {
    if (job.isDefined)
      job.get.stop()
  }

}