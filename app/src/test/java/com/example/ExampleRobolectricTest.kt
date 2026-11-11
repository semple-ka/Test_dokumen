package com.example

import android.content.Context
import android.graphics.RectF
import androidx.test.core.app.ApplicationProvider
import com.example.layoutkeeper.engine.layout.LayoutGuardian
import com.example.layoutkeeper.model.ElementContainerType
import com.example.layoutkeeper.model.OverflowStrategy
import com.example.layoutkeeper.model.TextElement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LayoutKeeper", appName)
    }

    @Test
    fun `layout guardian detects fit without overflow`() {
        val guardian = LayoutGuardian()
        val element = TextElement(
            id = "test_1",
            pageIndex = 0,
            originalText = "The application provides a comprehensive document management system.",
            currentText = "The application provides a comprehensive document management system.",
            bounds = RectF(0.1f, 0.1f, 0.8f, 0.15f),
            fontSizePt = 12f
        )
        val fit = guardian.evaluateFit(
            element = element,
            candidateText = "The application provides a professional document editing system.",
            pageElements = listOf(element)
        )
        assertNotNull(fit)
        assertEquals(OverflowStrategy.PRESERVE_SIZE_WARN, fit.recommendedStrategy)
    }

    @Test
    fun `layout guardian detects overflow on long replacement`() {
        val guardian = LayoutGuardian()
        val element = TextElement(
            id = "test_2",
            pageIndex = 0,
            originalText = "Short Title",
            currentText = "Short Title",
            bounds = RectF(0.1f, 0.1f, 0.4f, 0.15f),
            fontSizePt = 14f
        )
        val candidate = "EXTREMELY LONG REPLACEMENT TITLE THAT CLEARLY EXCEEDS ORIGINAL BOUNDING BOX AREA SUBSTANTIALLY"
        val fit = guardian.evaluateFit(
            element = element,
            candidateText = candidate,
            pageElements = listOf(element)
        )
        assertTrue("Should detect overflow on much longer text", fit.hasOverflow)
        assertTrue("Overflow delta should be positive", fit.overflowDeltaPx > 0f)
    }
}
