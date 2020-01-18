package raulcastilla215alu.matrix;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;

import raulcastilla215alu.mytools.IOModule;

/**
 * Read a CSV file and store the information into a matrix.
 * 
 * @author Raul Castilla Bravo.
 * @author Ricardo Manuel Ruiz Diaz.
 */
public class Matrix {

	/*
	 * Privates attributes.
	 */
	protected ArrayList<ArrayList<String>> matrix;
	
	/**
	 * Constructor. Generate an empty matrix.
	 */
	public Matrix() {
		this.matrix = new ArrayList<>();
	}
	
	/**
	 * Constructor. Initializes the matrix using the information from the 
	 * CSV file introduced by parameters.
	 * 
	 * @param path CSV file path.
	 */
	public Matrix(String path) {
		this.matrix = new ArrayList<>();
		
		readCSV(path, ',');
	}
	
	/**
	 * Constructor. Initializes the matrix using the information from the 
	 * CSV file introduced by parameters.
	 * 
	 * @param path CSV file path.
	 * @param sep column separator.
	 */
	public Matrix(String path, char sep) {
		this.matrix = new ArrayList<>();
		
		readCSV(path, sep);
	}
	
	/**
	 * Saves matrix information into a CSV file.
	 * 
	 * @param path CSV file path.
	 */
	public void toCSV(String path) {
		IOModule.write(path, this.toString(), false);
	}
	
