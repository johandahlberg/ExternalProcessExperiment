package com.example

import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecuteResultHandler
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.PumpStreamHandler
import java.io.FileOutputStream
import org.apache.commons.exec.ExecuteWatchdog
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scala.util.Random
import java.io.File
import java.io.FileInputStream
import java.io.FileDescriptor

object UsingApacheExec {

  def fileInputStreamToString(fis: FileInputStream): String = {
    scala.io.Source.fromInputStream(fis).getLines.mkString("\n")
  }

  def runProcess(i: Int): Future[(Int, Int, FileInputStream)] = {
    future {

      val streamHandler = new PumpStreamHandler()
      val stdOutStream = new FileInputStream(FileDescriptor.out)
      val stdErrStream = new FileInputStream(FileDescriptor.err)
      streamHandler.setProcessOutputStream(stdOutStream)
      streamHandler.setProcessErrorStream(stdErrStream)

      val watchDog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT)

      val randomWait = Random.nextInt(6)

      val resultHandler = new DefaultExecuteResultHandler()

      val executor = new DefaultExecutor()
      executor.setWatchdog(watchDog)
      executor.setStreamHandler(streamHandler)

      val commandLine = new CommandLine("/bin/sh")
      commandLine.addArgument("-c")
      commandLine.addArgument("sleep " + randomWait + "; echo some nice test string" + i, false)

      executor.execute(commandLine, resultHandler)
      resultHandler.waitFor()

      //println("This is what I found on stdout: " + fileInputStreamToString(stdOutStream))
      //println("This is what I found on stderr: " + fileInputStreamToString(stdErrStream))

      val exitValue = resultHandler.getExitValue()
      //println(exitValue + " for process: " + i)
      (i, exitValue, stdOutStream)
    }
  }

  def main(args: Array[String]): Unit = {

    val x = 1 to 10
    val result = x.map(f => runProcess(f))

    println("I'm not blocking here!")

    while (!result.forall(p => p.isCompleted))
      result.filter(p => p.isCompleted).foreach(f => {
        val values = f.value.get.get
        //println(values + " finished.")
        //println(fileInputStreamToString(values._3))
      })

    println("Everything finally finished!")

  }
}
