package raulcastilla215alu.mytools;

import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types.ACTIONS;
import tools.Vector2d;

/**
 * Defines an agent state.
 * 
 * @author Raul Castilla Bravo.
 *
 */
public class AgentState extends State {

	/**
	 * Private attributes.
	 */
	private Vector2d agentCellPos;
	private Vector2d portalCellPos;
	private Vector2d agentRealPos;
	private Vector2d portalRealPos;
	private float orientationRad;
	
	private boolean agentDead;
	private boolean agentWinner;
	private int blockSize;
	private double score;
	
	private float angle_diff = 0.2f;
	private float speed_limit = 5.5f;     //9.35f;
	private int lengthOfVision = 3;
	private Observation nearestWall;
	
	public static final int ITYPEPORTAL = 2; //itype of portal.
	public static final int ITYPEPLAYER = 1; //itype of player.
	public static final int ITYPEBLOCK = 0;
	
	public static final int ANGLECENTRALGREENZONE = 30;
	public static final int ANGLELEFTGREENZONE = 30;
	public static final int ANGLERIGHTGREENZONE = 30;
	
	private static final int UPPER = 0;
	private static final int DOWN = 1;
	
	private static final int LEFT = 0;
	private static final int RIGHT = 1;
	
	public static final int AXISX = 0;
	public static final int AXISY = 1;
	
	public static final int DEFAULTWALLDISTANCE = 10;
	
