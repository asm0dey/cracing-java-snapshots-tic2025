package com.example.demo.controller

import com.example.demo.model.WikipediaEvent
import com.example.demo.service.WikipediaEventService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/wikipedia")
class WikipediaController(private val wikipediaEventService: WikipediaEventService) {

    /**
     * Returns the latest event from the Wikipedia SSE stream.
     * @return A Mono that emits the latest WikipediaEvent
     */
    @GetMapping("/latest")
    fun getLatestEvent(): Mono<WikipediaEvent> {
        return wikipediaEventService.getLatestEvent()
    }
}