package com.example.tictactoe

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Random

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var playerScoreText: TextView
    private lateinit var aiScoreText: TextView
    private lateinit var tiesScoreText: TextView
    private lateinit var restartButton: Button
    private lateinit var undoButton: Button
    private lateinit var menuButton: Button
    private lateinit var shareButton: Button

    private val tileButtons = arrayOfNulls<Button>(9)

    private val board = CharArray(9) { ' ' }
    private var gameOver = false
    private var aiThinking = false
    private var currentPlayer: Char = 'X'

    private var mode: GameMode = GameMode.SINGLE_PLAYER
    private var difficulty: Difficulty = Difficulty.MEDIUM

    private lateinit var prefs: PrefsManager
    private lateinit var sound: SoundManager

    // Move history for undo (2-player mode)
    private val moveHistory = mutableListOf<Int>()
    private val history = mutableListOf<Pair<Int, Char>>()

    private val winningLines = arrayOf(
        intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
        intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
        intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = PrefsManager(this)
        sound = SoundManager(this).apply {
            soundEnabled = prefs.soundEnabled
            vibrationEnabled = prefs.vibrationEnabled
        }

        // Mode from intent
        mode = try {
            GameMode.valueOf(intent.getStringExtra("mode") ?: GameMode.SINGLE_PLAYER.name)
        } catch (_: Exception) { GameMode.SINGLE_PLAYER }

        difficulty = try {
            Difficulty.valueOf(prefs.difficulty)
        } catch (_: Exception) { Difficulty.MEDIUM }

        statusText = findViewById(R.id.tvStatus)
        playerScoreText = findViewById(R.id.tvPlayerScore)
        aiScoreText = findViewById(R.id.tvAiScore)
        tiesScoreText = findViewById(R.id.tvTiesScore)
        restartButton = findViewById(R.id.btnRestart)
        undoButton = findViewById(R.id.btnUndo)
        menuButton = findViewById(R.id.btnMenu)
        shareButton = findViewById(R.id.btnShare)

        for (i in 0..8) {
            tileButtons[i] = findViewById(
                resources.getIdentifier("button$i", "id", packageName)
            )
            tileButtons[i]?.setOnClickListener { onTileClick(i) }
        }

        restartButton.setOnClickListener { restartBoard() }
        undoButton.setOnClickListener { undoMove() }
        menuButton.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }
        shareButton.setOnClickListener { shareResult() }

        undoButton.visibility = if (mode == GameMode.TWO_PLAYER) Button.VISIBLE else Button.GONE

        updateScoreboard()
        setStatus(if (mode == GameMode.SINGLE_PLAYER) getString(R.string.status_your_turn)
                  else getString(R.string.status_player1_turn))

        // Entrance animation
        findViewById<android.view.View>(R.id.board).startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.slide_up)
        )
    }

    private fun onTileClick(index: Int) {
        if (gameOver || aiThinking || board[index] != ' ') return

        if (mode == GameMode.SINGLE_PLAYER) {
            onPlayerMove(index)
        } else {
            onTwoPlayerMove(index)
        }
    }

    // ============ SINGLE PLAYER ============
    private fun onPlayerMove(index: Int) {
        placeMark(index, 'X')
        sound.playClick()
        sound.vibrate(40)

        val win = findWinningLine('X')
        if (win != null) { endGame('X', win); return }
        if (isBoardFull()) { endGame(' ', null); return }

        aiThinking = true
        setStatus(getString(R.string.status_ai_thinking))
        Handler(Looper.getMainLooper()).postDelayed({ performAiMove() }, 400)
    }

    private fun performAiMove() {
        val move = bestMove()
        if (move >= 0) {
            placeMark(move, 'O')
            sound.playClick()
            sound.vibrate(40)
        }

        val win = findWinningLine('O')
        if (win != null) {
            endGame('O', win)
        } else if (isBoardFull()) {
            endGame(' ', null)
        } else {
            aiThinking = false
            setStatus(getString(R.string.status_your_turn))
        }
    }

    // ============ TWO PLAYER ============
    private fun onTwoPlayerMove(index: Int) {
        placeMark(index, currentPlayer)
        sound.playClick()
        sound.vibrate(40)

        val win = findWinningLine(currentPlayer)
        if (win != null) { endGame(currentPlayer, win); return }
        if (isBoardFull()) { endGame(' ', null); return }

        currentPlayer = if (currentPlayer == 'X') 'O' else 'X'
        setStatus(
            if (currentPlayer == 'X') getString(R.string.status_player1_turn)
            else getString(R.string.status_player2_turn)
        )
    }

    private fun placeMark(index: Int, player: Char) {
        board[index] = player
        tileButtons[index]?.text = player.toString()
        tileButtons[index]?.setTextColor(
            ContextCompat.getColor(
                this,
                if (player == 'X') R.color.x_color else R.color.o_color
            )
        )
        // Pop animation
        tileButtons[index]?.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.pop_in)
        )
        history.add(index to player)
        moveHistory.add(index)
    }

    private fun undoMove() {
        if (mode != GameMode.TWO_PLAYER) {
            Toast.makeText(this, "Undo only in 2-player mode", Toast.LENGTH_SHORT).show()
            return
        }
        if (history.isEmpty() || gameOver) return

        val (idx, _) = history.removeAt(history.size - 1)
        moveHistory.removeAt(moveHistory.size - 1)
        board[idx] = ' '
        tileButtons[idx]?.text = ""
        tileButtons[idx]?.backgroundTintList = null
        tileButtons[idx]?.setBackgroundResource(R.drawable.tile_selector)

        currentPlayer = if (currentPlayer == 'X') 'O' else 'X'
        setStatus(
            if (currentPlayer == 'X') getString(R.string.status_player1_turn)
            else getString(R.string.status_player2_turn)
        )
    }

    // ============ AI ============
    private fun bestMove(): Int {
        val empty = (0..8).filter { board[it] == ' ' }
        if (empty.isEmpty()) return -1

        // Difficulty-based randomness
        val random = Random()
        when (difficulty) {
            Difficulty.EASY -> if (random.nextInt(100) < 90) return empty.random()
            Difficulty.MEDIUM -> if (random.nextInt(100) < 40) return empty.random()
            Difficulty.HARD -> if (random.nextInt(100) < 15) return empty.random()
            Difficulty.IMPOSSIBLE -> { /* always optimal */ }
        }

        var bestScore = Int.MIN_VALUE
        var best = empty.first()
        for (i in empty) {
            board[i] = 'O'
            val score = minimax(board, 0, false)
            board[i] = ' '
            if (score > bestScore) { bestScore = score; best = i }
        }
        return best
    }

    private fun minimax(state: CharArray, depth: Int, isMax: Boolean): Int {
        val w = evaluateWinner(state)
        if (w == 'O') return 10 - depth
        if (w == 'X') return depth - 10
        if (state.none { it == ' ' }) return 0

        return if (isMax) {
            var best = Int.MIN_VALUE
            for (i in 0..8) if (state[i] == ' ') {
                state[i] = 'O'
                best = maxOf(best, minimax(state, depth + 1, false))
                state[i] = ' '
            }
            best
        } else {
            var best = Int.MAX_VALUE
            for (i in 0..8) if (state[i] == ' ') {
                state[i] = 'X'
                best = minOf(best, minimax(state, depth + 1, true))
                state[i] = ' '
            }
            best
        }
    }

    private fun evaluateWinner(state: CharArray): Char {
        for (line in winningLines) {
            val a = state[line[0]]; val b = state[line[1]]; val c = state[line[2]]
            if (a != ' ' && a == b && b == c) return a
        }
        return ' '
    }

    private fun findWinningLine(player: Char): IntArray? {
        for (line in winningLines) {
            if (board[line[0]] == player && board[line[1]] == player && board[line[2]] == player)
                return line
        }
        return null
    }

    private fun isBoardFull(): Boolean = board.none { it == ' ' }

    // ============ END GAME ============
    private fun endGame(winner: Char, winningLine: IntArray?) {
        gameOver = true
        aiThinking = false

        if (winningLine != null) {
            for (idx in winningLine) {
                tileButtons[idx]?.setBackgroundResource(R.drawable.tile_winner)
                tileButtons[idx]?.backgroundTintList = null
                // Pulse animation
                val pulse = ObjectAnimator.ofFloat(tileButtons[idx], "scaleX", 1f, 1.15f, 1f)
                pulse.duration = 500
                pulse.repeatCount = 3
                pulse.start()
                val pulse2 = ObjectAnimator.ofFloat(tileButtons[idx], "scaleY", 1f, 1.15f, 1f)
                pulse2.duration = 500
                pulse2.repeatCount = 3
                pulse2.start()
            }
        }

        when (winner) {
            'X' -> {
                prefs.playerWins++
                prefs.totalGames++
                if (mode == GameMode.SINGLE_PLAYER) {
                    setStatus(getString(R.string.status_you_win))
                } else {
                    setStatus(getString(R.string.status_player1_win))
                }
                sound.playWin()
            }
            'O' -> {
                prefs.aiWins++
                prefs.totalGames++
                if (mode == GameMode.SINGLE_PLAYER) {
                    setStatus(getString(R.string.status_ai_wins))
                } else {
                    setStatus(getString(R.string.status_player2_win))
                }
                sound.playWin()
            }
            else -> {
                prefs.ties++
                prefs.totalGames++
                setStatus(getString(R.string.status_draw))
                sound.playDraw()
            }
        }
        sound.vibrate(200)
        updateScoreboard()
    }

    // ============ UI ============
    private fun restartBoard() {
        for (i in 0..8) {
            board[i] = ' '
            tileButtons[i]?.text = ""
            tileButtons[i]?.setBackgroundResource(R.drawable.tile_selector)
            tileButtons[i]?.backgroundTintList = null
        }
        gameOver = false
        aiThinking = false
        currentPlayer = 'X'
        history.clear()
        moveHistory.clear()

        setStatus(
            if (mode == GameMode.SINGLE_PLAYER) getString(R.string.status_your_turn)
            else getString(R.string.status_player1_turn)
        )
    }

    private fun updateScoreboard() {
        playerScoreText.text = getString(R.string.score_player, prefs.playerWins)
        aiScoreText.text = getString(R.string.score_ai, prefs.aiWins)
        tiesScoreText.text = getString(R.string.score_ties, prefs.ties)
    }

    private fun setStatus(text: String) {
        statusText.text = text
        statusText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pop_in))
    }

    private fun shareResult() {
        val text = "I played Tic Tac Toe Pro!\n" +
                "Wins: ${prefs.playerWins} | Losses: ${prefs.aiWins} | Draws: ${prefs.ties}\n" +
                "Can you beat me?"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    override fun onDestroy() {
        super.onDestroy()
        sound.release()
    }
}