	/**
	 * Constructor.
	 * @param stateObs game observations.
	 */
	public AgentState(StateObservation stateObs) {
		orientationRad = 0;
		blockSize = stateObs.getBlockSize();
		score = stateObs.getGameScore();
		perceive(stateObs);
	}
	
	
	/**
	 * Copy constructor.
	 * 
	 * @param obj object to be copied.
	 */
	public AgentState(AgentState obj) {
		super(obj);
		
		this.agentCellPos = new Vector2d(obj.agentCellPos);
		if(obj.portalCellPos != null) {
			this.portalCellPos = new Vector2d(obj.portalCellPos);
		}  
		
		this.agentRealPos = new Vector2d(obj.agentRealPos);
		if(obj.portalRealPos != null) {
			this.portalRealPos = new Vector2d(obj.portalRealPos);
		}
		
		this.orientationRad = obj.orientationRad;
		this.score = obj.score;
		this.blockSize = obj.blockSize;
		this.agentDead = obj.agentDead;
	}
	
	
	/**
	 * Interprets game informations.
	 * 
	 * @param stateObs game observations.
	 */
	public void perceive(StateObservation stateObs) {
		
		// AgentState attributes
		agentRealPos = stateObs.getAvatarPosition();
		agentCellPos = calculateCell(agentRealPos, stateObs.getBlockSize());	
		score = stateObs.getGameScore();
		agentDead = false;
		agentWinner = false;
		if(this.portalCellPos == null) {
			updatePortalPos(stateObs);
		}
		
		// Initialize arrayStateValues
		ArrayList<Integer> arrayStateValues = new ArrayList<>();
		for(int i = 0; i < State.NUMATTRIBUTES; i++) {
			arrayStateValues.add(0);
		}
		
		// Perceive orientation
		updateOrientation(stateObs);
		arrayStateValues.set(State.POSORIENTATION, perceiveOrientation());
		
		// Perceive displacement
		arrayStateValues.set(State.POSDISPLACEMENT, perceiveDisplacement(stateObs));
		
		// Perceive compass
		int compassValue = perceiveCompass();
		compassValue = correctCompass(stateObs, compassValue);
		arrayStateValues.set(State.POSCOMPASS, compassValue);
		
		// Perceive fast
		arrayStateValues.set(State.POSFAST, perceiveFast(stateObs));
		
		correctCompass(stateObs, 0);
		
		super.update(arrayStateValues);
	}
	
	
	/**
	 * Increase o decrease the orientationRad attribute
	 * taking into account if the last action was LEFT or
	 * RIGHT.
	 * 
	 * @param stateObs Game observations.
	 */
	private void updateOrientation(StateObservation stateObs) {
		ACTIONS lastAction = stateObs.getAvatarLastAction();
		if(lastAction.equals(ACTIONS.ACTION_LEFT)) {
			orientationRad += angle_diff;
		}
		if(lastAction.equals(ACTIONS.ACTION_RIGHT)) {
			orientationRad -= angle_diff;
		}
	}
	
	
	/**
	 * Look in game grid to find portal positions.
	 * 
	 * @param stateObs Game observations.
	 * @return All portal observations.
	 */
	private ArrayList<Observation> findPortals(StateObservation stateObs){
		ArrayList<Observation> output = new ArrayList<>();
		ArrayList<Observation>[][] grid = stateObs.getObservationGrid();
		Observation obs;
		ArrayList<Observation> arrayObs;
		
		for(int i = 0; i < grid.length; i++) {
			for(int j = 0; j < grid[0].length; j++) {
				arrayObs = grid[i][j];
				if(!arrayObs.isEmpty()) {
					obs = arrayObs.get(0);
					if(obs.itype == ITYPEPORTAL) {
						output.add(obs);
					}
				}

			}
		}
		return output;
	}
	
	
	/**
	 * Get the nearest observation to the agent.
	 * 
	 * @param obs Array of game observations.
	 * @return the nearest observation to the agent.
	 */
	private Observation getNearest(ArrayList<Observation> obs) {
		Vector2d currentCellPos;
		float currentDistance;
		Observation nearestObs = obs.get(0);
		float nearestDistance = 100000;
		
		for(int i = 0; i < obs.size(); i++) {
			if(obs.get(i) != null) {
				currentCellPos = calculateCell(obs.get(i).position, this.blockSize);
				currentDistance = distance(this.agentCellPos, currentCellPos);
				if(currentDistance < nearestDistance) {
					nearestDistance = currentDistance;
					nearestObs = obs.get(i);
				}
			}
		}
		return nearestObs;
	}
	
	
	/**
	 * Return true if both Observations are neighbors
	 * @param obsA Observation
	 * @param obsB Observation
	 * @param axis LEFT to check if obsA is leftNeighbor of obsB or RIGHT in the other hand.
	 * @return True if both Observations are neighbors.
	 */
	private boolean isNeighbor(Observation obsA, Observation obsB, int axis) {
		Vector2d posA = calculateCell(obsA.position, blockSize);
		Vector2d posB = calculateCell(obsB.position, blockSize);
		
		if(axis == LEFT) {
			return (posA.y == posB.y) && (posA.x+1 == posB.x);
		} else {
			return (posA.y == posB.y) && (posA.x-1 == posB.x);
		}
		
	}
	
	
	/**
	 * Return an array with Observations which are neighbors of each others.
	 * 
	 * @param arrayObs Array of Observations.
	 * @param obs Seed of the neighborhood.
	 * @return Array with Observations which are neighbors of each others.
	 */
	private ArrayList<Observation> getNeighbors(ArrayList<Observation> arrayObs, Observation obs){
		ArrayList<Observation> arrayNeighbors = new ArrayList<>();
		arrayNeighbors.add(obs);
		return getNeighborsRec(arrayObs, arrayNeighbors);
	}
	
	
	/**
	 * Recursive method for getNeighbors.
	 * 
	 * @param arrayObs Array of Observations.
	 * @param obs Seed of the neighborhood.
	 * @return Array with Observations which are neighbors of each others.
	 */
	private ArrayList<Observation> getNeighborsRec(ArrayList<Observation> arrayObs, 
			                                       ArrayList<Observation> arrayNeighbors){
		boolean modified = false;
		Observation o;
		
		// Left part
		Observation leftNeighbor = arrayNeighbors.get(0);
		for(int i = 0; i < arrayObs.size(); i++) {
			o = arrayObs.get(i);
			if(isNeighbor(o, leftNeighbor, LEFT)) {
				arrayNeighbors.add(0, o);
				arrayObs.remove(o);
				modified = true;
			}
		}
		
		// Right part
		Observation rightNeighbor = arrayNeighbors.get(arrayNeighbors.size()-1);
		for(int i = 0; i < arrayObs.size(); i++) {
			o = arrayObs.get(i);
			if(isNeighbor(o, rightNeighbor, RIGHT)) {
				arrayNeighbors.add(o);
				arrayObs.remove(o);
				modified = true;
			}
		}
		
		if(modified) {
			return getNeighborsRec(arrayObs, arrayNeighbors);
		} else {
			return arrayNeighbors;
		}
	}
	
	
	/**
	 * Return the middle neighbor of the neighborhood.
	 * 
	 * @param arrayNeighbors Array of Observations
	 * @return middle neighbor.
	 */
	private Observation getMiddleNeighbor(ArrayList<Observation> arrayNeighbors) {
		int index = Math.round(arrayNeighbors.size()/2-1);
		return arrayNeighbors.get(index);
	}
	

