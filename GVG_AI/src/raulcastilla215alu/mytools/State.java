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
	private int orientation;
	private int displacement;
	private int compassEW;
	private int compassNS;
	private boolean danger;
	private boolean fast;
	
	public static final int POSORIENTATION = 0;
	public static final int POSDISPLACEMENT = 1;
	public static final int POSCOMPASSEW = 2;
	public static final int POSCOMPASSNS = 3;
	public static final int POSDANGER = 4;
	public static final int POSFAST = 5;
	
	public static final int NORTH = 0;
	public static final int SOUTH = 1; 
	public static final int EAST = 2;
	public static final int WEST = 3;
	
	public static final int ANGLE0 = 4;
	public static final int ANGLE45 = 5;
	public static final int ANGLE90 = 6;
	public static final int ANGLE135 = 7;
	public static final int ANGLE180 = 8;
	public static final int ANGLE225 = 9;
	public static final int ANGLE270 = 10;
	public static final int ANGLE315 = 11;
	
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
		
		this.compassEW = obj.compassEW;
		this.compassNS = obj.compassNS;
		
		this.danger = obj.danger;
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
		
		this.compassEW = array.get(POSCOMPASSEW);
		this.compassNS = array.get(POSCOMPASSNS);
		
		this.danger = (array.get(POSDANGER) == 0 ? false : true);
		this.fast = (array.get(POSFAST)) == 0 ? false : true;
	}
	
	/**
	 * Returns true if the attributes are exactly the same.
	 */
	@Override
	public boolean equals(Object obj) {
		State aux = (State) obj;
		return (this.orientation == aux.orientation &&
				this.displacement == aux.displacement &&
				this.compassEW == aux.compassEW &&
				this.compassNS == aux.compassNS &&
				this.danger == aux.danger &&
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
			   "Compass EW = " + compassToString(this.compassEW) + "\n" + 
			   "Compass NS = " + compassToString(this.compassNS) + "\n" +
			   "Danger = " + Boolean.toString(this.danger) + "\n" + 
			   "Fast = " + Boolean.toString(this.fast) + "\n"; 
		
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
			case ANGLE0: return "0";
			case ANGLE45: return "45";
			case ANGLE90: return "90";
			case ANGLE135: return "135";
			case ANGLE180: return "180";
			case ANGLE225: return "225";
			case ANGLE270: return "270";
			case ANGLE315: return "315";
		}
		return "Not an orientation value";
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
		return "Not a compass value";
	}
}
