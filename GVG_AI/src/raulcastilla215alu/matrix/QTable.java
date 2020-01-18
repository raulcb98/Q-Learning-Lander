package raulcastilla215alu.matrix;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import ontology.Types.ACTIONS;
import raulcastilla215alu.mytools.*;

/**
 * Defines a Qtable.
 * @author Ricardo Manuel Ruiz Diaz.
 * @author Raul Castilla Bravo.
 */
public class QTable extends Matrix {

	/**
	 * Privates attributes.
	 */
	private ArrayList<State> states;
	private ArrayList<ACTIONS> actions;
	
	/**
	 * Constructor. Initializes the Qtable using the information introduced
	 * by parameters. 
	 * 
	 * @param states array of row index.
	 * @param actions array of column index.
	 * @param path CSV file path to load the qtable. If the CSV file doesnt exist
	 * generates a random Qtable.
	 */
	public QTable(ArrayList<State> states, ArrayList<ACTIONS> actions, String path) {
		this.states = states;
		this.actions = actions;
		
		File qTableFile = new File(path);
		if (!qTableFile.exists()) {
			initializesWithRandoms();
			System.out.println("Generate my own QTable");
		} else {
			readCSV(path, ',');
			System.out.println("Read the QTable successfully");
		}
	}
	
	/**
	 * Get a Q value.
	 * 
	 * @param s state to index the row.
	 * @param a action to index the column.
	 * @return Qvalue.
	 */
	public float get(State s, ACTIONS a) {
		int row = states.indexOf(s);
		int column = actions.indexOf(a);		
		return Float.parseFloat(get(row, column));
	}
	
	/**
	 * Set a Q value.
	 * 
	 * @param s state to index the row.
	 * @param a action to index the column.
	 * @param qValue 
	 */
	public void set(State s, ACTIONS a, float qValue) {
		int row = states.indexOf(s);
		int column = actions.indexOf(a);		
		set(row, column, Float.toString(qValue));
	}
	
	/**
	 * Create row with zeros.
	 *  
	 * @param length row length.
	 * @return row with zeros.
	 */
	private ArrayList<String> createRowWithZeros(int length) {
		ArrayList<String> array = new ArrayList<>();
		
		for(int i = 0; i < length; i++) {
			array.add("0");
		}
		
		return array;
	}
	
	/**
	 * Initializes the Qtable with zeros.
	 */
	private void initializesWithZeros() {
		for(int i = 0; i < states.size(); i++) {
			addRow(createRowWithZeros(actions.size())); 		
		}
	}
	
	/**
	 * Create row with random values.
	 *  
	 * @param length row length.
	 * @return row with random values.
	 */
	private ArrayList<String> createRowWithRandoms(int length) {
		ArrayList<String> array = new ArrayList<>();
		Random rd = new Random();
		
		for(int i = 0; i < length; i++) {
			array.add(Float.toString(rd.nextFloat()));
		}
		
		return array;
	}
	
	/**
	 * Initializes the Qtable with random values.
	 */
	private void initializesWithRandoms() {
		for(int i = 0; i < states.size(); i++) {
			addRow(createRowWithRandoms(actions.size())); 		
		}
	}
	
	/**
	 * Get max Qvalue.
	 * 
	 * @param s state to index row.
	 * @return max Qvalue.
	 */
	public float getMaxQValue(State s) {
		int indexRow = states.indexOf(s);
		if(indexRow == -1) {
			System.out.println("longitud= " + states.size());
			System.out.println("indexRow = " + indexRow);
			System.out.println("States = " + (State)s);
		}
		
		ArrayList<String> array = getRow(indexRow);
		float max = Float.parseFloat(array.get(0));
		
		for(int i = 1; i < array.size(); i++) {
			float value = Float.parseFloat(array.get(i));
			if (max < value) {
				max = value;
			}
		}
		return max;
	}
	
	/**
	 * Get best Action.
	 * 
	 * @param s state to index row.
	 * @return best action.
	 */
	public ACTIONS getBestAction(State s) {
		int indexRow = states.indexOf(s);
		ArrayList<String> array = getRow(indexRow);
		float max = Float.parseFloat(array.get(0));
		int indexMax = 0;
		
		for(int i = 1; i < array.size(); i++) {
			float value = Float.parseFloat(array.get(i));
			if (max < value) {
				max = value;
				indexMax = i;
			}
		}

		return actions.get(indexMax);
	}
	
	/**
	 * Get a random action.
	 * 
	 * @return random action.
	 */
	public ACTIONS getRandomAction() {
    	Random rd = new Random();
    	int action = rd.nextInt(actions.size());
    	
        return actions.get(action);
	}
}