	/**
	 * Update portalCellPos with the nearest portal observation.
	 * 
	 * @param stateObs Game observations.
	 */
	private void updatePortalPos(StateObservation stateObs) {
		ArrayList<Observation> portals = findPortals(stateObs);
		Observation nearestPortal = getNearest(portals);
		ArrayList<Observation> arrayNeighbors = getNeighbors(portals, nearestPortal);
		this.portalRealPos = getMiddleNeighbor(arrayNeighbors).position;
		this.portalCellPos = calculateCell(portalRealPos, blockSize);
	}
	
	
	/**
	 * Calculate the current orientation. 
	 * @return current orientation. 
	 */
	private int perceiveOrientation() {
		float degrees = (float) (Math.toDegrees(orientationRad)%360);
		if(degrees < 0) {
			degrees += 360;
		}
		return degreesToRegion(degrees, UPPER);
	}
	
	
	/**
	 * Calculate the angle which forms the vector introduced
	 * with the X axis (angle 0º).
	 * 
	 * @param x coordinate of axis x.
	 * @param y coordinate of axis y.
	 * @return Angle in degrees.
	 */
	private float calculateDegreesFromVector(float x, float y) {
		
		if(x == 0) {
			if(y > 0) return 90;
			else return 270;
		}
		
		float value =  (float)Math.toDegrees(Math.atan(y/x));
		
		if(x < 0 && y > 0) return value + 180;
		if(x < 0 && y < 0) return value + 180;
		if(x > 0 && y < 0) return value + 360;
		
		return value;
	}
	
	
	/**
	 * Calculate the current displacement.
	 * 
	 * @param stateObs Game observations.
	 * @return current displacement.
	 */
	private int perceiveDisplacement(StateObservation stateObs) {
		Vector2d dir = stateObs.getAvatarOrientation();
		dir.set(dir.x, -dir.y);
		
		float degrees = calculateDegreesFromVector((float)dir.x, (float)dir.y);
		return degreesToRegion(degrees, DOWN);
	}
	
	
	/**
	 * Cast from integer to the region associated to angles.
	 * 
	 * @param degrees Angle in degrees.
	 * @return region associated to the angle.
	 */
	private int degreesToRegion(float degrees, int axis) {
		
		int upperValue = 90;
		int downValue = 270;
		
		// Origin angle system value
		float initialValue = (axis == UPPER ? upperValue : downValue);
		
		// Initial and final value of each region
		float iniCentralGreenZone = initialValue - ANGLECENTRALGREENZONE/2;
		float finCentralGreenZone = initialValue + ANGLECENTRALGREENZONE/2;
		
		float iniLeftGreenZone, finLeftGreenZone, iniRightGreenZone, finRightGreenZone;
		
		if (axis == UPPER) {
			iniLeftGreenZone = finCentralGreenZone;
			finLeftGreenZone = iniLeftGreenZone + ANGLELEFTGREENZONE;
			
			finRightGreenZone = iniCentralGreenZone;
			iniRightGreenZone = finRightGreenZone - ANGLERIGHTGREENZONE;
		} else {
			finLeftGreenZone = iniCentralGreenZone;
			iniLeftGreenZone = finLeftGreenZone - ANGLELEFTGREENZONE;
			
			iniRightGreenZone = finCentralGreenZone;
			finRightGreenZone = iniRightGreenZone + ANGLERIGHTGREENZONE;
		}

		// Check region
		if(axis == UPPER) {
			if(degrees < iniRightGreenZone || degrees > downValue) return State.RIGHTREDZONE;
			if(degrees > finLeftGreenZone) return State.LEFTREDZONE;
		} else {
			if(degrees > finRightGreenZone || degrees < upperValue) return State.RIGHTREDZONE;
			if(degrees < iniLeftGreenZone) return State.LEFTREDZONE;
		}
		
		if(finRightGreenZone   >= degrees && degrees >= iniRightGreenZone)   return State.RIGHTGREENZONE;
		if(finCentralGreenZone >= degrees && degrees >= iniCentralGreenZone) return State.CENTRALGREENZONE;
		if(finLeftGreenZone    >= degrees && degrees >= iniLeftGreenZone)    return State.LEFTGREENZONE;
		
		return -1;
	}
	
	
	/**
	 * Calculate the current compass orientation
	 * 
	 * @return current compass orientation.
	 */
	private int perceiveCompass() {
		if(this.portalCellPos == null) return State.SOUTH;
		
		if(overPosition(agentCellPos, portalCellPos)) return State.SOUTH;
		if(underPosition(agentCellPos, portalCellPos)) return State.NORTH;
		if(leftPosition(agentCellPos, portalCellPos)) return State.EAST;
		if(rightPosition(agentCellPos, portalCellPos)) return State.WEST;
		
		return State.SOUTH;
	}
	
	
	/**
	 * Create an ArrayList of Observation but each element of
	 * the array is null.
	 * 
	 * @param length Number of array elements.
	 * @return ArrayList of null.
	 */
	private ArrayList<Observation> createNullArrayObservation(int length){
		ArrayList<Observation> arrayObs = new ArrayList<>();
		
		for(int i = 0; i < length; i++) {
			arrayObs.add(null);
		}
		
		return arrayObs;
	}
	
	
	/**
	 * Get a row of the grid game. If index row is negative or greater 
	 * than the number of rows, it returns the first or the last row 
	 * respectively.
	 * 
	 * @param stateObs Game observation.
	 * @param indexRow Row index.
	 * @return Row of the grid game.
	 */
	private ArrayList<Observation> getRow(StateObservation stateObs, int indexRow){
		ArrayList<Observation>[][] grid = stateObs.getObservationGrid();
		ArrayList<Observation> row = new ArrayList<>();
		ArrayList<Observation> aux;
		
		if(indexRow >= grid[0].length) return createNullArrayObservation(grid.length);
		if(indexRow < 0) indexRow = 0;
		
		for(int i = 0; i < grid.length; i++) {
			aux = grid[i][indexRow];
			if(!aux.isEmpty()) {
				row.add(aux.get(0));
			} else {
				row.add(null);
			}
		}
		return row;
	}
	
	
	/**
	 * Column a row of the grid game. If index column is negative or greater 
	 * than the number of columns, it returns the first or the last column 
	 * respectively.
	 * 
	 * @param stateObs Game observation.
	 * @param indexColumn column index.
	 * @return Column of the grid game.
	 */
	private ArrayList<Observation> getColumn(StateObservation stateObs, int indexColumn){
		ArrayList<Observation>[][] grid = stateObs.getObservationGrid();
		ArrayList<Observation> column = new ArrayList<>();
		ArrayList<Observation> aux;
		
		if(indexColumn >= grid.length) return createNullArrayObservation(grid[0].length);
		if(indexColumn < 0) indexColumn = 0;
		
		for(int i = 0; i < grid[0].length; i++) {
			aux = grid[indexColumn][i];
			if(!aux.isEmpty()) {
				column.add(aux.get(0));
			} else {
				column.add(null);
			}
		}
		return column;
	}
	
	
	/**
	 * Complete arrayA where arrayA has null values with the value of arrayB 
	 * in that position.
	 * 
	 * @param arrayA Array of observations.
	 * @param arrayB Array of observations which is projected.
	 * @return Array with the combination of the two arrays.
	 */
	private ArrayList<Observation> project(ArrayList<Observation> arrayA, ArrayList<Observation> arrayB){
		for(int i = 0; i < arrayA.size(); i++) {
			if(arrayA.get(i) == null) {
				arrayA.set(i, arrayB.get(i));
			}
		}
		return arrayA;
	}
	
	
	private void removeAgentObservation(ArrayList<Observation> obs) {
		for(int i = 0; i < obs.size(); i++) {
			if(obs.get(i) != null && obs.get(i).itype == ITYPEPLAYER) {
				obs.set(i, null);
			}
		}
	}
	
