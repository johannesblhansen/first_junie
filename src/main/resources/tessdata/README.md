# Tesseract OCR Training Data

This directory contains the Tesseract OCR training data files needed for the application to recognize digits in Sudoku puzzles.

## Included Files

- `eng.traineddata`: The English language training data file from the "standard" version of Tesseract OCR training data, which is optimized for speed and smaller file size.

## About the Training Data

The training data file (`eng.traineddata`) is used by Tesseract OCR to recognize text in images. For this application, we specifically use it to recognize digits (1-9) in Sudoku puzzles.

We've included the "fast" version of the training data, which is optimized for speed and has a smaller file size (approximately 4MB) compared to the standard version (which is over 20MB).

## Alternative Training Data

If you want to experiment with different training data files, you can replace the included file with one from these sources:

- Standard (most accurate but largest): https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata
- Best (good balance of accuracy and size): https://github.com/tesseract-ocr/tessdata_best/raw/main/eng.traineddata
- Fast (smallest and fastest, currently used): https://github.com/tesseract-ocr/tessdata_fast/raw/main/eng.traineddata

## Note

The training data file is now included directly in the application package, so no additional downloads are required for the OCR functionality to work.
