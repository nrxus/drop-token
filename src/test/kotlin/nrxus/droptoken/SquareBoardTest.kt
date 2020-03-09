package nrxus.droptoken

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SquareBoardTest {
    @Test
    fun `horizontal win`() {
        val squareBoard = SquareBoard(4, mutableListOf(
                0, 0, 0, 0,
                -1, 1, 1, -1,
                1
        ))
        assertThat(squareBoard.state()).isEqualTo(SquareBoard.State.Done(0))
    }

    @Test
    fun `vertical win`() {
        val squareBoard = SquareBoard(4, mutableListOf(
                0, 1, 0, 0,
                0, 1, 1, 0,
                0, 1, 0, 1,
                1, 1, -1, -1
        ))
        assertThat(squareBoard.state()).isEqualTo(SquareBoard.State.Done(1))
    }

    @Test
    fun `diagonal right up win`() {
        val squareBoard = SquareBoard(4, mutableListOf(
                1, 1, 0, 0,
                0, 1, 1, 0,
                0, 0, 1, 1,
                1, 1, -1, 1
        ))
        assertThat(squareBoard.state()).isEqualTo(SquareBoard.State.Done(1))
    }

    @Test
    fun `diagonal left up win`() {
        val squareBoard = SquareBoard(4, mutableListOf(
                1, 1, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 1,
                0, 1, -1, 0
        ))
        assertThat(squareBoard.state()).isEqualTo(SquareBoard.State.Done(0))
    }

    @Test
    fun `tied is done without a winner`() {
        val squareBoard = SquareBoard(4, mutableListOf(
                1, 1, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 1,
                1, 1, 0, 0
        ))
        assertThat(squareBoard.state()).isEqualTo(SquareBoard.State.Done(null))
    }

    @Test
    fun `in progress`() {
        val squareBoard = SquareBoard(4, mutableListOf(
                1, 1, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 1,
                1, -1, 0, 0
        ))
        assertThat(squareBoard.state()).isEqualTo(SquareBoard.State.InProgress)
    }
}