package com.example.demo.model

import java.time.LocalDateTime

/**
 * Represents an event from the Wikipedia SSE API.
 */
data class WikipediaEvent(
    val id: String? = null,
    val type: String? = null,
    val title: String? = null,
    val comment: String? = null,
    val user: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val content: String? = null
)