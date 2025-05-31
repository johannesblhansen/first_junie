package com.sudoku.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sudoku.engine.SudokuSolver;
import com.sudoku.model.SudokuBoard;
import com.sudoku.service.SudokuImageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @MockBean
    private SudokuImageService sudokuImageService;

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

    @Test
    void testExtractFromClipboard() throws Exception {
        // Create a mock Sudoku board to be returned by the service
        int[][] board = new int[9][9];
        board[0][0] = 5;
        board[0][1] = 3;
        SudokuBoard mockBoard = new SudokuBoard(board);

        // Mock the service to return the board
        when(sudokuImageService.extractBoardFromClipboard()).thenReturn(mockBoard);

        // Perform the request and validate the response
        mockMvc.perform(get("/api/sudoku/extract-from-clipboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.board").exists())
                .andExpect(jsonPath("$.board[0][0]").value(5))
                .andExpect(jsonPath("$.board[0][1]").value(3));
    }

    @Test
    void testExtractFromClipboardError() throws Exception {
        // Mock the service to throw an exception
        when(sudokuImageService.extractBoardFromClipboard())
                .thenThrow(new RuntimeException("No image found in clipboard"));

        // Perform the request and validate the response
        mockMvc.perform(get("/api/sudoku/extract-from-clipboard"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testSolveFromClipboard() throws Exception {
        // Create a mock Sudoku board to be returned by the service
        int[][] board = new int[9][9];
        board[0][0] = 5;
        board[0][1] = 3;
        SudokuBoard mockBoard = new SudokuBoard(board);

        // Mock the service to return the board
        when(sudokuImageService.extractBoardFromClipboard()).thenReturn(mockBoard);

        // Mock the solver to return success
        when(sudokuSolver.solve(any(int[][].class))).thenReturn(true);

        // Perform the request and validate the response
        mockMvc.perform(get("/api/sudoku/solve-from-clipboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.board").exists());
    }

    @Test
    void testSolveFromClipboardUnsolvable() throws Exception {
        // Create a mock Sudoku board to be returned by the service
        int[][] board = new int[9][9];
        board[0][0] = 5;
        board[0][1] = 3;
        SudokuBoard mockBoard = new SudokuBoard(board);

        // Mock the service to return the board
        when(sudokuImageService.extractBoardFromClipboard()).thenReturn(mockBoard);

        // Mock the solver to return failure
        when(sudokuSolver.solve(any(int[][].class))).thenReturn(false);

        // Perform the request and validate the response
        mockMvc.perform(get("/api/sudoku/solve-from-clipboard"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testExtractFromImage() throws Exception {
        // Create a mock Sudoku board to be returned by the service
        int[][] board = new int[9][9];
        board[0][0] = 5;
        board[0][1] = 3;
        SudokuBoard mockBoard = new SudokuBoard(board);

        // Mock the service to return the board
        when(sudokuImageService.extractBoardFromBase64Image(any(String.class))).thenReturn(mockBoard);

        // Create a request body with a base64 image
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("image", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

        // Perform the request and validate the response
        mockMvc.perform(post("/api/sudoku/extract-from-image")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.board").exists())
                .andExpect(jsonPath("$.board[0][0]").value(5))
                .andExpect(jsonPath("$.board[0][1]").value(3));
    }

    @Test
    void testExtractFromImageNoImageData() throws Exception {
        // Create a request body with no image data
        Map<String, String> requestBody = new HashMap<>();

        // Perform the request and validate the response
        mockMvc.perform(post("/api/sudoku/extract-from-image")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testExtractFromImageError() throws Exception {
        // Mock the service to throw an exception
        when(sudokuImageService.extractBoardFromBase64Image(any(String.class)))
                .thenThrow(new RuntimeException("Invalid image data"));

        // Create a request body with a base64 image
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("image", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

        // Perform the request and validate the response
        mockMvc.perform(post("/api/sudoku/extract-from-image")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testSolveFromImage() throws Exception {
        // Create a mock Sudoku board to be returned by the service
        int[][] board = new int[9][9];
        board[0][0] = 5;
        board[0][1] = 3;
        SudokuBoard mockBoard = new SudokuBoard(board);

        // Mock the service to return the board
        when(sudokuImageService.extractBoardFromBase64Image(any(String.class))).thenReturn(mockBoard);

        // Mock the solver to return success
        when(sudokuSolver.solve(any(int[][].class))).thenReturn(true);

        // Create a request body with a base64 image
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("image", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

        // Perform the request and validate the response
        mockMvc.perform(post("/api/sudoku/solve-from-image")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.board").exists());
    }

    @Test
    void testSolveFromImageNoImageData() throws Exception {
        // Create a request body with no image data
        Map<String, String> requestBody = new HashMap<>();

        // Perform the request and validate the response
        mockMvc.perform(post("/api/sudoku/solve-from-image")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSolveFromImageUnsolvable() throws Exception {
        // Create a mock Sudoku board to be returned by the service
        int[][] board = new int[9][9];
        board[0][0] = 5;
        board[0][1] = 3;
        SudokuBoard mockBoard = new SudokuBoard(board);

        // Mock the service to return the board
        when(sudokuImageService.extractBoardFromBase64Image(any(String.class))).thenReturn(mockBoard);

        // Mock the solver to return failure
        when(sudokuSolver.solve(any(int[][].class))).thenReturn(false);

        // Create a request body with a base64 image
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("image", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

        // Perform the request and validate the response
        mockMvc.perform(post("/api/sudoku/solve-from-image")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }
}
