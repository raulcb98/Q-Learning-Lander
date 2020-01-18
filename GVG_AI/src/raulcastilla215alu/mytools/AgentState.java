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
	private int blockSize;
	private double score;
	
	private float angle_diff = 0.2f;
	private float speed_limit = 9;

	
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
		agentDead = isDead(stateObs);
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
	
	
	private void updateOrientation(StateObservation stateObs) {
		ACTIONS lastAction = stateObs.getAvatarLastAction();
		if(lastAction.equals(ACTIONS.ACTION_LEFT)) {
			orientationRad += angle_diff;
		}
		if(lastAction.equals(ACTIONS.ACTION_RIGHT)) {
			orientationRad -= angle_diff;
		}
	}
	
	private void updatePortalCellPos(StateObservation stateObs) {
		ArrayList<Observation>[] portalsPos = stateObs.getPortalsPositions();
		
		if(portalsPos != null) {
			// Cast from ArrayList<Observation>[] to ArrayList<Observation>
			ArrayList<Observation> aux = new ArrayList<>();
			for(int i = 0; i < portalsPos.length; i++) {
				aux.add(portalsPos[0].get(0));
			}
			
			this.portalCellPos = calculateCell(getNearest(aux).position, this.blockSize);
		}
	}
	
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
	
	
	private int perceiveOrientation() {
		float degrees = (float) Math.toDegrees(orientationRad);
		return degreesToConstant(degrees);
	}
	
	
	private int perceiveDisplacement(StateObservation stateObs) {
		Vector2d dir = stateObs.getAvatarOrientation();
		dir.set(dir.x, -dir.y);
		
		float degrees = (float)Math.toDegrees(Math.atan(dir.y/dir.x));
		return degreesToConstant(degrees);
	}
	
	private int degreesToConstant(float degrees) {
		if(degrees < 45) return State.ANGLE0;
		if(degrees < 90) return State.ANGLE45;
		if(degrees < 135) return State.ANGLE90;
		if(degrees < 180) return State.ANGLE135;
		if(degrees < 225) return State.ANGLE180;
		if(degrees < 270) return State.ANGLE225;
		if(degrees < 315) return State.ANGLE270;
		if(degrees < 0) return State.ANGLE315;
		
		return -1;
	}
	
	
	private int perceiveCompassEW() {
		if(this.portalCellPos == null) return State.WEST;
		
		if(this.agentCellPos.x >= this.portalCellPos.x) return State.WEST;
		return State.EAST;
	}
	
	
	private int perceiveCompassNS() {
		if(this.portalCellPos == null) return State.SOUTH;
		
		if(this.agentCellPos.y >= this.portalCellPos.y) return State.NORTH;
		return State.SOUTH;
	}
	
	private int perceiveDanger(StateObservation stateObs) {
		return State.FALSE;
	}
	
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
	 * Checks if the agent is dead.
	 * 
	 * @param stateObs game information.
	 * @return true if the agent is dead.
	 */
	private boolean isDead(StateObservation stateObs) {
		Vector2d orientation = stateObs.getAvatarOrientation();
		return orientation.equals(new Vector2d(0,1));
	}
	

	public float distance(Vector2d posA, Vector2d posB) {
		
		float difX = (float)(posA.x - posB.x);
		float difY = (float)(posA.y - posB.y);
		
		return (float)Math.sqrt(Math.pow(difX, 2) + Math.pow(difY, 2));
	}
	
	/**
	 * Returns a String with the information of the Object.
	 */
	@Override
	public String toString() {
		String str = super.toString();
		str +=  "\nAgent position = " + agentCellPos.toString() + "\n" +
				"Portal position = " + portalCellPos.toString() + "\n" +
				"Orientation = " + (int)Math.toDegrees(orientationRad) + "\n\n";
		return str;
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

}