	/**
	 * Read a CSV file from path introduced by parameters and 
	 * store into the matrix.
	 * 
	 * @param path CSV file path.
	 */
	@SuppressWarnings("deprecation")
	protected void readCSV(String path, char sep) {
		
		// Variables locales
		Reader reader = null;
		CSVReader csvReader = null;
		
		try {
			// Lectura de archivo csv
			reader = Files.newBufferedReader(Paths.get(path));
			csvReader = new CSVReader(reader, sep);
			List<String[]> records = csvReader.readAll();
			
			// Por cada fila leida...
			ArrayList<String> aux_row = null;
			for(int i = 0; i < records.size(); i++) {
				aux_row = new ArrayList<>();
				String[] row = records.get(i);
				
				// Por cada columna de la fila...
				for(int j = 0; j < row.length; j++) {
					aux_row.add(row[j]);
				}
				
				// Incluimos la nueva fila en la matriz
				matrix.add(aux_row);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(csvReader != null) {
				try {
					csvReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Add a new row to the matrix.
	 * 
	 * @param newRow row to be added.
	 */
	protected void addRow(ArrayList<String> row) {
		try {
			if(!this.matrix.isEmpty() && this.matrix.get(0).size() != row.size()) {
				throw new SizeException("The length of the row must agree with the width of the matrix");
			}
			
			@SuppressWarnings("unchecked")
			ArrayList<String> newRow = (ArrayList<String>) row.clone();
				
			this.matrix.add(newRow);
			
		} catch(SizeException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Add a new row to the matrix in the position specified by parameter 
	 * and displace down the others rows.
	 * 
	 * @param index Row position.
	 * @param row row to be added.
	 */
	protected void addRow(int index, ArrayList<String> row) {
		try {
			if(!this.matrix.isEmpty() && this.matrix.get(0).size() != row.size()) {
				throw new SizeException("The length of the row must agree with the width of the matrix");
			}
			
			@SuppressWarnings("unchecked")
			ArrayList<String> newRow = (ArrayList<String>) row.clone();
				
			this.matrix.add(index, newRow);
			
		} catch(SizeException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Add a new column to the matrix.
	 * 
	 * @param newColumn Column to be added.
	 */
	protected void addColumn(ArrayList<String> column) {
		
		try {
			if(!matrix.isEmpty() && matrix.size() != column.size()) {
				throw new SizeException("The length of the column must agree with the height of the matrix");
			}
			
			@SuppressWarnings("unchecked")
			ArrayList<String> newColumn = (ArrayList<String>)column.clone();
			
			if(matrix.isEmpty()) {
				for(int indexRow = 0; indexRow < newColumn.size(); indexRow++) {
					matrix.add(new ArrayList<>());
				}
			}
			
			for(int indexRow = 0; indexRow < newColumn.size(); indexRow++) {
				matrix.get(indexRow).add(newColumn.get(indexRow));
			}
				
		} catch (SizeException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Add a new column to the matrix in the position specified by parameter
	 * and displace right other columns.
	 * 
	 * @param index Column index.
	 * @param newColumn Column to be added.
	 */
	protected void addColumn(int index, ArrayList<String> column) {
		
		try {
			if(!matrix.isEmpty() && matrix.size() != column.size()) {
				throw new SizeException("The length of the column must agree with the height of the matrix");
			}
			
			@SuppressWarnings("unchecked")
			ArrayList<String> newColumn = (ArrayList<String>)column.clone();
			
			if(matrix.isEmpty()) {
				for(int indexRow = 0; indexRow < newColumn.size(); indexRow++) {
					matrix.add(index, new ArrayList<>());
				}
			}
			
			for(int indexRow = 0; indexRow < newColumn.size(); indexRow++) {
				matrix.get(indexRow).add(index, newColumn.get(indexRow));
			}
				
		} catch (SizeException ex) {
			ex.printStackTrace();
		}
	}
	
	
	/**
	 * Remove the row specified by index.
	 * 
	 * @param indexRow row index.
	 */
	protected void removeRow(int indexRow) {
		this.matrix.remove(indexRow);
	}
	
	/**
	 * Removve the column specified by index.
	 * 
	 * @param indexColumn index column.
	 */
	protected void removeColumn(int indexColumn) {
		for(int indexRow = 0; indexRow < matrix.size(); indexRow++) {
			this.matrix.get(indexRow).remove(indexColumn);
		}
	}
		
	/**
	 * Set cell value.
	 * 
	 * @param row row index.
	 * @param column column index.
	 * @param element element to insert.
	 */
	protected void set(int row, int column, String element) {
		this.matrix.get(row).set(column, element);
	}
	
	/**
	 * Get cell value.
	 * 
	 * @param row row index. 
	 * @param column column index.
	 * @return cell value.
	 */
	protected String get(int row, int column) {
		return this.matrix.get(row).get(column);
	}
	
	/**
	 * Get matrix width.
	 * 
	 * @return matrix width.
	 */
	protected int getWidth() {
		if(matrix.isEmpty()) {
			return 0;
		}
		return matrix.get(0).size();
	}
	
	/**
	 * Get matrix height.
	 * @return matrix height.
	 */
	protected int getHeight() {
		if(matrix.isEmpty()) {
			return 0;
		} 
		return matrix.size();
	}
	
	/**
	 * Get matrix row.
	 * 
	 * @param row row index.
	 * @return matrix row.
	 */
	@SuppressWarnings("unchecked")
	protected ArrayList<String> getRow(int row){
		if(matrix.isEmpty()) {
			return null;
		}
		return (ArrayList<String>)matrix.get(row).clone();
	}
	
	/**
	 * Get matrix column.
	 * 
	 * @param column column index.
	 * @return matrix column.
	 */
	protected ArrayList<String> getColumn(int column){
		if(matrix.isEmpty()) {
			return null;
		}
		
		ArrayList<String> aux = new ArrayList<>();
		for(int indexRow = 0; indexRow < matrix.size(); indexRow++) {
			aux.add(matrix.get(indexRow).get(column));
		}
		return aux;
	}
		
	/**
	 * Get matrix information in double format.
	 * 
	 * @return double format matrix.
	 */
	protected double[][] castStr2Double(){
		if(matrix.isEmpty()) return null;
		
		double[][] doubleMatrix = new double[matrix.size()][matrix.get(0).size()];
		
		for(int indexRow = 0; indexRow < matrix.size(); indexRow++) {
			for(int indexColumn = 0; indexColumn < matrix.get(0).size(); indexColumn++) {
				doubleMatrix[indexRow][indexColumn] = Double.parseDouble(matrix.get(indexRow).get(indexColumn));
			}
		}
		return doubleMatrix;
	}
	
	/**
	 * Cast an array of string to an array of doubles.
	 * 
	 * @param array strings array.
	 * @return doubles array.
	 */
	protected static double[] castStr2Double(ArrayList<String> array) {
		double[] doubleArray = new double[array.size()];
		
		for(int i = 0; i < array.size(); i++) {
			doubleArray[i] = Double.parseDouble(array.get(i));
		}
		return doubleArray;
	}
	
	/**
	 * Get a string with the information of the matrix
	 * in CSV format.
	 */
	@Override
	public String toString() {
		String str = "";
		
		if(!matrix.isEmpty()) {
			for(int indexRow = 0; indexRow < matrix.size(); indexRow++) {
				for(int indexColumn = 0; indexColumn < matrix.get(indexRow).size(); indexColumn++) {
					str += matrix.get(indexRow).get(indexColumn).toString();
					if(indexColumn < matrix.get(indexRow).size() - 1) {
						 str += ",";
					}
				}
				str += "\n";
			}
		}

		return str;
	}
	
}