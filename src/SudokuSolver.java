import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import lpsolve.*;

public class SudokuSolver {
	
	static final int VAR_NUMBER = 729;
	static final int CELLS_NUM = 81;
	static final int ROW_SIZE = (int) Math.sqrt(CELLS_NUM);
	static final int COLUMN_SIZE = ROW_SIZE;
	static final int MAX_VALUE = ROW_SIZE;
	static int[] colno = new int[COLUMN_SIZE];
	static double[] sparseRow = new double[ROW_SIZE];
	static LpSolve lp;
	static BufferedReader br = null;
	static double[] objFunc = new double[VAR_NUMBER + 1];

  public static void main(String[] args) {
		try {
			
			final long start = System.nanoTime();

			setFilePath("C:\\temp\\5.txt");

			String line = null;
			int x = 0;
			while ((line = getNextLine()) != null && (x < 10000)) {
				initLp();
				loadMatrixFromString(line);
				lp.setObjFn(objFunc);
				lp.setMaxim();
				if(lp.solve() != LpSolve.OPTIMAL) {
					System.err.println("NOT OPTIMAL for line " + x);
				}
				x++;
				//System.out.println(x);
				//lp.writeLp("modelSpecific"+x+".lp");
				lp.deleteLp();
			}
			
			//lp.deleteLp();
			
			final long end = System.nanoTime();
			System.out.println(formatTime(end - start));
			System.out.println("DONE");

			closeFile();
			
		} catch (LpSolveException e) {
			e.printStackTrace();
		}
  }
  
	public static int getIndex(int row, int column, int value) {
		return 81 * (row - 1) + 9 * (column - 1) + value;
	}
	
	static void loadMatrixFromString(String matrixStr) throws LpSolveException {
		char[] charArr = matrixStr.toCharArray();
		
		for(int i = 0; i < ROW_SIZE; i++) {
			for(int j = 0; j < COLUMN_SIZE; j++) {
				if(charArr[9 * i + j] - '0' != 0 ) {
					addConstraint(i, j, charArr[9 * i + j] - '0');
				}
			}
		}
		lp.setAddRowmode(false);
	}

	private static void addConstraint(int i, int j, int value) throws LpSolveException {
		int j1;
		int v2;
		int v3;
		int v4;
		int v;
		
		int boxXTopCorner = (i / 3) * 3;
		int boxYTopCorner = (j / 3) * 3;
		
		for(int k2 = 1; k2 <= ROW_SIZE; k2++) {
			j1 = 0;
			v = 0;
			v2 = 0;
			v3 = 0;
			v4 = 0;
			if(k2 == value) {
				v = 1;
			}
			if (k2 == i + 1) {
				v2 = 1;
			}
			if (k2 == j + 1) {
				v3 = 1;
			}
			// No other integers in this (i,j) position
			colno[j1] = getIndex(i + 1, j + 1, k2);
			lp.addConstraintex(1, sparseRow, colno, LpSolve.EQ, v);
			
			// No other cell with this value on the same column
			colno[j1] = getIndex(k2, j + 1, value);
			lp.addConstraintex(1, sparseRow, colno, LpSolve.EQ, v2);
			
			// No other cell with this value on the same row
			colno[j1] = getIndex(i + 1, k2, value);
			lp.addConstraintex(1, sparseRow, colno, LpSolve.EQ, v3);
			
			// TODO: No Other same values in the same box  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
			int boxX = boxXTopCorner + ((k2 - 1) / 3);
			int boxY = boxYTopCorner + ((k2 - 1) % 3);
			if(boxX == i && boxY == j) {
				v4 = 1;
			}			
			colno[j1] = getIndex( boxX + 1, boxY + 1, value);
			lp.addConstraintex(1, sparseRow, colno, LpSolve.EQ, v4);
		}
	}
	
