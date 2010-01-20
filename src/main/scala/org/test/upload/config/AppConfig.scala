package org.test.upload
package config

import org.springframework.context.annotation.{Bean,Configuration,ImportResource}
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.PropertiesFactoryBean
import org.springframework.core.io.ClassPathResource
import org.springframework.web.multipart.commons.CommonsMultipartResolver

import org.slf4j.LoggerFactory
import org.joda.time.Duration

@Configuration
class AppConfig {

	val logger = LoggerFactory.getLogger(classOf[AppConfig])
	val defaultSleepTime = 10
	
	//get the value from configuration.properties
	@Value("#{configuration.wait}")
	var sleepTime = ""

	//load configuration.properties file
	@Bean
	def configuration = {
		val factory = new PropertiesFactoryBean
		factory.setLocation(new ClassPathResource("configuration.properties"))
		factory
	}

	// configure the file handler //
	@Bean 
	def fileProcessor = {
		//try to parse the configured waiting time, default to defaultSleepTime if parsing goes wrong
		val wait = try {
			sleepTime.toFloat
		} catch {
			case e:NumberFormatException => 
				logger.error("Cannot convert configured value '{}' to an amount of second. Use default value: {}", sleepTime, defaultSleepTime)
				defaultSleepTime
		}
		new WaitFileProcessor(new Duration((wait*1000).toLong))
	}

	// configure the common-upload file handler //
	@Bean 
	def multipartResolver = {
		val c = new CommonsMultipartResolver()
		c.setMaxUploadSize(10000000)
		c
	}
	
	// Configure the endpoint //
	@Bean
	def endpoint = new UploadEndpoint(fileProcessor)
}