	/**
	 * Projects a range of rows or columns of the grid game in a single array.
	 * 
	 * @param stateObs Game observation
	 * @param ini Initial value of the range.
	 * @param fin Final value of the range. This index is included in the projection.
	 * @param axis AXISY to project rows and AXISX to project columns.
	 * @return Projection of the range of rows or columns.
	 */
	private ArrayList<Observation> searchWalls(StateObservation stateObs, int ini, int fin, int axis){
		
		ArrayList<Observation> aux;
		ArrayList<Observation> currentWall;
		
		if(ini < 0) ini = 0;
		
		if(axis == AXISY) {
			currentWall = getRow(stateObs, ini);
			removeAgentObservation(currentWall);
			
			for(int i = ini+1; i <= fin; i++) {
				aux = getRow(stateObs, i);
				currentWall = project(currentWall, aux);
				removeAgentObservation(currentWall);
			}
		} else {
			currentWall = getColumn(stateObs, ini);
			removeAgentObservation(currentWall);
			
			for(int i = ini+1; i <= fin; i++) {
				aux = getColumn(stateObs, i);
				currentWall = project(currentWall, aux);
				removeAgentObservation(currentWall);
			}
		}

		return currentWall;
	}
	
	
	/**
	 * Return true if the position introduced has a wall observation near.
	 * 
	 * @param arrayObs Array of Observation.
	 * @param pos Position under study.
	 * @param axis AXISY if the Array of Observation is a projection of rows or AXISX 
	 *             if it is a projection of columns.
	 * @return True if the position introduced has a wall observation near.
	 */
	int contador = 0;
	private boolean isCollisionPossible(ArrayList<Observation> arrayObs, Vector2d pos, int axis) {
		int refValue = 0;
		
		if(axis == AXISY) {
			refValue = (int)pos.x;
		} else {
			refValue = (int)pos.y;
		}
		
		int pos1 = (int)(refValue-1 > 0 ? refValue-1 : 0);
		int pos2 = (int)refValue;
		int pos3 = (int)(refValue+1 < arrayObs.size() ? refValue+1 : refValue);
		
		int itype1 = (arrayObs.get(pos1) != null ? arrayObs.get(pos1).itype: -1);
		int itype2 = (arrayObs.get(pos2) != null ? arrayObs.get(pos2).itype: -1);
		int itype3 = (arrayObs.get(pos3) != null ? arrayObs.get(pos3).itype: -1);
		
//		boolean output = itype1 == ITYPEBLOCK || itype2 == ITYPEBLOCK || itype3 == ITYPEBLOCK;
//		if(contador >= 40) {
//			System.out.println("True");
//		} contador++;
		
		return itype1 == ITYPEBLOCK || itype2 == ITYPEBLOCK || itype3 == ITYPEBLOCK;
	}
	
	
	/**
	 * Change the compass value if a collision is possible.
	 * 
	 * @param stateObs Game observation. 
	 * @param currentCompass Current compass value.
	 * @return Compass value corrected.
	 */
	private int correctCompass(StateObservation stateObs, int currentCompass) {
		if(currentCompass == State.WEST || currentCompass == State.EAST) {
			int iniDown = (int)agentCellPos.y;
			int finDown = iniDown + lengthOfVision; 
			
			ArrayList<Observation> downWalls = searchWalls(stateObs, iniDown, finDown, AXISY);
//			for(int i = 0; i < downWalls.size(); i++) {
//				System.out.print((downWalls.get(i) != null ? "B" : "_"));
//			}
//			System.out.println("");
			this.nearestWall = null;
			if(isCollisionPossible(downWalls, agentCellPos, AXISY)) {
				this.nearestWall = getNearest(downWalls);
				return State.NORTH;
			}
		}
		
		return currentCompass;
	}
	
	
	public float distanceToNearestWall() {
		if(this.nearestWall == null) return DEFAULTWALLDISTANCE;
		
		return distance(this.agentRealPos, this.nearestWall.position);
	}
	
	
	/**
	 * Check if the agent has followed compass recommendations.
	 * 
	 * @param previousState Previous agent state.
	 * @param currentState Current agent state.
	 * @param previousCompass Previous compass value.
	 * @return TRUE, FALSE o NONE.
	 */
	public static int obeyCompass(AgentState previousState, AgentState currentState, int previousCompass) {
		Vector2d previousPos = previousState.agentCellPos;
		Vector2d currentPos = currentState.agentCellPos;
		
		int difx = (int)(currentPos.x - previousPos.x);
		int dify = (int)(currentPos.y - previousPos.y);
		
		
		switch (previousCompass) {
			case State.NORTH:
				if(dify == 0) return State.NONE;
				if(dify < 0) {
					System.out.println("*********************************** Obedece NORTE *****************************");
					return State.TRUE;
				}
				if(dify > 0) return State.FALSE;
			case State.SOUTH:
				if(dify == 0) return State.NONE;
				if(dify < 0) return State.FALSE;
				if(dify > 0) return State.TRUE;
			case State.WEST:
				if(difx == 0) return State.NONE;
				if(difx < 0) return State.TRUE;
				if(difx > 0) return State.FALSE;
			case State.EAST:
				if(difx == 0) return State.NONE;
				if(difx < 0) return State.FALSE;
				if(difx > 0) return State.TRUE;
			
		}
		return State.ERROR;
	}
	
	
	/**
	 * Return true if A is over B.
	 * @param posA Left element of the comparison.
	 * @param posB Right element of the comparison.
	 * @return true if A is over B.
	 */
	private static boolean overPosition(Vector2d posA, Vector2d posB) {
		return posA.x == posB.x && posA.y < posB.y;
	}
	
	
	/**
	 * Return true if A is under B.
	 * @param posA Left element of the comparison.
	 * @param posB Right element of the comparison.
	 * @return true if A is under B.
	 */
	private static boolean underPosition(Vector2d posA, Vector2d posB) {
		return posA.x == posB.x && posA.y > posB.y;
	}
	
	
	/**
	 * Return true if A is left B.
	 * @param posA Left element of the comparison.
	 * @param posB Right element of the comparison.
	 * @return true if A is left B.
	 */
	private static boolean leftPosition(Vector2d posA, Vector2d posB) {
		return posA.x < posB.x;
	}
	
	
	/**
	 * Return true if A is right B.
	 * @param posA Left element of the comparison.
	 * @param posB Right element of the comparison.
	 * @return true if A is right B.
	 */
	private static boolean rightPosition(Vector2d posA, Vector2d posB) {
		return posA.x > posB.x;
	}
	
	
	/**
	 * Check if the avatar speed is over a limit speed.
	 * 
	 * @param stateObs Game observations.
	 * @return True if the avatar moves fast.
	 */
	private int perceiveFast(StateObservation stateObs) {
		return (stateObs.getAvatarSpeed() > speed_limit ? State.TRUE : State.FALSE);
	}
	
	
	/**
	 * Cast the position expressed in reals values to position expressed in cell coordinates.
	 * 
	 * @param pos position expressed in reals values.
	 * @param blockSize reference measure to cast positions.
	 * @return position expressed in cell coordinates.
	 */
	public static Vector2d calculateCell(Vector2d pos, int blockSize) {
		Vector2d cellCoords = new Vector2d();
		
		int x = (int) (pos.x/blockSize);
		int y = (int) (pos.y/blockSize);
		
		cellCoords.set(x, y);
		
		return cellCoords;
	}
	
	
	/**
	 * Calculate the euclidean distance between two points.
	 * 
	 * @param posA point A.
	 * @param posB point B.
	 * @return euclidean distance between the two points.
	 */
	public static float distance(Vector2d posA, Vector2d posB) {
		
		float difX = (float)(posA.x - posB.x);
		float difY = (float)(posA.y - posB.y);
		
		return (float)Math.sqrt(Math.pow(difX, 2) + Math.pow(difY, 2));
	}
	
	
	/**
	 * @return Manhattan distance between agent and nearest portal.
	 */
	public float distanceToPortal(int axis) {
		if(axis == AXISX)
			return (float)Math.abs(this.agentRealPos.x - this.portalRealPos.x);
		else
			return (float)Math.abs(this.agentRealPos.y - this.portalRealPos.y);
	}
	
	
	/**
	 * Returns a String with the information of the Object.
	 */
	@Override
	public String toString() {
		String str = super.toString();
		str +=  "Agent position = " + agentCellPos.toString() + "\n" + 
				"Portal position = ";
		
		if(portalCellPos == null) {
			str += "null" + "\n";
		} else {
			str += portalCellPos.toString() + "\n";
		}

		str += "Orientation value = " + (float)Math.toDegrees(this.orientationRad) + "\n" +
				"************************** \n\n";
		
		return str;
	}

	
	/**
	 * Set the agentDead value.
	 * @param value agentDead value.
	 */
	public void setAgentDead(boolean value) {
		this.agentDead = value;
	}
	
	
	/**
	 * Set the agentWinner value.
	 * @param value agentWinner value.
	 */
	public void setAgentWinner(boolean value) {
		this.agentWinner = value;
	}
	
	
	/**
	 * Return true if the agent has won.
	 * @return true if the agent has won.
	 */
	public boolean isAgentWinner() {
		return this.agentWinner;
	}
	
	
	/**
	 * Return true if the agent is dead.
	 * @return true if the agent is dead.
	 */
	public boolean isAgentDead() {
		return agentDead;
	}
	
	
	/**
	 * @return true if the portal exist.
	 */
	public boolean portalExist() {
		return portalCellPos != null;
	}
	
	
	/**
	 * @return current score.
	 */
	public double getScore() {
		return score;
	}
	

