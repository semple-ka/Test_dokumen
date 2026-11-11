package com.example.layoutkeeper.model

enum class TestStatus {
    PENDING,
    RUNNING,
    PASSED,
    FAILED
}

data class AcceptanceTestCase(
    val id: String,
    val code: String, // e.g. "Test A"
    val title: String,
    val description: String,
    val targetFormat: String,
    var status: TestStatus = TestStatus.PENDING,
    var executionLog: String = "",
    var executionDurationMs: Long = 0L
)
