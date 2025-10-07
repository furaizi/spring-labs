package org.example.lab1

import mu.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
@Order(1)
class First : CommandLineRunner {

    override fun run(vararg args: String?) {
        log.info { "First" }
    }
}