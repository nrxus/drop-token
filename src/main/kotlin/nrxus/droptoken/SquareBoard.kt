package nrxus.droptoken

/// The numbers in tokens represent a player id; where the id > 0
/// Any "holes" in the board game are denoted by -1
class SquareBoard(private val length: Int, val tokens: MutableList<Int>) {
    sealed class State {
        object InProgress : State()
        data class Done(val winner: Int?) : State()
    }

    fun drop(column: Int, player: Int): Boolean {
        // out of column bounds
        if (column < 0 || column >= length) {
            return false
        }

        // find first empty position on this column
        var position = column
        while (tokens.getOrElse(position) { -1 } != -1) {
            position += length
        }

        // out of row bounds
        if (position >= length * length) {
            // out of row bounds
            return false
        }

        // fill with -1 until the position if necessary
        tokens.addAll((0..position - tokens.size).map { -1 })

        tokens[position] = player

        return true
    }

    fun state(): State {
        when (val winningRow = winningRow()) {
            null -> Unit
            else -> return State.Done(tokens[winningRow * length])
        }

        when (val winningColumn = winningColumn()) {
            null -> Unit
            else -> return State.Done(tokens[winningColumn])
        }

        when (val rightUpWinner = rightUpWinner()) {
            null -> Unit
            else -> return State.Done(rightUpWinner)
        }

        when (val leftUpWinner = leftUpWinner()) {
            null -> Unit
            else -> return State.Done(leftUpWinner)
        }

        return if (tokens.size != length * length || tokens.any { it == -1 }) {
            State.InProgress
        } else {
            State.Done(null)
        }
    }

    private fun leftUpWinner(): Int? = (0 until length)
            .mapNotNull { tokens.getOrNull(it * length + (length - it - 1)) }
            .let {
                if (hasWinner(it)) {
                    it[0]
                } else {
                    null
                }
            }

    private fun rightUpWinner(): Int? = (0 until length)
            .mapNotNull { tokens.getOrNull(it * length + it) }
            .let {
                if (hasWinner(it)) {
                    it[0]
                } else {
                    null
                }
            }

    private fun winningColumn(): Int? = (0 until length).find { i ->
        val column = (0 until length)
                .mapNotNull { j -> tokens.getOrNull(i + length * j) }
        hasWinner(column)
    }

    private fun winningRow(): Int? = (0 until length).find { i ->
        val row = (0 until length)
                .map { j -> tokens.getOrElse(i * length + j) { -1 } }
                .filterNot { it == -1 }
        hasWinner(row)
    }

    private fun hasWinner(cells: List<Int>): Boolean = if (cells.size < length) {
        false
    } else {
        cells.distinct().count() == 1
    }
}