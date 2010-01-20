package org.test.upload
package config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.multipart.commons.CommonsMultipartResolver

import org.slf4j.LoggerFactory
import org.joda.time.Duration

@Configuration
class ScalaAppConfig {

	val logger = LoggerFactory.getLogger(classOf[ScalaAppConfig])
	val defaultSleepTime = 10
	
	@Value("#{configuration.wait}")
	var sleepTime = ""
	
	/*
	 * configure the file handler
	 */
	@Bean 
	def fileProcessor = {
		//try to parse the configurated waited time, default to defaultSleepTime if parsing goes wrong
		val wait = try {
			sleepTime.toLong
		} catch {
			case e:NumberFormatException => defaultSleepTime
		}
		new WaitFileProcessor(new Duration(wait*1000))
	}

	/*
	 * configure the common-upload file handler
	 */
	@Bean 
	def multipartResolver() = {
		val c = new CommonsMultipartResolver()
		c.setMaxUploadSize(10000000)
		c
	}
	
	/*
	 * Configure the endpoint
	 */
	@Bean
	def endpoint() = new UploadEndpoint(fileProcessor)
}
