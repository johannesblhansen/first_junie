package com.sudoku.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sudoku.engine.SudokuSolver;
import com.sudoku.model.SudokuBoard;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SudokuController.class)
class SudokuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SudokuSolver sudokuSolver;

    @Test
    void testSolveSudokuSuccess() throws Exception {
        // Create a valid Sudoku board
        int[][] board = new int[9][9];
        board[0][0] = 5;
        board[0][1] = 3;
        SudokuBoard sudokuBoard = new SudokuBoard(board);

        // Mock the solver to return success
        when(sudokuSolver.solve(any(int[][].class))).thenReturn(true);

        // Perform the request and validate the response
        mockMvc.perform(post("/api/sudoku/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sudokuBoard)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.board").exists());
    }

    @Test
    void testSolveSudokuUnsolvable() throws Exception {
        // Create a valid Sudoku board
        int[][] board = new int[9][9];
        board[0][0] = 5;
        board[0][1] = 3;
        SudokuBoard sudokuBoard = new SudokuBoard(board);

        // Mock the solver to return failure
        when(sudokuSolver.solve(any(int[][].class))).thenReturn(false);

        // Perform the request and validate the response
        mockMvc.perform(post("/api/sudoku/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sudokuBoard)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSolveSudokuInvalidBoard() throws Exception {
        // Create an invalid Sudoku board (null)
        SudokuBoard sudokuBoard = new SudokuBoard(null);

        // Perform the request and validate the response
        mockMvc.perform(post("/api/sudoku/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sudokuBoard)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSolveSudokuInvalidValues() throws Exception {
        // Create an invalid Sudoku board (with values outside 0-9 range)
        int[][] board = new int[9][9];
        board[0][0] = 10; // Invalid value
        SudokuBoard sudokuBoard = new SudokuBoard(board);

        // Perform the request and validate the response
        mockMvc.perform(post("/api/sudoku/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sudokuBoard)))
                .andExpect(status().isBadRequest());
    }
}