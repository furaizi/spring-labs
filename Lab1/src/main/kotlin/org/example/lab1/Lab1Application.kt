package org.example.lab1

import mu.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.annotation.Order

private val log = KotlinLogging.logger {}

@SpringBootApplication
@Order(2)
class Lab1Application : CommandLineRunner {
    override fun run(vararg args: String?) {
        log.info("Hello from Spring Boot!")
    }
}

fun main(args: Array<String>) {
    log.info { "Begin of main" }
    runApplication<Lab1Application>(*args)
    log.info { "End of main" }
}
