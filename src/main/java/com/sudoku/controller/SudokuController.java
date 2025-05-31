package com.sudoku.controller;

import com.sudoku.engine.SudokuSolver;
import com.sudoku.model.SudokuBoard;
import com.sudoku.service.SudokuImageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * REST controller for Sudoku solving operations.
 */
@RestController
@RequestMapping("/api/sudoku")
public class SudokuController {

    private final SudokuSolver sudokuSolver;
    private final SudokuImageService sudokuImageService;

    @Autowired
    public SudokuController(SudokuSolver sudokuSolver, SudokuImageService sudokuImageService) {
        this.sudokuSolver = sudokuSolver;
        this.sudokuImageService = sudokuImageService;
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
     * Extracts a Sudoku board from an image in the clipboard.
     *
     * @return the extracted Sudoku board
     */
    @GetMapping("/extract-from-clipboard")
    public ResponseEntity<SudokuBoard> extractFromClipboard() {
        try {
            SudokuBoard board = sudokuImageService.extractBoardFromClipboard();
            return ResponseEntity.ok(board);
        } catch (RuntimeException e) {
            // Pass through the original error message without wrapping it
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "An unexpected error occurred while extracting the Sudoku board");
        }
    }

    /**
     * Extracts a Sudoku board from an image provided as base64 data.
     *
     * @param requestBody a map containing the base64-encoded image data
     * @return the extracted Sudoku board
     */
    @PostMapping("/extract-from-image")
    public ResponseEntity<SudokuBoard> extractFromImage(@RequestBody Map<String, String> requestBody) {
        try {
            String base64Image = requestBody.get("image");
            if (base64Image == null || base64Image.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No image data provided");
            }

            SudokuBoard board = sudokuImageService.extractBoardFromBase64Image(base64Image);
            return ResponseEntity.ok(board);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            // Pass through the original error message without wrapping it
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "An unexpected error occurred while extracting the Sudoku board");
        }
    }

    /**
     * Extracts a Sudoku board from the clipboard and solves it.
     *
     * @return the solved Sudoku board
     */
    @GetMapping("/solve-from-clipboard")
    public ResponseEntity<SudokuBoard> solveFromClipboard() {
        try {
            // Extract board from clipboard
            SudokuBoard board = sudokuImageService.extractBoardFromClipboard();

            // Create a copy of the board to avoid modifying the input
            int[][] boardCopy = deepCopyBoard(board.getBoard());

            // Solve the puzzle
            boolean solved = sudokuSolver.solve(boardCopy);

            if (!solved) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsolvable Sudoku puzzle");
            }

            return ResponseEntity.ok(new SudokuBoard(boardCopy));
        } catch (ResponseStatusException e) {
            // Pass through existing ResponseStatusExceptions
            throw e;
        } catch (RuntimeException e) {
            // Pass through RuntimeExceptions with their original message
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            // Generic message for other exceptions
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "An unexpected error occurred while solving the Sudoku board");
        }
    }

    /**
     * Extracts a Sudoku board from an image provided as base64 data and solves it.
     *
     * @param requestBody a map containing the base64-encoded image data
     * @return the solved Sudoku board
     */
    @PostMapping("/solve-from-image")
    public ResponseEntity<SudokuBoard> solveFromImage(@RequestBody Map<String, String> requestBody) {
        try {
            String base64Image = requestBody.get("image");
            if (base64Image == null || base64Image.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No image data provided");
            }

            // Extract board from image
            SudokuBoard board = sudokuImageService.extractBoardFromBase64Image(base64Image);

            // Create a copy of the board to avoid modifying the input
            int[][] boardCopy = deepCopyBoard(board.getBoard());

            // Solve the puzzle
            boolean solved = sudokuSolver.solve(boardCopy);

            if (!solved) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsolvable Sudoku puzzle");
            }

            return ResponseEntity.ok(new SudokuBoard(boardCopy));
        } catch (ResponseStatusException e) {
            // Pass through existing ResponseStatusExceptions
            throw e;
        } catch (RuntimeException e) {
            // Pass through RuntimeExceptions with their original message
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            // Generic message for other exceptions
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "An unexpected error occurred while solving the Sudoku board");
        }
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
