package at.aau.serg.controllers

import at.aau.serg.models.GameResult
import at.aau.serg.services.GameResultService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import kotlin.math.max
import kotlin.math.min

@RestController
@RequestMapping("/leaderboard")
class LeaderboardController(
    private val gameResultService: GameResultService
) {

    @GetMapping
    fun getLeaderboard(@RequestParam(required = false) rank: Int?): List<GameResult> {
        // 1. Sortiranje: Score silazno, pa vrijeme uzlazno (manje vremena = bolje)
        val sortedList = gameResultService.getGameResults().sortedWith(
            compareByDescending<GameResult> { it.score }
                .thenBy { it.timeInSeconds } // Provjeri zove li se polje timeInSeconds ili timeSpent
        )

        // 2. Ako rank nije poslan, vrati cijelu listu
        if (rank == null) {
            return sortedList
        }

        // 3. Validacija ranka: Ako je neispravan, baci HTTP 400
        if (rank < 0 || rank >= sortedList.size) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Rank is out of bounds")
        }

        // 4. Izračunaj prozor: rank-3 do rank+3
        // max i min osiguravaju da ne izađemo izvan granica liste (0 do size-1)
        val startIndex = max(0, rank - 3)
        val endIndex = min(sortedList.size - 1, rank + 3)

        // subList je u Kotlinu (od, do) gdje je 'do' isključen, zato stavljamo +1
        return sortedList.subList(startIndex, endIndex + 1)
    }
}