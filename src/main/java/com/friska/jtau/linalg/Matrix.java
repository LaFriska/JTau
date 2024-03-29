package com.friska.jtau.linalg;

import com.friska.jtau.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Matrix {

    private final float[][] state;
    private final MatrixDimension dimensions;


    /**
     * The Matrix class represents a matrix in mathematics where it has more than 1 number of row or columns. The matrix may
     * be further inherited by specific forms of matrices, such as vectors, and square matrices. Thus, the matrix object is malleable
     * and vectors can also be parsed as matrices.
     * **/
    public Matrix(float[][] inputArray){
        if(inputArray == null) throw new IncompatibleMatrixException("The input array must not be null.");
        if(inputArray.length == 0) throw new IncompatibleMatrixException("The input array length must be greater than 0.");
        inputArray = arrayCopy(inputArray);
        check(inputArray);
        state = inputArray;
        dimensions = new MatrixDimension(state.length, state[0].length);
    }

    /**
     * Ensures that every row in the input array have the same length, since a matrix must be rectangular.
     * */
    private static void check(float[][] inputArray){
        for(int r = 1; r < inputArray.length; r++){
            if(inputArray[r].length != inputArray[0].length) throw new IncompatibleMatrixException("Each row of the input array must have the same length.");
        }
    }


    /**
     * Takes in an input array such that each row has varying lengths, and pad rows that are too short with 0s. That way, this
     * method can be called on the float array before parsing in a potentially ragged input array, to prevent IncompatibleMatrixException.
     * */
    public static float[][] pad(float[][] inputArray){
        int longestLength = inputArray[0].length;
        float[][] newArray = new float[inputArray.length][];
        for(int r = 1; r < inputArray.length; r++)
            if(inputArray[r].length > longestLength) longestLength = inputArray[r].length;
        for(int r = 0; r < inputArray.length; r++){
            newArray[r] = Arrays.copyOf(inputArray[r], longestLength);
        }
        return newArray;
    }

    /**
     * Clones the input array.
     * */
    private static float[][] arrayCopy(float[][] array){
        float[][] res = new float[array.length][];
        for (int r = 0; r < array.length; r++) {
            res[r] = new float[array[r].length];
            System.arraycopy(array[r], 0, res[r], 0, array[r].length);
        }
        return res;
    }

    /**
     * Creates a new matrix with String input, where each row is separated using commas, and columns are separated using semicolons.
     * An example of this notation is as below:
     * <p>
     * "4, 2, 6, 3; 5, 2, 6, 2; 12, 13.5, 5, 2; 3, 5, 6, 2"
     * <p>
     * White space are ignored, but it is often nice to format a string like so:
     * <p>
     * "4, 2, 6, 3;<p>
     * 5, 2, 6, 2; <p>
     * 12, 13.5, 5, 2; <p>
     * 3, 5, 6, 2"
     * */
    public static Matrix parse(@NotNull String s){
        try{
            String cleaned = Utils.removeWhitespace(s);
            if(cleaned.isEmpty()) throw new IncompatibleMatrixException("Cannot parse an empty String into Matrix.");
            String[] rowStrings = cleaned.split(";");
            float[][] state = new float[rowStrings.length][];
            String[] currentRow;
            for (int r = 0; r < rowStrings.length; r++) {
                currentRow = rowStrings[r].split(",");
                state[r] = new float[currentRow.length];
                for (int c = 0; c < currentRow.length; c++) {
                    state[r][c] = Float.parseFloat(currentRow[c]);
                }
            }
            return new Matrix(state);
        }catch (NumberFormatException e){
            throw new NumberFormatException("Error, cannot parse \"" + s +"\" into a matrix, make sure all entries are formatted correctly, and should be able to be parsed into the matrix as a float.");
        }
    }

    /**
     * Returns an instance of the MatrixDimension record, where its row length and column length are held.
     * **/
    public final MatrixDimension getDimensions() {
        return dimensions;
    }

    /**
     * Returns the number of rows there are in the matrix.
     * **/
    public final int getRowLength(){
        return dimensions.row();
    }

    /**
     * Returns the number of columns there are in the matrix.
     * **/
    public final int getColLength(){
        return dimensions.col();
    }

    /**
     * Checks if the matrix could be represented as a square matrix.
     * **/
    public final boolean isSquare(){
        return dimensions.row() == dimensions.col();
    }

    /**
     * Checks if the matrix could be represented as a horizontal vector.
     * **/
    public final boolean isHorizontalVector(){
        return dimensions.row() == 1;
    }

    /**
     * Checks if the matrix could be represented as a vertical vector.
     * **/
    public final boolean isVerticalVector(){
        return dimensions.col() == 1;
    }

    /**
     * Retrieves an entry from the matrix given the row and col index. Note that this linear algebra
     * library uses 0-indexing, meaning that the first entry into a matrix always have an index of 0.
     * **/
    public float get(int row, int col){
        if(row >= dimensions.row() || row < 0) throw new IncompatibleMatrixException("Row index of " + row + " is out of bounds.");
        if(col >= dimensions.col() || col < 0) throw new IncompatibleMatrixException("Column index of " + col + " is out of bounds.");
        return state[row][col];
    }

    /**
     * Creates and returns a new SquareMatrix object with the same float array.
     *
     * @throws IncompatibleMatrixException If the number of rows and columns do not match.
     * **/
    public SquareMatrix toSquareMatrix(){
        if(!isSquare()) throw new IncompatibleMatrixException("Cannot convert a non-square matrix into a SquareMatrix object.");
        return new SquareMatrix(state);
    }

    /**
     * Creates and returns a new Vector object with the same data.
     *
     * @throws IncompatibleMatrixException If the number of columns is not 1.
     * **/
    public Vector toVector(){
        if(!isVerticalVector()) throw new IncompatibleMatrixException("Cannot convert a matrix with more than 1 columns to a vector.");
        return getColumnVector(0);
    }

    /**
     * Creates and returns a new matrix object with the same float array. This method is mainly designed
     * for classes that inherit the matrix class, that the user wants to parse into a matrix object.
     * **/
    public Matrix toMatrix(){
        return new Matrix(state);
    }

    public AugmentedMatrix toAugmentedMatrix(){
        return new AugmentedMatrix(state);
    }

    /**
     * Retrieves one column from the matrix and returns it as a vector. This is used in matrix multiplications, and more.
     * **/
    public Vector getColumnVector(int columnIndex){
        if(columnIndex >= dimensions.col() || columnIndex < 0) throw new IncompatibleMatrixException("Cannot extract column vector, as column index of " + columnIndex + " is out of bounds.");
        float[] vec = new float[dimensions.row()];
        for(int r = 0; r < dimensions.row(); r++){
            vec[r] = state[r][columnIndex];
        }
        return new Vector(vec);
    }

    /**
     * Creates and returns a matrix representation of a row vector from the float array.
     * **/
    public Matrix getRowVector(int rowIndex){
        if(rowIndex >= dimensions.row() || rowIndex < 0) throw new IncompatibleMatrixException("Cannot extract row vector, as row index of " + rowIndex + " is out of bounds.");
        float[][] vec = new float[1][dimensions.col()];
        vec[0] = state[rowIndex];
        return new Matrix(vec);
    }

    /**
     * Scans through entries of the matrix and returns true if the entries equate, and false if at least one entry is different.
     * */
    public static <M extends Matrix> boolean compare(M matrix1, M matrix2){
        if((matrix1.getRowLength() != matrix2.getRowLength()) || matrix1.getColLength() != matrix2.getColLength()) return false;
        for(int r = 0; r < matrix1.getRowLength(); r++){
            for(int c = 0; c < matrix1.getColLength(); c++){
                if(matrix1.get(r, c) != matrix2.get(r, c)) return false;
            }
        }
        return true;
    }


    /**
     * With a specified row and column length, this method takes a float array of entries and
     * formats it into a matrix. The procedure follows the way you would read a book (in a Western country),
     * from left to right, top to down, where each element of the first row is input first, then each element of
     * the second row, and so on.
     *
     * @throws IncompatibleMatrixException the row length multiplied by column length is not equal to the numbers of entries
     * **/
    public static Matrix formulate(int rowLength, int colLength, float... entries){
        if(rowLength * colLength != entries.length) throw new IncompatibleMatrixException("Cannot format entries of length " + entries.length + " into a " + rowLength + " x " + colLength + " matrix.");
        int count = 0;
        float[][] state = new float[rowLength][colLength];
        for(int r = 0; r < rowLength; r++){
            for(int c = 0; c < colLength; c++){
                state[r][c] = entries[count];
                count++;
            }
        }
        return new Matrix(state);
    }


    /**
     * Represents the matrix as a string. This is mainly for testing purposes, where the matrix could be printed on
     * the console in similar way as it is represented in mathematics. An example of this would be as follows:
     * <p>
     * ⌈4 2 6 1 5⌉ <p>
     * |3 6 9 2 2|<p>
     * |2 2 6 4 5|<p>
     * |4 6 2 9 8|<p>
     * ⌊0 2 2 2 4⌋<p>
     * **/
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int r = 0; r < dimensions.row(); r++){
            sb.append(dimensions.row() > 1 ? (r == 0 ? "⌈" : r == dimensions.row() - 1 ? "⌊" : "|") : "[");
            for(int c = 0; c < dimensions.col(); c++){
                sb.append(Utils.format(state[r][c]));
                if(c != dimensions.col() - 1) sb.append(" ");
            }
            sb.append(dimensions.row() > 1 ? (r == 0 ? "⌉" : r == dimensions.row() - 1 ? "⌋" : "|") : "]").append("\n");
        }
        return sb.toString();
    }

    /**
     * Returns the Tex string used to represent the matrix.
     *
     * @param id Signifies what type of brackets around the matrix is desired.
     * **/
    public String getTex(String id){ //TODO javadoc
        StringBuilder rowString = new StringBuilder("\\begin{" + id + "}").append("\n");
        for (int r = 0; r < dimensions.row(); r++) {
            for (int c = 0; c < dimensions.col(); c++) {
                rowString.append(Utils.format(get(r, c))).append(c == dimensions.col() - 1 ?  r == dimensions.row() - 1 ? "" : "\\\\" : "&");
            }
            rowString.append("\n");
        }
        rowString.append("\\end{").append(id).append("}");
        return rowString.toString();
    }


    /**
     * Returns the Tex string used to represent the matrix using conventional square brackets.
     * */
    public String getTex(){
        return getTex("bmatrix");
    }

    /**
     * Returns the Tex string used to represent the matrix using vertical lines as brackets
     * signifying the determinant.
     * */
    public String getTexDeterminant(){
        return getTex("vmatrix");
    }

    /**
     * @return The state float array.
     * */
    protected float[][] getState() {
        return state;
    }
}
