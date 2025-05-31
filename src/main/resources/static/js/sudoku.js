document.addEventListener('DOMContentLoaded', function() {
    // DOM elements
    const sudokuGrid = document.getElementById('sudoku-grid');
    const solveBtn = document.getElementById('solve-btn');
    const clearBtn = document.getElementById('clear-btn');
    const exampleBtn = document.getElementById('example-btn');
    const clipboardBtn = document.getElementById('clipboard-btn');
    const statusMessage = document.getElementById('status-message');

    // Example Sudoku puzzle
    const examplePuzzle = [
        [5, 3, 0, 0, 7, 0, 0, 0, 0],
        [6, 0, 0, 1, 9, 5, 0, 0, 0],
        [0, 9, 8, 0, 0, 0, 0, 6, 0],
        [8, 0, 0, 0, 6, 0, 0, 0, 3],
        [4, 0, 0, 8, 0, 3, 0, 0, 1],
        [7, 0, 0, 0, 2, 0, 0, 0, 6],
        [0, 6, 0, 0, 0, 0, 2, 8, 0],
        [0, 0, 0, 4, 1, 9, 0, 0, 5],
        [0, 0, 0, 0, 8, 0, 0, 7, 9]
    ];

    // Initialize the grid
    initializeGrid();

    // Event listeners
    solveBtn.addEventListener('click', solveSudoku);
    clearBtn.addEventListener('click', clearGrid);
    exampleBtn.addEventListener('click', loadExample);
    clipboardBtn.addEventListener('click', extractFromClipboard);

    /**
     * Initializes the Sudoku grid with input cells
     */
    function initializeGrid() {
        sudokuGrid.innerHTML = '';

        for (let row = 0; row < 9; row++) {
            for (let col = 0; col < 9; col++) {
                const cell = document.createElement('div');
                cell.className = 'cell';

                const input = document.createElement('input');
                input.type = 'text';
                input.maxLength = 1;
                input.dataset.row = row;
                input.dataset.col = col;

                // Only allow numbers 1-9 and empty/backspace
                input.addEventListener('keypress', function(e) {
                    if (!/[1-9]/.test(e.key)) {
                        e.preventDefault();
                    }
                });

                // Clear status message when user starts editing
                input.addEventListener('focus', function() {
                    statusMessage.textContent = '';
                    statusMessage.className = 'status-message';
                });

                cell.appendChild(input);
                sudokuGrid.appendChild(cell);
            }
        }
    }

    /**
     * Collects the current state of the Sudoku grid
     * @returns {Array} 2D array representing the Sudoku board
     */
    function getBoardState() {
        const board = Array(9).fill().map(() => Array(9).fill(0));
        const inputs = sudokuGrid.querySelectorAll('input');

        inputs.forEach(input => {
            const row = parseInt(input.dataset.row);
            const col = parseInt(input.dataset.col);
            const value = input.value.trim();

            board[row][col] = value === '' ? 0 : parseInt(value);
        });

        return board;
    }

    /**
     * Updates the grid with the provided board state
     * @param {Array} board - 2D array representing the Sudoku board
     * @param {boolean} markOriginal - Whether to mark original numbers
     */
    function updateGrid(board, markOriginal = false) {
        const inputs = sudokuGrid.querySelectorAll('input');
        const originalBoard = markOriginal ? getBoardState() : null;

        inputs.forEach(input => {
            const row = parseInt(input.dataset.row);
            const col = parseInt(input.dataset.col);
            const value = board[row][col];

            input.value = value === 0 ? '' : value;

            // Mark cells as original or solved
            if (markOriginal) {
                if (originalBoard[row][col] !== 0) {
                    input.classList.add('original');
                } else if (value !== 0) {
                    input.classList.add('solved');
                }

                // Make all cells read-only after solving
                input.readOnly = true;
            }
        });
    }

    /**
     * Sends the current board state to the API for solving
     */
    function solveSudoku() {
        const board = getBoardState();

        // Check if the board is empty
        if (board.flat().every(cell => cell === 0)) {
            showStatus('Please enter some numbers first', 'error');
            return;
        }

        // Show loading message
        showStatus('Solving...', '');

        fetch('/api/sudoku/solve', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ board: board })
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text);
                });
            }
            return response.json();
        })
        .then(data => {
            updateGrid(data.board, true);
            showStatus('Puzzle solved successfully!', 'success');
        })
        .catch(error => {
            let errorMessage = 'Failed to solve the puzzle';

            // Try to extract the error message from the response
            try {
                const errorObj = JSON.parse(error.message);
                if (errorObj.message) {
                    errorMessage = errorObj.message;
                }
            } catch (e) {
                // If parsing fails, use the error message as is
                if (error.message) {
                    errorMessage = error.message;
                }
            }

            showStatus(errorMessage, 'error');
        });
    }

    /**
     * Clears the Sudoku grid
     */
    function clearGrid() {
        initializeGrid();
        showStatus('', '');
    }

    /**
     * Loads the example Sudoku puzzle
     */
    function loadExample() {
        updateGrid(examplePuzzle);
        showStatus('Example puzzle loaded', '');
    }

    /**
     * Extracts a Sudoku board from the clipboard
     */
    function extractFromClipboard() {
        // Show loading message
        showStatus('Extracting from clipboard... You may be prompted to allow clipboard access.', '');

        // Check if Clipboard API is supported
        if (!navigator.clipboard) {
            showStatus('Clipboard API is not supported in your browser. Please use a modern browser.', 'error');
            return;
        }

        // Try to read from clipboard using the Clipboard API
        navigator.clipboard.read()
            .then(clipboardItems => {
                // Find an image in the clipboard
                const imageItem = clipboardItems.find(item => 
                    item.types.some(type => type.startsWith('image/')));

                if (!imageItem) {
                    throw new Error('No image found in clipboard. Please copy a Sudoku puzzle screenshot to your clipboard first.');
                }

                // Get the image type
                const imageType = imageItem.types.find(type => type.startsWith('image/'));

                // Get the image blob
                return imageItem.getType(imageType);
            })
            .then(blob => {
                // Convert blob to base64
                return blobToBase64(blob);
            })
            .then(base64Image => {
                // Send the base64 image to the backend
                return fetch('/api/sudoku/extract-from-image', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ image: base64Image })
                });
            })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        throw new Error(text);
                    });
                }
                return response.json();
            })
            .then(data => {
                updateGrid(data.board);
                showStatus('Sudoku board extracted from clipboard', 'success');
            })
            .catch(error => {
                let errorMessage = 'Failed to extract Sudoku board from clipboard';

                // Try to extract the error message from the response
                try {
                    const errorObj = JSON.parse(error.message);
                    if (errorObj.message) {
                        errorMessage = errorObj.message;
                    }
                } catch (e) {
                    // If parsing fails, use the error message as is
                    if (error.message) {
                        errorMessage = error.message;
                    }
                }

                showStatus(errorMessage, 'error');

            });
    }


    /**
     * Converts a Blob to a base64 string
     * @param {Blob} blob - The blob to convert
     * @returns {Promise<string>} A promise that resolves with the base64 string
     */
    function blobToBase64(blob) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onloadend = () => resolve(reader.result);
            reader.onerror = reject;
            reader.readAsDataURL(blob);
        });
    }

    /**
     * Displays a status message
     * @param {string} message - The message to display
     * @param {string} type - The type of message (error, success, etc.)
     */
    function showStatus(message, type) {
        statusMessage.textContent = message;
        statusMessage.className = 'status-message';

        if (type) {
            statusMessage.classList.add(type);
        }
    }
});
