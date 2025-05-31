# Sudoku Solver

A web application that solves Sudoku puzzles. Input an unsolved Sudoku puzzle, and the application will find and return a solution.

## Features

- **Web Interface**: User-friendly interface to input and solve Sudoku puzzles
- **REST API**: Backend API for programmatic access to the Sudoku solver
- **Validation**: Validates input puzzles to ensure they are valid Sudoku boards
- **Image Processing**: Extract Sudoku puzzles from images using OCR (requires Tesseract)
- **Clipboard Support**: Import puzzles directly from clipboard images
- **Responsive Design**: Works on both desktop and mobile devices

## Technologies Used

- **Backend**:
  - Java 21
  - Spring Boot 3.2.0
  - Spring MVC for REST API
  - OpenCV 4.5.1 for image processing
  - Tesseract OCR (via Tess4J 5.2.0) for digit recognition
    - Platform-specific native libraries (Windows, Linux, macOS)
  - Maven for dependency management and build

- **Frontend**:
  - HTML5
  - CSS3
  - JavaScript (vanilla)

- **Testing**:
  - JUnit 5
  - Spring Boot Test
  - Mockito

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven

#### Tesseract OCR

The application includes everything you need for OCR functionality:

1. Platform-specific Tesseract OCR libraries are bundled with the application
2. The required training data file (`eng.traineddata`) is already included in the package

No additional downloads or installations are required for the OCR functionality to work.

