package com.sudoku.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents a Sudoku board for API requests and responses.
 */
public class SudokuBoard {

    @NotNull(message = "Board cannot be null")
    @Size(min = 9, max = 9, message = "Board must be a 9x9 grid")
    private int[][] board;

    // Default constructor for JSON deserialization
    public SudokuBoard() {
    }

    public SudokuBoard(int[][] board) {
        this.board = board;
    }

    public int[][] getBoard() {
        return board;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }

    /**
     * Validates that the board contains only valid Sudoku values (0-9).
     * 0 represents an empty cell.
     *
     * @return true if the board is valid, false otherwise
     */
    public boolean isValid() {
        if (board == null || board.length != 9) {
            return false;
        }

        for (int[] row : board) {
            if (row == null || row.length != 9) {
                return false;
            }

            for (int cell : row) {
                if (cell < 0 || cell > 9) {
                    return false;
                }
            }
        }

        return true;
    }
}