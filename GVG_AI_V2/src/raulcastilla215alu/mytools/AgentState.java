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
	
	private Vector2d orientationVector;
	private Vector2d displacementVector;
	private Vector2d goalVector;
	private float speed;
	
	private boolean agentDead;
	private boolean agentWinner;
	
	private int blockSize;
	private double score;
	
	private float angle_diff = 0.2f;
	private float min_speed_limit = 4.5f;     //9.35f;
	private float max_speed_limit = 6f;
	
	public static final int ITYPEPORTAL = 2; //itype of portal.
	public static final int ITYPEPLAYER = 1; //itype of player.
	public static final int ITYPEBLOCK = 0;
	
	public static final int ANGLECENTRALGREENZONE = 30;
	public static final int ANGLELEFTGREENZONE = 30;
	public static final int ANGLERIGHTGREENZONE = 30;
	
	private static final int LEFT = 0;
	private static final int RIGHT = 1;
	
	public static final int AXISX = 0;
	public static final int AXISY = 1;
	
	public static final int DEFAULTWALLDISTANCE = 10;
	
	public static final int STEP = 36;
	
	private Vector2d defaultVector;
	
	
	/**
	 * Constructor.
	 * @param stateObs game observations.
	 */
	public AgentState(StateObservation stateObs) {
		orientationRad = 0;
		blockSize = stateObs.getBlockSize();
		score = stateObs.getGameScore();
		
		defaultVector = new Vector2d(0,-1);

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
		
		this.orientationVector = obj.orientationVector;
		this.displacementVector = obj.displacementVector;
		this.goalVector = obj.goalVector;
		this.speed = obj.speed;
		
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
		
		speed = (float)stateObs.getAvatarSpeed();
		
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
		int displacementValue = perceiveDisplacement(stateObs);
		arrayStateValues.set(State.POSDISPLACEMENT, displacementValue);
		
		// Perceive goal
		arrayStateValues.set(State.POSGOAL, perceiveGoal(displacementValue));
		
		// Perceive fast
		arrayStateValues.set(State.POSFAST, perceiveFast(stateObs));
		
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
		int index = Math.round(arrayNeighbors.size()/2);
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
		
		double x = Math.cos(Math.toRadians(degrees));
		double y = Math.sin(Math.toRadians(degrees));
		
		this.orientationVector = new Vector2d(x,y);
		
		return (int)(degrees/STEP);
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
		this.displacementVector = stateObs.getAvatarOrientation();

		displacementVector.set(displacementVector.x, -displacementVector.y);
		
		if(displacementVector.x == 0 && displacementVector.y == 0) {
			this.displacementVector = this.defaultVector;
		}
		
		
		float degrees = calculateDegreesFromVector((float)displacementVector.x, (float)displacementVector.y);
		return (int)(degrees/STEP);
	}
	
	
	/**
	 * Return true if the displacement is orientated to the goal.
	 * 
	 * @param displacementRegion Displacement region
	 * @return True if the displacement is orientated to the goal.
	 */
	private int perceiveGoal(int displacementRegion) {
		
		double x = this.portalRealPos.x - this.agentRealPos.x;
		double y = -(this.portalRealPos.y - this.agentRealPos.y);
		
		double norma = Math.sqrt(x*x + y*y);
		x = x/norma;
		y = y/norma;
		
		this.goalVector = new Vector2d(x,y);
		
		float degrees = calculateDegreesFromVector((float)goalVector.x, (float)goalVector.y);
		int goalRegion = (int)(degrees/STEP);
	
		return (goalRegion == displacementRegion ? State.TRUE : State.FALSE);
	}
	
	/**
	 * Check if the avatar speed is over a limit speed.
	 * 
	 * @param stateObs Game observations.
	 * @return True if the avatar moves fast.
	 */
	private int perceiveFast(StateObservation stateObs) {
		if(this.fast == FALSE) 
			return (stateObs.getAvatarSpeed() > max_speed_limit ? State.TRUE : State.FALSE);
		else 
			return (stateObs.getAvatarSpeed() > min_speed_limit ? State.TRUE : State.FALSE);
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
	 * Calculate the angle between two vectors and return the result in degrees.
	 * 
	 * @param vectorA VectorA.
	 * @param vectorB VectorB.
	 * @return angle in degrees.
	 */
	public static float angleBetweenVectors(Vector2d vectorA, Vector2d vectorB) {
		double sum = vectorA.x * vectorB.x + vectorA.y * vectorB.y;
		double modA = Math.sqrt(vectorA.x*vectorA.x + vectorA.y*vectorA.y);
		double modB = Math.sqrt(vectorB.x*vectorB.x + vectorB.y*vectorB.y);
		double argument = sum/(modA*modB);
		double degrees = Math.toDegrees(Math.acos(argument));

		return (float)degrees;
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
	 * True if the agent is over the portal.
	 * 
	 * @return True if the agent is over the portal.
	 */
	public boolean isAgentOverPortal() {
		return overPosition(this.agentCellPos, this.portalCellPos);
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
	 * @return Orientation vector.
	 */
	public Vector2d getOrientationVector() {
		return this.orientationVector;
	}
	
	
	/**
	 * @return Displacement vector.
	 */
	public Vector2d getDisplacementVector() {
		return this.displacementVector;
	}
	
	
	/**
	 * @return Goal vector.
	 */
	public Vector2d getGoalVector() {
		return this.goalVector;
	}
	
	
	/**
	 * @return Agent speed.
	 */
	public float getSpeed() {
		return this.speed;
	}
	
	
	/**
	 * @return True if displacement is in the direction of the goal.
	 */
	public boolean isDisplacementCorrect() {
		return goal == TRUE;
	}
	
}
