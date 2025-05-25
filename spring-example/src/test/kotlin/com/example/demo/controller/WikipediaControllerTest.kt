package com.example.demo.controller

import com.example.demo.model.WikipediaEvent
import com.example.demo.service.WikipediaEventService
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@WebFluxTest(WikipediaController::class)
@Import(WikipediaControllerTest.TestConfig::class)
class WikipediaControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var wikipediaEventService: WikipediaEventService

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun wikipediaEventService(): WikipediaEventService = Mockito.mock(WikipediaEventService::class.java)
    }

    @Test
    fun `should return latest event`() {
        // Given
        val testEvent = WikipediaEvent(
            id = "test-id",
            type = "test-type",
            title = "Test Title",
            comment = "Test Comment",
            user = "Test User",
            timestamp = LocalDateTime.now(),
            content = "Test Content"
        )

        Mockito.`when`(wikipediaEventService.getLatestEvent()).thenReturn(Mono.just(testEvent))

        // When & Then
        webTestClient.get()
            .uri("/api/wikipedia/latest")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo("test-id")
            .jsonPath("$.type").isEqualTo("test-type")
            .jsonPath("$.title").isEqualTo("Test Title")
            .jsonPath("$.comment").isEqualTo("Test Comment")
            .jsonPath("$.user").isEqualTo("Test User")
            .jsonPath("$.content").isEqualTo("Test Content")
    }
}
