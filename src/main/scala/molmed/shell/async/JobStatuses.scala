package molmed.shell.async

object JobStatuses {
  sealed trait JobStatus  
  case object Running extends JobStatus
  case object Failed extends JobStatus
  case object Finished extends JobStatus
}