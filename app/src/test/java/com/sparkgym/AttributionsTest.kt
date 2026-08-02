package com.sparkgym

import com.sparkgym.domain.model.Attributions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Attribution is a licence obligation, not a courtesy.
 *
 * The muscle model is CC BY-SA 4.0 and the online food data is ODbL; both
 * require credit the user can actually see. Dropping an entry — or shipping one
 * with a blank licence or a dead-looking link — is a compliance failure, so the
 * list is pinned.
 */
class AttributionsTest {

    @Test
    fun `the share-alike sources are both credited`() {
        val names = Attributions.all.map { it.name }
        assertTrue("Z-Anatomy must be credited (CC BY-SA 4.0)", names.any { it.contains("Z-Anatomy") })
        assertTrue("Open Food Facts must be credited (ODbL)", names.any { it.contains("Open Food Facts") })
    }

    @Test
    fun `share-alike terms are flagged as such`() {
        val flagged = Attributions.shareAlike.map { it.name }
        assertTrue("Z-Anatomy is share-alike", flagged.any { it.contains("Z-Anatomy") })
        assertTrue("Open Food Facts is share-alike", flagged.any { it.contains("Open Food Facts") })
    }

    @Test
    fun `every entry names a licence, a link and what it is used for`() {
        Attributions.all.forEach {
            assertTrue("${it.name} has no licence", it.licence.isNotBlank())
            assertTrue("${it.name} has no link", it.url.startsWith("https://"))
            assertTrue("${it.name} does not say what it is for", it.use.length > 20)
        }
    }

    @Test
    fun `nothing is listed twice`() {
        val names = Attributions.all.map { it.name }
        assertEquals(names.size, names.toSet().size)
    }
}
