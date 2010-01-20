package org.test.upload


/**
 * Our file processor interface. 
 * Only one (trivial) method
 */
trait FileProcessor {

	def apply(path:String) : Unit

}

/* *********************************************************** */
// IMPLEMENTATION 
/* *********************************************************** */

import java.io.{File,IOException}
import org.joda.time.Duration
import org.slf4j.LoggerFactory

/*
 * Trivial implementation of the processor interface
 * that just waits the configured time before deleting
 * the file
 */
class WaitFileProcessor(sleepTime:Duration) extends FileProcessor {
	
	val logger = LoggerFactory.getLogger(classOf[WaitFileProcessor])
	
	override def apply(path:String) : Unit = {
		
		logger.info("Process new file: '{}'",path)
		try {
			val file = new File(path)
			Thread.sleep(sleepTime.getMillis)
			logger.info("End process file: '{}'",path)
		} catch {
			case e:IOException => 
				logger.error("Error when processing file '{}'. Reported exception: {}", path, e.getMessage)
		}
		
		//clean-up
		try {
			if(! new File(path).delete) {
				logger.error("Error when trying to delete file '{}'. You will have to delete it by hand.",path)
			}
		} catch {
			case io:IOException => //ignored
		}	
	}
	
}