If the bundled libraries don't work on your system, you can still install Tesseract OCR manually:
  - **Linux**:
    - Ubuntu/Debian: `sudo apt-get install tesseract-ocr libtesseract-dev`
    - Fedora: `sudo dnf install tesseract tesseract-devel`
    - Arch Linux: `sudo pacman -S tesseract tesseract-data-eng`
  - **macOS**: `brew install tesseract`
  - **Windows**: Download and install from [UB-Mannheim's GitHub repository](https://github.com/UB-Mannheim/tesseract/wiki)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/sudoku-solver.git
   cd sudoku-solver
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

4. Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

## Usage

### Web Interface

1. Open your browser and navigate to `http://localhost:8080`
2. Enter the numbers of your unsolved Sudoku puzzle in the grid
3. Leave cells blank for empty positions or use 0
4. Click "Solve" to find the solution
5. Click "Clear" to reset the grid
6. Click "Load Example" to load a sample puzzle

### REST API

#### Solve a Sudoku Puzzle

**Endpoint**: `POST /api/sudoku/solve`

**Request Body**:
```json
{
  "board": [
    [5, 3, 0, 0, 7, 0, 0, 0, 0],
    [6, 0, 0, 1, 9, 5, 0, 0, 0],
    [0, 9, 8, 0, 0, 0, 0, 6, 0],
    [8, 0, 0, 0, 6, 0, 0, 0, 3],
    [4, 0, 0, 8, 0, 3, 0, 0, 1],
    [7, 0, 0, 0, 2, 0, 0, 0, 6],
    [0, 6, 0, 0, 0, 0, 2, 8, 0],
    [0, 0, 0, 4, 1, 9, 0, 0, 5],
    [0, 0, 0, 0, 8, 0, 0, 7, 9]
  ]
}
```

**Response** (200 OK):
```json
{
  "board": [
    [5, 3, 4, 6, 7, 8, 9, 1, 2],
    [6, 7, 2, 1, 9, 5, 3, 4, 8],
    [1, 9, 8, 3, 4, 2, 5, 6, 7],
    [8, 5, 9, 7, 6, 1, 4, 2, 3],
    [4, 2, 6, 8, 5, 3, 7, 9, 1],
    [7, 1, 3, 9, 2, 4, 8, 5, 6],
    [9, 6, 1, 5, 3, 7, 2, 8, 4],
    [2, 8, 7, 4, 1, 9, 6, 3, 5],
    [3, 4, 5, 2, 8, 6, 1, 7, 9]
  ]
}
```

**Error Responses**:
- 400 Bad Request: If the board is invalid or unsolvable

#### Extract a Sudoku Board from Clipboard

**Endpoint**: `GET /api/sudoku/extract-from-clipboard`

**Request Body**: None

**Response** (200 OK):
```json
{
  "board": [
    [5, 3, 0, 0, 7, 0, 0, 0, 0],
    [6, 0, 0, 1, 9, 5, 0, 0, 0],
    [0, 9, 8, 0, 0, 0, 0, 6, 0],
    [8, 0, 0, 0, 6, 0, 0, 0, 3],
    [4, 0, 0, 8, 0, 3, 0, 0, 1],
    [7, 0, 0, 0, 2, 0, 0, 0, 6],
    [0, 6, 0, 0, 0, 0, 2, 8, 0],
    [0, 0, 0, 4, 1, 9, 0, 0, 5],
    [0, 0, 0, 0, 8, 0, 0, 7, 9]
  ]
}
```

**Error Responses**:
- 500 Internal Server Error: If no image is found in clipboard or OCR fails

#### Extract a Sudoku Board from Image

**Endpoint**: `POST /api/sudoku/extract-from-image`

**Request Body**:
```json
{
  "image": "base64_encoded_image_data"
}
```

**Response** (200 OK):
```json
{
  "board": [
    [5, 3, 0, 0, 7, 0, 0, 0, 0],
    [6, 0, 0, 1, 9, 5, 0, 0, 0],
    [0, 9, 8, 0, 0, 0, 0, 6, 0],
    [8, 0, 0, 0, 6, 0, 0, 0, 3],
    [4, 0, 0, 8, 0, 3, 0, 0, 1],
    [7, 0, 0, 0, 2, 0, 0, 0, 6],
    [0, 6, 0, 0, 0, 0, 2, 8, 0],
    [0, 0, 0, 4, 1, 9, 0, 0, 5],
    [0, 0, 0, 0, 8, 0, 0, 7, 9]
  ]
}
```

**Error Responses**:
- 400 Bad Request: If no image data is provided
- 500 Internal Server Error: If OCR fails

#### Solve a Sudoku Board from Clipboard

**Endpoint**: `GET /api/sudoku/solve-from-clipboard`

**Request Body**: None

**Response** (200 OK):
```json
{
  "board": [
    [5, 3, 4, 6, 7, 8, 9, 1, 2],
    [6, 7, 2, 1, 9, 5, 3, 4, 8],
    [1, 9, 8, 3, 4, 2, 5, 6, 7],
    [8, 5, 9, 7, 6, 1, 4, 2, 3],
    [4, 2, 6, 8, 5, 3, 7, 9, 1],
    [7, 1, 3, 9, 2, 4, 8, 5, 6],
    [9, 6, 1, 5, 3, 7, 2, 8, 4],
    [2, 8, 7, 4, 1, 9, 6, 3, 5],
    [3, 4, 5, 2, 8, 6, 1, 7, 9]
  ]
}
```

**Error Responses**:
- 400 Bad Request: If the extracted board is unsolvable
- 500 Internal Server Error: If no image is found in clipboard or OCR fails

#### Solve a Sudoku Board from Image

**Endpoint**: `POST /api/sudoku/solve-from-image`

**Request Body**:
```json
{
  "image": "base64_encoded_image_data"
}
```

**Response** (200 OK):
```json
{
  "board": [
    [5, 3, 4, 6, 7, 8, 9, 1, 2],
    [6, 7, 2, 1, 9, 5, 3, 4, 8],
    [1, 9, 8, 3, 4, 2, 5, 6, 7],
    [8, 5, 9, 7, 6, 1, 4, 2, 3],
    [4, 2, 6, 8, 5, 3, 7, 9, 1],
    [7, 1, 3, 9, 2, 4, 8, 5, 6],
    [9, 6, 1, 5, 3, 7, 2, 8, 4],
    [2, 8, 7, 4, 1, 9, 6, 3, 5],
    [3, 4, 5, 2, 8, 6, 1, 7, 9]
  ]
}
```

**Error Responses**:
- 400 Bad Request: If no image data is provided or the extracted board is unsolvable
- 500 Internal Server Error: If OCR fails

## How It Works

The Sudoku solver uses a backtracking algorithm to solve puzzles:

1. Find an empty cell in the grid
2. Try placing numbers 1-9 in the cell
3. Check if the number is valid according to Sudoku rules (no duplicates in row, column, or 3x3 box)
4. If valid, recursively try to solve the rest of the puzzle
5. If the recursive call returns false, backtrack and try a different number
6. If all numbers 1-9 have been tried and none work, the puzzle is unsolvable

## Testing

The project includes comprehensive unit tests for both the solver engine and the REST API.

To run the tests:
```bash
mvn test
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Thanks to all contributors who have helped with this project
- Inspired by various Sudoku solving algorithms and implementations
