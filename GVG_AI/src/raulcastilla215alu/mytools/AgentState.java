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
	private float orientationRad;
	
	private boolean agentDead;
	private boolean agentWinner;
	private int blockSize;
	private double score;
	
	private float angle_diff = 0.2f;
	private float speed_limit = 6;     //9.35f;
	
	public static final int ITYPEPORTAL = 2; //itype of portal.
	
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
		agentCellPos = calculateCell(stateObs.getAvatarPosition(), stateObs.getBlockSize());	
		score = stateObs.getGameScore();
		agentDead = false;
		agentWinner = false;
		updatePortalCellPos(stateObs);
		
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
		
		// Perceive compass EW
		arrayStateValues.set(State.POSCOMPASSEW, perceiveCompassEW());
		
		// Perceive compass NS
		arrayStateValues.set(State.POSCOMPASSNS, perceiveCompassNS());
		
		// Perceive danger
		arrayStateValues.set(State.POSDANGER, perceiveDanger(stateObs));
		
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
	 * Update portalCellPos with the nearest portal observation.
	 * 
	 * @param stateObs Game observations.
	 */
	private void updatePortalCellPos(StateObservation stateObs) {
		ArrayList<Observation> portalsPos = findPortals(stateObs);
		this.portalCellPos = calculateCell(getNearest(portalsPos).position, this.blockSize);
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
			currentCellPos = calculateCell(obs.get(i).position, this.blockSize);
			currentDistance = distance(this.agentCellPos, currentCellPos);
			if(currentDistance < nearestDistance) {
				nearestDistance = currentDistance;
				nearestObs = obs.get(i);
			}
		}
		return nearestObs;
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
		return degreesToConstant(degrees);
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
		return degreesToConstant(degrees);
	}
	
	
	/**
	 * Cast from integer to the constant associated to angles.
	 * 
	 * @param degrees Angle in degrees.
	 * @return constant associated to the angle.
	 */
	private int degreesToConstant(float degrees) {
		if(degrees < 45) return State.ANGLE0;
		if(degrees < 90) return State.ANGLE45;
		if(degrees < 135) return State.ANGLE90;
		if(degrees < 180) return State.ANGLE135;
		if(degrees < 225) return State.ANGLE180;
		if(degrees < 270) return State.ANGLE225;
		if(degrees < 315) return State.ANGLE270;
		if(degrees <= 360) return State.ANGLE315;
		
		return -1;
	}
	
	
	/**
	 * Calculate the current compassEW orientation taking into 
	 * account the nearest portal position.
	 * 
	 * @return current compassEW orientation.
	 */
	private int perceiveCompassEW() {
		if(this.portalCellPos == null) return State.WEST;
		
		if(this.agentCellPos.x >= this.portalCellPos.x) return State.WEST;
		return State.EAST;
	}
	
	/**
	 * Calculate the current compassNS orientation taking into 
	 * account the nearest portal position.
	 * 
	 * @return current compassNS orientation.
	 */
	private int perceiveCompassNS() {
		if(this.portalCellPos == null) return State.SOUTH;
		
		if(this.agentCellPos.y >= this.portalCellPos.y) return State.NORTH;
		return State.SOUTH;
	}
	
	
	private int perceiveDanger(StateObservation stateObs) {
		return State.FALSE;
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
	 * @return euclidean distance between agent and nearest portal.
	 */
	public float distanceToPortal() {
		return distance(this.agentCellPos, this.portalCellPos);
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

		str += "************************** \n\n";
		
		return str;
	}

	public void setAgentDead(boolean value) {
		this.agentDead = value;
	}
	
	public void setAgentWinner(boolean value) {
		this.agentWinner = value;
	}
	
	public boolean isAgentWinner() {
		return this.agentWinner;
	}
	
	/**
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
		return fast;
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
