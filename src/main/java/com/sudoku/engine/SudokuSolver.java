package com.sudoku.engine;

import org.springframework.stereotype.Service;

/**
 * Service responsible for solving Sudoku puzzles.
 */
@Service
public class SudokuSolver {

    private static final int GRID_SIZE = 9;
    private static final int EMPTY_CELL = 0;

    /**
     * Solves the given Sudoku puzzle.
     *
     * @param board the unsolved Sudoku board (9x9 grid where 0 represents empty cells)
     * @return true if the puzzle is solvable, false otherwise
     */
    public boolean solve(int[][] board) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                // Find an empty cell
                if (board[row][col] == EMPTY_CELL) {
                    // Try placing numbers 1-9
                    for (int number = 1; number <= GRID_SIZE; number++) {
                        if (isValidPlacement(board, row, col, number)) {
                            // Place the number if it's valid
                            board[row][col] = number;
                            
                            // Recursively try to solve the rest of the puzzle
                            if (solve(board)) {
                                return true;
                            }
                            
                            // If placing the number doesn't lead to a solution, backtrack
                            board[row][col] = EMPTY_CELL;
                        }
                    }
                    // If no number can be placed, the puzzle is unsolvable
                    return false;
                }
            }
        }
        // If we've filled all cells, the puzzle is solved
        return true;
    }

    /**
     * Checks if placing a number at the specified position is valid.
     *
     * @param board  the Sudoku board
     * @param row    the row index
     * @param col    the column index
     * @param number the number to place
     * @return true if the placement is valid, false otherwise
     */
    private boolean isValidPlacement(int[][] board, int row, int col, int number) {
        return !isNumberInRow(board, row, number) &&
               !isNumberInColumn(board, col, number) &&
               !isNumberInBox(board, row - row % 3, col - col % 3, number);
    }

    /**
     * Checks if the number already exists in the specified row.
     */
    private boolean isNumberInRow(int[][] board, int row, int number) {
        for (int col = 0; col < GRID_SIZE; col++) {
            if (board[row][col] == number) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the number already exists in the specified column.
     */
    private boolean isNumberInColumn(int[][] board, int col, int number) {
        for (int row = 0; row < GRID_SIZE; row++) {
            if (board[row][col] == number) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the number already exists in the 3x3 box.
     */
    private boolean isNumberInBox(int[][] board, int boxStartRow, int boxStartCol, int number) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (board[row + boxStartRow][col + boxStartCol] == number) {
                    return true;
                }
            }
        }
        return false;
    }
}