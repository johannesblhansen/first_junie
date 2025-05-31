package com.sudoku.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SudokuSolverTest {

    private final SudokuSolver sudokuSolver = new SudokuSolver();

    @Test
    void testSolveValidPuzzle() {
        // A valid Sudoku puzzle
        int[][] board = {
            {5, 3, 0, 0, 7, 0, 0, 0, 0},
            {6, 0, 0, 1, 9, 5, 0, 0, 0},
            {0, 9, 8, 0, 0, 0, 0, 6, 0},
            {8, 0, 0, 0, 6, 0, 0, 0, 3},
            {4, 0, 0, 8, 0, 3, 0, 0, 1},
            {7, 0, 0, 0, 2, 0, 0, 0, 6},
            {0, 6, 0, 0, 0, 0, 2, 8, 0},
            {0, 0, 0, 4, 1, 9, 0, 0, 5},
            {0, 0, 0, 0, 8, 0, 0, 7, 9}
        };

        // Solve the puzzle
        boolean result = sudokuSolver.solve(board);

        // Verify the puzzle was solved
        assertTrue(result);
        
        // Verify the solution is valid
        assertTrue(isValidSolution(board));
    }

    @Test
    void testSolveUnsolvablePuzzle() {
        // An unsolvable Sudoku puzzle (two 5s in the same row)
        int[][] board = {
            {5, 3, 0, 0, 7, 0, 0, 0, 5}, // Two 5s in the first row
            {6, 0, 0, 1, 9, 5, 0, 0, 0},
            {0, 9, 8, 0, 0, 0, 0, 6, 0},
            {8, 0, 0, 0, 6, 0, 0, 0, 3},
            {4, 0, 0, 8, 0, 3, 0, 0, 1},
            {7, 0, 0, 0, 2, 0, 0, 0, 6},
            {0, 6, 0, 0, 0, 0, 2, 8, 0},
            {0, 0, 0, 4, 1, 9, 0, 0, 5},
            {0, 0, 0, 0, 8, 0, 0, 7, 9}
        };

        // Try to solve the puzzle
        boolean result = sudokuSolver.solve(board);

        // Verify the puzzle was not solved
        assertFalse(result);
    }

    @Test
    void testSolveEmptyPuzzle() {
        // An empty Sudoku puzzle
        int[][] board = new int[9][9];

        // Solve the puzzle
        boolean result = sudokuSolver.solve(board);

        // Verify the puzzle was solved
        assertTrue(result);
        
        // Verify the solution is valid
        assertTrue(isValidSolution(board));
    }

    /**
     * Checks if the Sudoku solution is valid.
     * A valid solution must have all numbers 1-9 in each row, column, and 3x3 box.
     */
    private boolean isValidSolution(int[][] board) {
        // Check rows
        for (int row = 0; row < 9; row++) {
            boolean[] used = new boolean[10]; // 0 index is not used
            for (int col = 0; col < 9; col++) {
                int num = board[row][col];
                if (num < 1 || num > 9 || used[num]) {
                    return false;
                }
                used[num] = true;
            }
        }

        // Check columns
        for (int col = 0; col < 9; col++) {
            boolean[] used = new boolean[10]; // 0 index is not used
            for (int row = 0; row < 9; row++) {
                int num = board[row][col];
                if (num < 1 || num > 9 || used[num]) {
                    return false;
                }
                used[num] = true;
            }
        }

        // Check 3x3 boxes
        for (int boxRow = 0; boxRow < 3; boxRow++) {
            for (int boxCol = 0; boxCol < 3; boxCol++) {
                boolean[] used = new boolean[10]; // 0 index is not used
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 3; col++) {
                        int num = board[row + boxRow * 3][col + boxCol * 3];
                        if (num < 1 || num > 9 || used[num]) {
                            return false;
                        }
                        used[num] = true;
                    }
                }
            }
        }

        return true;
    }
}