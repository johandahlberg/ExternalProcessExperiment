package molmed.shell.async

import scala.sys.process.Process

  case class ProcessWithOutputs(
    process: Process,
    stdOut: StringBuilder,
    stdErr: StringBuilder)