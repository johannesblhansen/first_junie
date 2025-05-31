package com.sudoku.service;

import com.sudoku.model.SudokuBoard;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

/**
 * Service for extracting Sudoku boards from images.
 */
@Service
public class SudokuImageService {

    static {
        // Load OpenCV native library
        nu.pattern.OpenCV.loadLocally();
    }


    /**
     * Extracts a Sudoku board from an image provided as a base64 string.
     *
     * @param base64Image the base64-encoded image data
     * @return the extracted Sudoku board
     * @throws RuntimeException if the extraction fails
     */
    public SudokuBoard extractBoardFromBase64Image(String base64Image) {
        try {
            if (base64Image == null || base64Image.isEmpty()) {
                throw new RuntimeException("No image data provided");
            }

            // Remove data URL prefix if present (e.g., "data:image/png;base64,")
            String base64Data = base64Image;
            if (base64Image.contains(",")) {
                base64Data = base64Image.split(",")[1];
            }

            // Decode base64 to byte array
            byte[] imageData = Base64.getDecoder().decode(base64Data);

            return extractBoardFromImageData(imageData);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid base64 image data", e);
        } catch (RuntimeException e) {
            // Don't wrap RuntimeExceptions that already have detailed messages
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract Sudoku board from image: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts a Sudoku board from image data provided as a byte array.
     *
     * @param imageData the image data as a byte array
     * @return the extracted Sudoku board
     * @throws RuntimeException if the extraction fails
     */
    public SudokuBoard extractBoardFromImageData(byte[] imageData) {
        try {
            if (imageData == null || imageData.length == 0) {
                throw new RuntimeException("No image data provided");
            }

            // Convert byte array to BufferedImage
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));

            if (image == null) {
                throw new RuntimeException("Could not read image data. Ensure it's a valid image format.");
            }

            // Process image to get the board
            int[][] board = extractBoardFromImage(image);
            return new SudokuBoard(board);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image data: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            // Don't wrap RuntimeExceptions that already have detailed messages
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract Sudoku board from image: " + e.getMessage(), e);
        }
    }


    /**
     * Extracts a Sudoku board from an image.
     *
     * @param image the image containing the Sudoku board
     * @return a 9x9 array representing the Sudoku board
     */
    private int[][] extractBoardFromImage(BufferedImage image) {
        // Convert BufferedImage to Mat
        Mat mat = bufferedImageToMat(image);

        // Preprocess the image
        Mat preprocessed = preprocessImage(mat);

        // Detect the grid
        Mat grid = detectGrid(preprocessed);

        // Extract cells
        List<Mat> cells = extractCells(grid);

        // Recognize digits
        int[][] board = recognizeDigits(cells);

        return board;
    }

