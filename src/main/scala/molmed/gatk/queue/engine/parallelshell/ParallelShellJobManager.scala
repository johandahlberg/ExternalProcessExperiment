package molmed.gatk.queue.engine.parallelshell

import org.broadinstitute.gatk.queue.engine.CommandLineJobManager
import org.broadinstitute.gatk.queue.function.CommandLineFunction
import molmed.shell.async.AsyncShellManager

class ParallelShellJobManager extends CommandLineJobManager[ParallelShellJobRunner] {

  def runnerType = classOf[ParallelShellJobRunner]

  val manager = AsyncShellManager()

  override def exit() = manager.exit()

  def create(function: CommandLineFunction): ParallelShellJobRunner = {
    val jobRunner = ParallelShellJobRunner(manager, function)
    jobRunner.start()
    jobRunner
  }

  override def updateStatus(
    runners: Set[ParallelShellJobRunner]): Set[ParallelShellJobRunner] =
    runners.filter(runner => runner.updateJobStatus())

  override def tryStop(runners: Set[ParallelShellJobRunner]): Unit = {
    runners.foreach { runner => runner.tryStop() }
  }

}