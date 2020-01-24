package raulcastilla215alu.mytools;

import java.util.ArrayList;

/**
 * Defines a Q-Table state.
 * 
 * @author Raul Castilla Bravo
 */
public class State {
	
	/**
	 * Private attributes
	 */
	protected int orientation;
	protected int displacement;
	protected int compass;
	protected int fast;
	
	public static final int NUMATTRIBUTES = 4;
	public static final int POSORIENTATION = 0;
	public static final int POSDISPLACEMENT = 1;
	public static final int POSCOMPASS = 2;
	public static final int POSFAST = 3;
	
	public static final int NUMCOMPASSVALUES = 4;
	public static final int NORTH = 0;
	public static final int SOUTH = 1; 
	public static final int EAST = 2;
	public static final int WEST = 3;
	
	public static final int NUMZONEVALUES = 5;
	public static final int CENTRALGREENZONE = 4;
	public static final int LEFTGREENZONE = 5;
	public static final int RIGHTGREENZONE = 6;
	public static final int LEFTREDZONE = 7;
	public static final int RIGHTREDZONE = 8;
	
	public static final int NUMBOOLEANVALUES = 2;
	public static final int FALSE = 0;
	public static final int TRUE = 1;
	
	public static final int NONE = -1;
	public static final int ERROR = -99999;
	
	private String errorMessage = "Not a valid value";
	
	
	/**
	 * Default constructor.
	 */
	public State() {
		
	}
	
	
	/**
	 * Copy constructor.
	 * 
	 * @param obj object to be copied.
	 */
	public State(State obj) {
		this.orientation = obj.orientation;
		this.displacement = obj.displacement;
		this.compass = obj.compass;
		this.fast = obj.fast;
	}
	
	
	/**
	 * Constructor. 
	 * 
	 * @param array Private attributes values expressed with integers.
	 */
	public State(ArrayList<Integer> array) {
		update(array);		
	}
	
	
	/**
	 * Updates private attributes values.
	 * 
	 * @param array Updates private attributes values.
	 */
	protected void update(ArrayList<Integer> array) {
		this.orientation = array.get(POSORIENTATION);
		this.displacement = array.get(POSDISPLACEMENT);
		this.compass = array.get(POSCOMPASS);
		this.fast = array.get(POSFAST);
	}
	
	
	/**
	 * Returns true if the attributes are exactly the same.
	 */
	@Override
	public boolean equals(Object obj) {
		State aux = (State) obj;
		return (this.orientation == aux.orientation &&
				this.displacement == aux.displacement &&
				this.compass == aux.compass &&
				this.fast == aux.fast);
	}

	
	/**
	 * Returns a String with the information of the object.
	 */
	@Override
	public String toString() {
		String str = "";
		
		str += "Orientation = " + orientationToString(this.orientation) + "\n" +
			   "Displacement = " + orientationToString(this.displacement) + "\n" +
			   "Compass = " + compassToString(this.compass) + "\n" + 
			   "Fast = " + booleanToString(this.fast) + "\n"; 
		
		return str;
	}
	
	
	/**
	 * Return a String with the semantic value associated to orientation.
	 * 
	 * @param value Orientation value.
	 * @return String with the semantic value associated to orientation.
	 */
	private String orientationToString(int value) {
		switch(value) {
			case CENTRALGREENZONE: return "Central green zone";
			case LEFTGREENZONE: return "Left green zone";
			case RIGHTGREENZONE: return "Right green zone";
			case LEFTREDZONE: return "Left red zone";
			case RIGHTREDZONE: return "Right red zone";
		}
		return errorMessage;
	}
	
	
	/**
	 * Return a String with the semantic value associated to compass.
	 * 
	 * @param value Compass value.
	 * @return String with the semantic value associated to compass.
	 */
	private String compassToString(int value) {
		switch(value) {
			case NORTH: return "North";
			case SOUTH: return "South";
			case EAST: return "East";
			case WEST: return "West";
		}
		return errorMessage;
	}
	
	
	/**
	 * Return a String with the semantic value associated to a boolean.
	 * 
	 * @param value Fast value.
	 * @return String with the semantic value associated to boolean.
	 */
	private String booleanToString(int value) {
		switch(value) {
			case TRUE: return "true";
			case FALSE: return "false";
		}
		return errorMessage;
	}
}
