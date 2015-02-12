package molmed.shell.async

import scala.sys.process._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import molmed.shell.async.JobStatuses._
import scala.util.{ Success, Failure }

object AsyncShellJob {

  val illegalStateException =
    new IllegalStateException(
      "Trying to call stop on a processes that hasn't been started yet!")

  def apply(commandLine: ProcessBuilder): AsyncShellJob = {
    new AsyncShellJob(commandLine)
  }

}

class AsyncShellJob(commandLine: ProcessBuilder) {

  private var process: Option[Process] = None

  def start(): AsyncShellJob = {

    // TODO Possibly this should be redirected to files instead.
    // Right now I'm doing nothing with these
    val stdOutBuilder = new StringBuilder()
    val stdErrBuilder = new StringBuilder()

    val processLogger = ProcessLogger(
      line => stdOutBuilder.append(line),
      line => stdErrBuilder.append(line))

    //val process = processToBuild.run(processLogger)
    process = Some(commandLine.run())
    this
  }

  def stop(): Unit = process.getOrElse {
    throw AsyncShellJob.illegalStateException
  }.destroy()

  def status(): JobStatus = {

    def exitStatus2JobStatus(exitStatus: Int): JobStatus =
      exitStatus match {
        case 0 => Finished
        case _ => {
          println("Found a failiure!")
          Failed
        }
      }

    val exitValue: Future[Int] =
      future {
        process.
          getOrElse(throw AsyncShellJob.illegalStateException).
          exitValue()
      }

    exitValue.onComplete {      
      case Success(value) => exitStatus2JobStatus(exitValue.value.get.get)
      case Failure(t)     => Failed
    }

    val status =
      if (exitValue.isCompleted) {
        exitStatus2JobStatus(exitValue.value.get.get)
      } else
        Running

    status
  }

}