package at.aau.serg.services

import at.aau.serg.models.GameResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameResultServiceTests {

    private lateinit var service: GameResultService

    @BeforeEach
    fun setup() {
        service = GameResultService()
    }

    @Test
    fun test_getGameResults_emptyList() {
        val result = service.getGameResults()
        assertEquals(emptyList<GameResult>(), result)
    }

    @Test
    fun test_addGameResult_getGameResults_containsSingleElement() {
        val gameResult = GameResult(1, "player1", 17, 15.3)

        service.addGameResult(gameResult)
        val res = service.getGameResults()

        assertEquals(1, res.size)
        // Hinweis: Die ID wird im Service überschrieben, daher prüfen wir die Inhalte
        assertEquals("player1", res[0].playerName)
        assertEquals(1, res[0].id)
    }

    @Test
    fun test_getGameResultById_existingId_returnsObject() {
        val gameResult = GameResult(0, "player1", 17, 15.3)
        service.addGameResult(gameResult)

        val res = service.getGameResult(1) // ID wird automatisch auf 1 gesetzt

        assertEquals("player1", res?.playerName)
    }

    @Test
    fun test_getGameResultById_nonexistentId_returnsNull() {
        service.addGameResult(GameResult(0, "p1", 10, 10.0))
        val res = service.getGameResult(99)
        assertNull(res)
    }

    @Test
    fun test_addGameResult_multipleEntries_correctId() {
        val gameResult1 = GameResult(0, "player1", 17, 15.3)
        val gameResult2 = GameResult(0, "player2", 25, 16.0)

        service.addGameResult(gameResult1)
        service.addGameResult(gameResult2)

        val res = service.getGameResults()

        assertEquals(2, res.size)
        assertEquals(1, res[0].id)
        assertEquals(2, res[1].id)
    }

    // NEU: Test für die Lösch-Funktion (wichtig für 100% Coverage)
    @Test
    fun test_deleteGameResult_existingId_removesElement() {
        service.addGameResult(GameResult(0, "toDelete", 10, 10.0))
        assertEquals(1, service.getGameResults().size)

        val deleted = service.deleteGameResult(1)

        assertTrue(deleted)
        assertEquals(0, service.getGameResults().size)
    }

    @Test
    fun test_deleteGameResult_nonexistentId_returnsFalse() {
        val deleted = service.deleteGameResult(999)
        assertEquals(false, deleted)
    }
}