	/**
	 * @return agent position in cell coordinates.
	 */
	public Vector2d getAgentPos() {
		return agentCellPos;
	}
	
	
	/**
	 * @return True if the agents moves fast.
	 */
	public boolean isFast() {
		return fast == TRUE;
	}
	
	
	/**
	 * True if the orientation is in left, central or right zone.
	 * 
	 * @return True if the orientation is in left, central or right zone.
	 */
	public boolean isOrientationInGreenZone() {
		return orientation == State.CENTRALGREENZONE ||
			   orientation == State.LEFTGREENZONE ||
			   orientation == State.RIGHTGREENZONE;
	}
	
	
	/**
	 * True if the displacement is in left, central or right zone.
	 * 
	 * @return True if the displacement is in left, central or right zone.
	 */
	public boolean isDisplacementInGreenZone() {
		return displacement == State.CENTRALGREENZONE || 
			   displacement == State.LEFTGREENZONE ||
			   displacement == State.RIGHTGREENZONE;
	}
	
	
	/**
	 * True if the agent is over the portal.
	 * 
	 * @return True if the agent is over the portal.
	 */
	public boolean isAgentOverPortal() {
		return overPosition(this.agentCellPos, this.portalCellPos);
	}
	
	
	/**
	 * Return compass value.
	 * 
	 * @return Compass value.
	 */
	public int getCompass() {
		return this.compass;
	}
	
	
	/*
	public static void testDisplacement() {
		float x;
		float y;
		
		// Primer cuadrante
		x = 0.5f;
		y = 0.5f;
		System.out.println("x = " + x + " y = " + y + "(45) -> " + calculateDegreesFromVector(x, y));
		
		x = 0.5f;
		y = 0.289f;
		System.out.println("x = " + x + " y = " + y + "(30) -> " + calculateDegreesFromVector(x, y));
		
		x = 0.289f;
		y = 0.5f;
		System.out.println("x = " + x + " y = " + y + "(60) -> " + calculateDegreesFromVector(x, y));
		System.out.println("\n");
		
		// Segundo cuadrante
		x = -0.5f;
		y = 0.5f;
		System.out.println("x = " + x + " y = " + y + "(135) -> " + calculateDegreesFromVector(x, y));
		
		x = -0.5f;
		y = 0.289f;
		System.out.println("x = " + x + " y = " + y + "(150) -> " + calculateDegreesFromVector(x, y));
		
		
		x = -0.289f;
		y = 0.5f;
		System.out.println("x = " + x + " y = " + y + "(120) -> " + calculateDegreesFromVector(x, y));
		System.out.println("\n");
		
		// Tercer cuadrante
		x = -0.5f;
		y = -0.5f;
		System.out.println("x = " + x + " y = " + y + "(225) -> " + calculateDegreesFromVector(x, y));
		
		x = -0.5f;
		y = -0.289f;
		System.out.println("x = " + x + " y = " + y + "(210) -> " + calculateDegreesFromVector(x, y));
		
		x = -0.289f;
		y = -0.5f;
		System.out.println("x = " + x + " y = " + y + "(240) -> " + calculateDegreesFromVector(x, y));
		System.out.println("\n");
		
		// Cuarto cuadrante
		x = 0.5f;
		y = -0.5f;
		System.out.println("x = " + x + " y = " + y + "(315) -> " + calculateDegreesFromVector(x, y));
		
		x = 0.5f;
		y = -0.289f;
		System.out.println("x = " + x + " y = " + y + "(330) -> " + calculateDegreesFromVector(x, y));
		
		x = 0.289f;
		y = -0.5f;
		System.out.println("x = " + x + " y = " + y + "(300) -> " + calculateDegreesFromVector(x, y));
		System.out.println("\n");
	}
	

	
	public static void main(String[] args) {
		testDisplacement();
	}
	 */
}
