package at.aau.serg.controllers

import at.aau.serg.models.GameResult
import at.aau.serg.services.GameResultService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals
import org.mockito.Mockito.`when` as whenever

class LeaderboardControllerTests {

    private lateinit var mockedService: GameResultService
    private lateinit var controller: LeaderboardController

    @BeforeEach
    fun setup() {
        mockedService = mock(GameResultService::class.java)
        controller = LeaderboardController(mockedService)
    }

    @Test
    fun `test getLeaderboard sorting score descending and time ascending`() {
        // Gleicher Score (20), aber 'second' ist schneller (10.0s) als 'first' (20.0s)
        val first = GameResult(1, "first", 20, 20.0)
        val second = GameResult(2, "second", 20, 10.0)
        val third = GameResult(3, "third", 10, 5.0)

        whenever(mockedService.getGameResults()).thenReturn(listOf(first, second, third))

        val res = controller.getLeaderboard(null)

        assertEquals(3, res.size)
        assertEquals(second, res[0]) // Schneller bei gleichem Score
        assertEquals(first, res[1])
        assertEquals(third, res[2])  // Niedrigster Score
    }

    @Test
    fun `test getLeaderboard with rank returns window`() {
        // Wir erstellen 10 Spieler (Score 100 bis 10)
        val results = (0..9).map { i ->
            GameResult(i.toLong(), "Player $i", 100 - i, 10.0)
        }
        whenever(mockedService.getGameResults()).thenReturn(results)

        // Wir fragen nach Rank 5 (Player 5). Erwartet: Rank 2, 3, 4, [5], 6, 7, 8
        val res = controller.getLeaderboard(5)

        assertEquals(7, res.size)
        assertEquals("Player 2", res.first().playerName)
        assertEquals("Player 8", res.last().playerName)
    }

    @Test
    fun `test getLeaderboard rank at start of list`() {
        val results = (0..9).map { i -> GameResult(i.toLong(), "P$i", 100 - i, 10.0) }
        whenever(mockedService.getGameResults()).thenReturn(results)

        // Rank 0: Sollte 0, 1, 2, 3 zurückgeben (da davor nichts ist)
        val res = controller.getLeaderboard(0)

        assertEquals(4, res.size)
        assertEquals("P0", res[0].playerName)
        assertEquals("P3", res[3].playerName)
    }

    @Test
    fun `test getLeaderboard rank at end of list`() {
        val results = (0..9).map { i -> GameResult(i.toLong(), "P$i", 100 - i, 10.0) }
        whenever(mockedService.getGameResults()).thenReturn(results)

        // Rank 9 (Letzter): Sollte 6, 7, 8, 9 zurückgeben
        val res = controller.getLeaderboard(9)

        assertEquals(4, res.size)
        assertEquals("P6", res[0].playerName)
        assertEquals("P9", res.last().playerName)
    }

    @Test
    fun `test getLeaderboard invalid rank throws 400`() {
        val results = listOf(GameResult(1, "P1", 10, 10.0))
        whenever(mockedService.getGameResults()).thenReturn(results)

        // Rank 5 bei nur einem Spieler muss Fehler werfen
        assertThrows<ResponseStatusException> {
            controller.getLeaderboard(5)
        }

        // Negativer Rank muss Fehler werfen
        assertThrows<ResponseStatusException> {
            controller.getLeaderboard(-1)
        }
    }
}