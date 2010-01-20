package org.test.upload

import java.io.{IOException, File, FileInputStream, InputStream, FileOutputStream}
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest
import scala.collection.JavaConversions._


/**
 * Define a message which will be sent 
 * to the asynchronous file processor
 */
case class NewFile(path:String)

/**
 * Spring 3 endpoint
 */
@Controller
class UploadEndpoint(fileProcessor:FileProcessor) {
	val logger = LoggerFactory.getLogger(classOf[UploadEndpoint])
	//start the file processor actor
	FileProcessorActor.start
	
	/** ******************************************************************
	 * The actual endpoint. It's here that  upload requests arrive
	 **********************************************************************/
	@RequestMapping( 
		value = Array("/upload"), 
		method = Array(RequestMethod.POST)
	)
	def onSubmit(request:DefaultMultipartHttpServletRequest) = {
		for(f <- request.getFileMap.values) {
			logger.info("Receive new upload: '{}'", f.getName)
			try {
				val reportFile = copyFileToTempDir(f)
				//send the file path to the file processor actor
				FileProcessorActor ! NewFile(reportFile.getAbsolutePath)
				logger.info("Upload '{}' processed by endpoint", f.getName)
			} catch {
				case e:IOException => logger.error("Error when pre-processing file '{}'. Reported exception is: {}",f.getName,e.getMessage)
			}
		}
		// let's do nothing 
		"redirect:/"
	}

	/** ******************************************************************
	 * An asynchronous actor that while actually process the file
	  ****************************************************************** */
	import scala.actors.Actor
	import Actor._
	private object FileProcessorActor extends Actor {
		override def act = {
			loop {
				react {
					case NewFile(path) =>fileProcessor(path)
				}
			}
		}
	}

	/*
	 * Just copy the multipart file to a temp dir
	 */
	private def copyFileToTempDir(src:MultipartFile) : File = {
		val in = src.getInputStream
		val fout = File.createTempFile(src.getName+"_"+System.currentTimeMillis, ".tmp")
		val out = new FileOutputStream(fout)
		// Transfer bytes from in to out 
		val buf = new Array[Byte](1024)
		var len = 0
		while ({len = in.read(buf) ; len} > 0) { out.write(buf, 0, len) } 
		in.close
		out.close
		fout
	}
}