    /**
     * Converts a BufferedImage to an OpenCV Mat.
     * 
     * This method attempts to convert the BufferedImage to a format that OpenCV can reliably read.
     * It first tries PNG format (which is lossless) and falls back to JPG if PNG encoding fails.
     * The method includes several validations to ensure the conversion is successful and to provide
     * detailed error messages if it fails.
     *
     * @param image the BufferedImage to convert
     * @return the converted Mat
     * @throws IllegalArgumentException if the input image is null
     * @throws RuntimeException if the conversion fails for any reason
     */
    private Mat bufferedImageToMat(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Input BufferedImage cannot be null");
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            // Convert to a format that OpenCV can reliably read (PNG is lossless)
            // If PNG fails, try JPG as fallback
            boolean success = ImageIO.write(image, "png", byteArrayOutputStream);
            if (!success) {
                byteArrayOutputStream.reset();
                success = ImageIO.write(image, "jpg", byteArrayOutputStream);
                if (!success) {
                    throw new IOException("Failed to encode image as PNG or JPG");
                }
            }

            byteArrayOutputStream.flush();
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Check if we have valid image data
            if (imageBytes.length == 0) {
                throw new IOException("Image encoding produced empty byte array");
            }

            // Decode the image bytes to Mat
            Mat mat = org.opencv.imgcodecs.Imgcodecs.imdecode(
                new MatOfByte(imageBytes),
                org.opencv.imgcodecs.Imgcodecs.IMREAD_UNCHANGED
            );

            // Verify the Mat was created successfully
            if (mat.empty()) {
                throw new IOException("OpenCV failed to decode the image data");
            }

            return mat;
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert BufferedImage to Mat: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert BufferedImage to Mat: " + e.getMessage(), e);
        }
    }

    /**
     * Converts an OpenCV Mat to a BufferedImage.
     *
     * @param mat the Mat to convert
     * @return the converted BufferedImage
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        try {
            MatOfByte matOfByte = new MatOfByte();
            org.opencv.imgcodecs.Imgcodecs.imencode(".jpg", mat, matOfByte);
            byte[] byteArray = matOfByte.toArray();
            return ImageIO.read(new ByteArrayInputStream(byteArray));
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Mat to BufferedImage", e);
        }
    }

    /**
     * Preprocesses an image for Sudoku grid detection.
     *
     * @param mat the image to preprocess
     * @return the preprocessed image
     */
    private Mat preprocessImage(Mat mat) {
        // Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);

        // Apply Gaussian blur to reduce noise
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);

        // Apply adaptive thresholding to get binary image
        Mat binary = new Mat();
        Imgproc.adaptiveThreshold(blurred, binary, 255,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY_INV, 11, 2);

        return binary;
    }

    /**
     * Detects the Sudoku grid in a preprocessed image.
     *
     * @param binary the preprocessed binary image
     * @return the detected grid
     */
    private Mat detectGrid(Mat binary) {
        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary.clone(), contours, hierarchy,
            Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find the largest contour (likely the Sudoku grid)
        double maxArea = 0;
        MatOfPoint largestContour = null;
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > maxArea) {
                maxArea = area;
                largestContour = contour;
            }
        }

        if (largestContour == null) {
            throw new RuntimeException("Could not detect Sudoku grid in the image");
        }

        // Get the corners of the grid
        MatOfPoint2f curve = new MatOfPoint2f(largestContour.toArray());
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        double epsilon = 0.02 * Imgproc.arcLength(curve, true);
        Imgproc.approxPolyDP(curve, approxCurve, epsilon, true);

        // Perspective transform to get a top-down view of the grid
        Point[] corners = sortCorners(approxCurve.toArray());
        Mat grid = perspectiveTransform(binary, corners);

        return grid;
    }

    /**
     * Sorts the corners of a quadrilateral in the order: top-left, top-right, bottom-right, bottom-left.
     *
     * @param corners the unsorted corners
     * @return the sorted corners
     */
    private org.opencv.core.Point[] sortCorners(org.opencv.core.Point[] corners) {
        if (corners.length != 4) {
            throw new RuntimeException("Expected 4 corners but got " + corners.length);
        }

        org.opencv.core.Point[] sortedCorners = new org.opencv.core.Point[4];
        org.opencv.core.Point center = new org.opencv.core.Point(0, 0);

        // Calculate center point
        for (org.opencv.core.Point corner : corners) {
            center.x += corner.x;
            center.y += corner.y;
        }
        center.x /= 4;
        center.y /= 4;

        // Sort corners
        List<org.opencv.core.Point> topPoints = new ArrayList<>();
        List<org.opencv.core.Point> bottomPoints = new ArrayList<>();

        for (org.opencv.core.Point corner : corners) {
            if (corner.y < center.y) {
                topPoints.add(corner);
            } else {
                bottomPoints.add(corner);
            }
        }

        // Sort top points by x coordinate
        sortedCorners[0] = topPoints.get(0).x < topPoints.get(1).x ? topPoints.get(0) : topPoints.get(1); // top-left
        sortedCorners[1] = topPoints.get(0).x > topPoints.get(1).x ? topPoints.get(0) : topPoints.get(1); // top-right

        // Sort bottom points by x coordinate
        sortedCorners[2] = bottomPoints.get(0).x > bottomPoints.get(1).x ? bottomPoints.get(0) : bottomPoints.get(1); // bottom-right
        sortedCorners[3] = bottomPoints.get(0).x < bottomPoints.get(1).x ? bottomPoints.get(0) : bottomPoints.get(1); // bottom-left

        return sortedCorners;
    }

    /**
     * Applies a perspective transform to get a top-down view of the grid.
     *
     * @param binary  the binary image
     * @param corners the corners of the grid
     * @return the transformed grid
     */
    private Mat perspectiveTransform(Mat binary, org.opencv.core.Point[] corners) {
        // Define the destination points for the transform
        int gridSize = 450; // Output grid size
        MatOfPoint2f src = new MatOfPoint2f(corners);
        MatOfPoint2f dst = new MatOfPoint2f(
            new org.opencv.core.Point(0, 0),
            new org.opencv.core.Point(gridSize, 0),
            new org.opencv.core.Point(gridSize, gridSize),
            new org.opencv.core.Point(0, gridSize)
        );

        // Get the transformation matrix
        Mat transformMatrix = Imgproc.getPerspectiveTransform(src, dst);

        // Apply the transform
        Mat warped = new Mat();
        Imgproc.warpPerspective(binary, warped, transformMatrix, new Size(gridSize, gridSize));

        return warped;
    }

    /**
     * Extracts individual cells from the Sudoku grid.
     *
     * @param grid the Sudoku grid
     * @return a list of cell images
     */
    private List<Mat> extractCells(Mat grid) {
        List<Mat> cells = new ArrayList<>();
        int cellSize = grid.width() / 9;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Rect rect = new Rect(j * cellSize, i * cellSize, cellSize, cellSize);
                Mat cell = new Mat(grid, rect);

                // Add some padding to remove grid lines
                int padding = (int) (cellSize * 0.1);
                if (padding > 0) {
                    Rect innerRect = new Rect(padding, padding,
                        cellSize - 2 * padding, cellSize - 2 * padding);
                    cell = new Mat(cell, innerRect);
                }

                cells.add(cell);
            }
        }

        return cells;
    }

    /**
     * Recognizes digits in the extracted cells using Tesseract OCR.
     *
     * @param cells the extracted cells
     * @return a 9x9 array representing the Sudoku board
     */
    private int[][] recognizeDigits(List<Mat> cells) {
        int[][] board = new int[9][9];
        Tesseract tesseract = new Tesseract();

        // Set Tesseract parameters
        try {
            // Try to find tessdata in the classpath resources
            try {
                // Check if we have tessdata in the resources
                java.net.URL tessDataResource = getClass().getClassLoader().getResource("tessdata");
                if (tessDataResource != null) {
                    // Use the tessdata from resources
                    String tessDataPath = new java.io.File(tessDataResource.toURI()).getAbsolutePath();
                    tesseract.setDatapath(tessDataPath);
                    System.out.println("Using bundled tessdata from: " + tessDataPath);
                } else {
                    // Try to extract tessdata from the JAR
                    java.io.File tempDir = java.io.File.createTempFile("tessdata", "").getParentFile();
                    java.io.File tessDataDir = new java.io.File(tempDir, "tessdata");
                    if (!tessDataDir.exists()) {
                        tessDataDir.mkdir();
                    }

                    // Extract eng.traineddata if it exists in the JAR
                    try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("tessdata/eng.traineddata")) {
                        if (is != null) {
                            java.io.File trainedDataFile = new java.io.File(tessDataDir, "eng.traineddata");
                            java.nio.file.Files.copy(is, trainedDataFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            tesseract.setDatapath(tessDataDir.getAbsolutePath());
                            System.out.println("Extracted tessdata to: " + tessDataDir.getAbsolutePath());
                        }
                    } catch (Exception e) {
                        // If extraction fails, fall back to system tessdata
                        System.out.println("Could not extract tessdata from JAR, falling back to system installation");
                    }
                }
            } catch (Exception e) {
                // If we can't find or extract tessdata, fall back to system installation
                System.out.println("Could not find bundled tessdata, falling back to system installation: " + e.getMessage());
            }

            tesseract.setLanguage("eng");
            tesseract.setPageSegMode(10); // Treat the image as a single character
            tesseract.setOcrEngineMode(1); // Use neural network LSTM engine
        } catch (java.lang.UnsatisfiedLinkError e) {
            throw new RuntimeException("Failed to load Tesseract native library. " +
                                      "This error occurred despite using platform-specific dependencies. " +
                                      "Please ensure your system meets all requirements for running Tesseract OCR.", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Tesseract OCR. " +
                                      "Please ensure the application has access to the tessdata directory.", e);
        }

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Mat cell = cells.get(i * 9 + j);

                // Check if cell is mostly empty (white)
                if (isCellEmpty(cell)) {
                    board[i][j] = 0;
                    continue;
                }

                BufferedImage cellImage = matToBufferedImage(cell);

                try {
                    String result = tesseract.doOCR(cellImage).trim();
                    if (result.matches("[1-9]")) {
                        board[i][j] = Integer.parseInt(result);
                    } else {
                        board[i][j] = 0; // Empty cell or unrecognized digit
                    }
                } catch (TesseractException e) {
                    board[i][j] = 0;
                }
            }
        }

        return board;
    }

    /**
     * Checks if a cell is mostly empty (white).
     *
     * @param cell the cell to check
     * @return true if the cell is mostly empty, false otherwise
     */
    private boolean isCellEmpty(Mat cell) {
        // Count white pixels
        int whitePixels = Core.countNonZero(cell);
        int totalPixels = cell.width() * cell.height();

        // If less than 5% of pixels are non-white, consider the cell empty
        return whitePixels < totalPixels * 0.05;
    }
}
