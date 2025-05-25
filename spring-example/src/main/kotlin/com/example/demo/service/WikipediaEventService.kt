package com.example.demo.service

import com.example.demo.model.WikipediaEvent
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.LocalDateTime

@Service
class WikipediaEventService {
    private val logger = LoggerFactory.getLogger(WikipediaEventService::class.java)
    private val webClient = WebClient.builder().build()

    // Current event that gets updated with each SSE message
    private var currentEvent: WikipediaEvent = WikipediaEvent(
        id = "initial",
        type = "initial",
        timestamp = LocalDateTime.now(),
        content = "Waiting for first event..."
    )

    @PostConstruct
    fun connectToWikipediaStream() {
        val wikipediaUrl = "https://stream.wikimedia.org/v2/stream/recentchange"

        webClient.get()
            .uri(wikipediaUrl)
            .retrieve()
            .bodyToFlux(String::class.java)
            .publishOn(Schedulers.boundedElastic())
            .doOnNext { eventData ->
                logger.debug("Received event: {}", eventData)
                // Parse the event data and update the current event
                if (eventData.isNotBlank() && !eventData.startsWith(":")) {
                    try {
                        // Simple parsing for demonstration purposes
                        // In a real application, you would parse the JSON data properly
                        val newEvent = WikipediaEvent(
                            id = extractField(eventData, "id"),
                            type = extractField(eventData, "type"),
                            title = extractField(eventData, "title"),
                            comment = extractField(eventData, "comment"),
                            user = extractField(eventData, "user"),
                            timestamp = LocalDateTime.now(),
                            content = eventData
                        )

                        // Update the current event
                        currentEvent = newEvent
                    } catch (e: Exception) {
                        logger.error("Error processing event: {}", e.message, e)
                    }
                }
            }
            .subscribe()
    }

    /**
     * Simple helper method to extract field values from the event data.
     * This is a very basic implementation for demonstration purposes.
     */
    private fun extractField(eventData: String, fieldName: String): String? {
        val pattern = "\"$fieldName\":\\s*\"([^\"]+)\"".toRegex()
        val matchResult = pattern.find(eventData)
        return matchResult?.groupValues?.getOrNull(1)
    }

    /**
     * Returns the latest event as a Mono.
     */
    fun getLatestEvent(): Mono<WikipediaEvent> {
        // If we haven't received any events yet, return the current event
        return Mono.just(currentEvent)
    }
}