	private static void closeFile() {
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	private static String getNextLine() {
		try {
			return br.readLine();
		} catch (IOException e) {
			return null;
		}
	}
	private static void setFilePath(String path) {

		try {
			br = new BufferedReader(new FileReader(path));
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public static String createString(int i, int j, int k) {
		return "" + i + j + k;
	}
	
	// A method that converts the nano-seconds to Seconds-Minutes-Hours form
	private static String formatTime(long nanoSeconds)
	{
	    int hours, minutes, remainder, totalSecondsNoFraction, seconds;
	    double totalSeconds;

	    // Calculating hours, minutes and seconds
	    totalSeconds = (double) nanoSeconds / 1000000000.0;
	    String s = Double.toString(totalSeconds);
	    String [] arr = s.split("\\.");
	    totalSecondsNoFraction = Integer.parseInt(arr[0]);
	    hours = totalSecondsNoFraction / 3600;
	    remainder = totalSecondsNoFraction % 3600;
	    minutes = remainder / 60;
	    seconds = remainder % 60;

	    // Formatting the string that conatins hours, minutes and seconds
	    StringBuilder result = new StringBuilder();
	    if (hours < 10) {
	    	result.append("0" + hours);
	    }
	    else {
	    	result.append(hours);
	    }
	    
	    if (minutes < 10) {
	    	result.append(":0" + minutes);
	    }
	    else {
	    	result.append(":" + minutes);
	    }
	    if (seconds < 10) {
	    	result.append(":0" + seconds);
	    }
	    else {
	    	result.append(":" + seconds);
	    }
	    result.append(":" + (nanoSeconds / 10000000));
	    
	    return result.toString();
	}
	
	private static void initLp() {
		try {
			lp = LpSolve.makeLp(0, VAR_NUMBER);
			lp.setVerbose(LpSolve.IMPORTANT);
			
			lp.setAddRowmode(true);
			
			// setup variable names
			int loop = 1;
			for(int i = 1; i <= ROW_SIZE; i++) {
				for(int j1 = 1; j1 <= COLUMN_SIZE; j1++) {
					for(int k = 1; k <= MAX_VALUE; k++) {
						lp.setColName(loop++, "x" + createString(i, j1, k));
					}
				}
			}
			
			// Adding the coefficient 1 to all vars
			for (int i = 0; i < ROW_SIZE; i++) {
				sparseRow[i] = 1;
			}
			
			// Dealing with only one value per cell
			for (int row = 0; row < ROW_SIZE; row++) {
				for (int column = 0; column < COLUMN_SIZE; column++) {
					for (int value = 0; value < MAX_VALUE; value++) {
						colno[value] = getIndex(row + 1, column + 1, value + 1);
					}
					lp.addConstraintex(9, sparseRow, colno, LpSolve.EQ, 1);
				}
			}
			
			// Dealing with only one value per row
			for (int row = 0; row < ROW_SIZE; row++) {
				for (int value = 0; value < MAX_VALUE; value++) {
					for (int column = 0; column < COLUMN_SIZE; column++) {
						colno[column] = getIndex(row + 1, column + 1, value + 1);
					}
					lp.addConstraintex(9, sparseRow, colno, LpSolve.EQ, 1);
				}
			}
			
			// Dealing with only one value per column
			for (int column = 0; column < COLUMN_SIZE; column++) {
				for (int value = 0; value < MAX_VALUE; value++) {
					for (int row = 0; row < ROW_SIZE; row++) {
						colno[row] = getIndex(row + 1, column + 1, value + 1);
					}
					lp.addConstraintex(9, sparseRow, colno, LpSolve.EQ, 1);
				}
			}
			
			// Dealing with only one value per box
			int sqrt = (int)Math.sqrt(MAX_VALUE);
			for (int m = 0; m < sqrt; m++) {
				for (int i = 0; i < sqrt; i++) {
					for (int value = 0; value < MAX_VALUE; value++) {
						int j = 0;
						for (int k = 0; k < sqrt; k++) {
							for (int n = sqrt * i; n < sqrt * i + sqrt ; n++) {
								colno[j++] = getIndex(k + (sqrt * m) + 1, n + 1, value + 1);
							}
						}
						lp.addConstraintex(9, sparseRow, colno, LpSolve.EQ, 1);
					}
				}
			}
			
			for (int i = 0; i < VAR_NUMBER; i++) {
				lp.setBinary(i + 1, true);
			}
		} catch (LpSolveException e) {
			e.printStackTrace();
		}
	}
		
}