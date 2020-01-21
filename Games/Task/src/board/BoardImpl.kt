package board

import board.Direction.*
import java.lang.IllegalArgumentException
import kotlin.math.max
import kotlin.math.min

open class DefaultSquareBoard(override val width: Int) : SquareBoard {

    protected val cells: List<Cell>

    init {
        if (width <= 0) {
            throw IllegalArgumentException()
        }

        this.cells = IntRange(1, width).flatMap { i -> IntRange(1, width).map { j -> Cell(i, j) } }
    }

    protected fun to1DIndex(row: Int, col: Int): Int {
        require(row >= 1) { "row must be 1-based index, was: $row" }
        require(col >= 1) { "col must be 1-based index, was: $col" }
        return (row - 1) * width + (col - 1)
    }

    override fun getCellOrNull(i: Int, j: Int): Cell? {
        if (i > width || j > width) {
            return null
        }

        return cells[to1DIndex(i, j)]
    }

    override fun getCell(i: Int, j: Int): Cell {
        return getCellOrNull(i, j) ?: throw IllegalArgumentException()
    }

    override fun getAllCells(): Collection<Cell> {
        return cells
    }

    override fun getRow(i: Int, jRange: IntProgression): List<Cell> {
        return getRange(i, jRange) { row, col -> to1DIndex(row, col)}
    }

    override fun getColumn(iRange: IntProgression, j: Int): List<Cell> {
        return getRange(j, iRange) { col, row -> to1DIndex(row, col)}
    }

    private fun getRange(fixedCoord: Int, range: IntProgression, indexerFunction: (Int, Int) -> Int) : List<Cell> {
        // Get the restricted boundaries
        val (start, end) = restrictToBoardBoundaries(range)

        return IntProgression.fromClosedRange(start, end, range.step).map { variableCoord ->
            val index = indexerFunction(fixedCoord, variableCoord)
            cells[index]
        }
    }

    private fun restrictToBoardBoundaries(range: IntProgression) : Pair<Int, Int> {
        return if (range.step > 0) {
            // For positive steps
            Pair(max(range.first, 1), min(range.last, width))
        } else {
            // For negative steps
            Pair(min(range.first, width), max(range.last, 1))
        }
    }

    override fun Cell.getNeighbour(direction: Direction): Cell? {
        return when (direction) {
            UP -> if (this.i > 1) cells[to1DIndex(this.i - 1, this.j)] else null
            DOWN -> if (this.i < width) cells[to1DIndex(this.i + 1, j)] else null
            LEFT -> if (this.j > 1) cells[to1DIndex(this.i, this.j - 1)] else null
            RIGHT -> if (this.j < width) cells[to1DIndex(this.i, this.j + 1)] else null
        }
    }
}

class DefaultGameBoard<T>(width: Int) : GameBoard<T>, DefaultSquareBoard(width) {

    private val values: HashMap<Cell, T?> = HashMap()

    init {
        this.cells.forEach { cell -> values[cell] = null }
    }

    override fun get(cell: Cell): T? {
        return values[cell]
    }

    override fun set(cell: Cell, value: T?) {
        values[cell] = value
    }

    override fun filter(predicate: (T?) -> Boolean): Collection<Cell> {
        return values.entries.filter { (cell, value) -> predicate(value) }.map { (cell, value) -> cell }
    }

    override fun find(predicate: (T?) -> Boolean): Cell? {
        return values.entries.find { (cell, value) -> predicate(value) }?.key
    }

    override fun any(predicate: (T?) -> Boolean): Boolean {
        return this.cells.any { cell -> predicate(get(cell)) }
    }

    override fun all(predicate: (T?) -> Boolean): Boolean {
        return this.cells.all { cell -> predicate(get(cell)) }
    }
}

fun createSquareBoard(width: Int): SquareBoard = DefaultSquareBoard(width)
fun <T> createGameBoard(width: Int): GameBoard<T> = DefaultGameBoard<T>(width)

