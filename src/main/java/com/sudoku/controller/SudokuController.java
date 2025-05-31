package com.sudoku.controller;

import com.sudoku.engine.SudokuSolver;
import com.sudoku.model.SudokuBoard;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for Sudoku solving operations.
 */
@RestController
@RequestMapping("/api/sudoku")
public class SudokuController {

    private final SudokuSolver sudokuSolver;

    @Autowired
    public SudokuController(SudokuSolver sudokuSolver) {
        this.sudokuSolver = sudokuSolver;
    }

    /**
     * Solves a Sudoku puzzle.
     *
     * @param sudokuBoard the unsolved Sudoku board
     * @return the solved Sudoku board
     */
    @PostMapping("/solve")
    public ResponseEntity<SudokuBoard> solveSudoku(@Valid @RequestBody SudokuBoard sudokuBoard) {
        if (!sudokuBoard.isValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Sudoku board");
        }

        // Create a copy of the board to avoid modifying the input
        int[][] boardCopy = deepCopyBoard(sudokuBoard.getBoard());

        // Solve the puzzle
        boolean solved = sudokuSolver.solve(boardCopy);

        if (!solved) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsolvable Sudoku puzzle");
        }

        return ResponseEntity.ok(new SudokuBoard(boardCopy));
    }

    /**
     * Creates a deep copy of the Sudoku board.
     *
     * @param original the original board
     * @return a deep copy of the board
     */
    private int[][] deepCopyBoard(int[][] original) {
        if (original == null) {
            return null;
        }

        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }

        return copy;
    }
}