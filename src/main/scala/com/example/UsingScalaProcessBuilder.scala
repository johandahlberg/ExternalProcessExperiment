package com.example

import scala.util.Random
import scala.sys.process._
import molmed.shell.async.AsyncShellManager
import molmed.shell.async.JobStatuses._

object UsingScalaProcessBuilder extends App {

  var echoNbr = 0

  def randomSleepInterval = Random.nextInt(10)
  def failure = if(Random.nextDouble() > 0.5) "echo 1" else "echo 0";
  
  def sleepCommand: ProcessBuilder = "sleep " + randomSleepInterval

  def echoCommand: ProcessBuilder = {
    echoNbr += 1
    "echo jobNumber: " + echoNbr
  }
  def cmdLine: ProcessBuilder = sleepCommand #&& echoCommand #&& failure

  val jobManager = AsyncShellManager()

  val firstProcess = jobManager.createAsyncShellJob(cmdLine)

  jobManager.createAsyncShellJob(cmdLine)
  jobManager.createAsyncShellJob(cmdLine)
  jobManager.createAsyncShellJob(cmdLine)
  jobManager.createAsyncShellJob(cmdLine)  
  jobManager.createAsyncShellJob(cmdLine)

  while (jobManager.queryStatus(firstProcess) != Finished)
    println(jobManager.queryStatus(firstProcess))
 

}