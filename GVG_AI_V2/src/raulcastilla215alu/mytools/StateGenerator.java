package raulcastilla215alu.mytools;

import java.util.ArrayList;

/**
 * Generates all possible and logical agent state.
 * 
 * @author Raul Castilla Bravo.
 */
public class StateGenerator {
	
	/**
	 * Generates all possible and logical agent state.
	 * @return An array of states.
	 */
	public static ArrayList<State> generate() {
		int[] zoneValues = new int[State.NUMZONEVALUES];
		zoneValues[0] = State.CENTRALGREENZONE;
		zoneValues[1] = State.LEFTGREENZONE;
		zoneValues[2] = State.RIGHTGREENZONE;
		zoneValues[3] = State.LEFTREDZONE;
		zoneValues[4] = State.RIGHTREDZONE;
		
		int[] compassValues = new int[State.NUMCOMPASSVALUES];
		compassValues[0] = State.NORTH;
		compassValues[1] = State.SOUTH;
		compassValues[2] = State.EAST;
		compassValues[3] = State.WEST;
		
		int[] booleanValues = new int[State.NUMBOOLEANVALUES];
		booleanValues[0] = State.FALSE;
		booleanValues[1] = State.TRUE;
		
		// Generation of orientation and displacement
		ArrayList<ArrayList<Integer>> combStates = combnk(2, zoneValues);
		
		// Generation of compass values
		combStates = addIntegerToCombination(combStates, compassValues);
		
		// Generation of fast values
		combStates = addIntegerToCombination(combStates, booleanValues);

		return toStates(combStates);		
	}
	
	
	/**
	 * Add a new value to all combination.
	 * 
	 * @param combStates combinations values.
	 * @param values to be added.
	 * @return combinations with the new values.
	 */
	@SuppressWarnings("unchecked")
	private static ArrayList<ArrayList<Integer>> addIntegerToCombination(ArrayList<ArrayList<Integer>> combStates, int[] values) {
		ArrayList<ArrayList<Integer>> output = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> aux;
		
		for(int indexValues = 0; indexValues < values.length; indexValues++) {
			for(int indexArray = 0; indexArray < combStates.size(); indexArray++) {
				aux = (ArrayList<Integer>) combStates.get(indexArray).clone();
				aux.add(values[indexValues]);
				output.add(aux);
			}
		}
		
		return output;
	}
	
	/**
	 * Create states for each combination.
	 * 
	 * @param combStates combinations values.
	 * @return states.
	 */
	@SuppressWarnings("unchecked")
	private static ArrayList<State> toStates(ArrayList<ArrayList<Integer>> combStates) {
		ArrayList<State> output = new ArrayList<State>();
		ArrayList<Integer> aux;
		
		for(int indexArray = 0; indexArray < combStates.size(); indexArray++) {
			aux = (ArrayList<Integer>) combStates.get(indexArray).clone();
			output.add(new State(aux));
		}

		return output;
	}
	
	
	/**
	 * Generates all possible combinations using the values and length specified.
	 * 
	 * @param length size of combination.
	 * @param values possible values for each positions.
	 * @return an array of arrays of integers.
	 */
	private static ArrayList<ArrayList<Integer>> combnk(int length, int[] values) {
		return combnkRec(length-1, values, new ArrayList<ArrayList<Integer>>());
	}
	
	/**
	 * Recursive call for combnk.
	 * 
	 * @param length size of combination.
	 * @param values possible values for each positions.
	 * @param array memory space to save middle values.
	 * @return an array of arrays of integers.
	 */
	@SuppressWarnings("unchecked")
	private static ArrayList<ArrayList<Integer>> combnkRec(int length, int[] values, ArrayList<ArrayList<Integer>> array){
		if(length == 0) {
			for(int indexValues = 0; indexValues < values.length; indexValues++) {
				array.add(new ArrayList<Integer>());
				array.get(indexValues).add(values[indexValues]);
			}
			return array;
		}
		ArrayList<ArrayList<Integer>> aux = new ArrayList<ArrayList<Integer>>();
		aux = combnkRec(length - 1, values, array);
				
		ArrayList<ArrayList<Integer>> output = new ArrayList<ArrayList<Integer>>();
		for(int indexValue = 0; indexValue < values.length; indexValue++) {
			for(int indexArray = 0; indexArray < aux.size(); indexArray++) {
				ArrayList<Integer> subArray = (ArrayList<Integer>) aux.get(indexArray).clone();	
				subArray.add(values[indexValue]);
				output.add(subArray);
			}
		}
		return output;				
	}
	
	/**
	 * Removes illogical states.
	 * 
	 * @param combStates an array of arrays of integers with all possible combinations.
	 */
	private static void filterStates(ArrayList<ArrayList<Integer>> combStates){
		for(int i = 0; i < combStates.size(); i++ ) {
			if(!isValid(combStates.get(i))) {
				combStates.remove(i);
				i--;
			}
		}
	}

	/**
	 * Applies restrictions to filter a state.
	 * 
	 * @param comb array of integers with one combination of values.
	 * @return true if overcomes all restrictions.
	 */
	private static boolean isValid(ArrayList<Integer> comb) {
		return true;	
	}
	
	public static void main(String[] args) {
		System.out.println("Longitud = " + StateGenerator.generate().size());
//		System.out.println(StateGenerator.generate());
	}
	
}
