package raulcastilla215alu.mytools;

import java.util.Random;

import ontology.Types.ACTIONS;
import raulcastilla215alu.matrix.QTable;

/**
 *  Defines the Q-learning algorithm.
 *  
 * @author Raul Castilla Bravo.
 *
 */
public class QLearning {

	/**
	 * Privates attributes.
	 */
	private QTable qTable;
	private float gamma;
	private float alpha;
	public static double time = 0;
	private float epsilon;
	
	private final float CONSTANT = 25000;
	
	private final float WINREWARD = 2000f;
	private final float DEADREWARD = -2000f;
	private final float SIMPLEREWARD = 100f;
	private final float BIGREWARD = 200f;
	
	private final int DISTANCEFACTOR = 10;

	
	/**
	 * Constructor. Initializes the Qtable.
	 * @param qTable initial Qtable.
	 */
	public QLearning(QTable qTable) {
		this.qTable = qTable;
		gamma = 0.5f;
		alpha = 0.8f;
		epsilon = 0.8f;
	}
	
	/**
	 * Save the Qtable information into CSV format.
	 * 
	 * @param path path to save the Qtable information.
	 */
	public void saveQTable(String path) {
		qTable.toCSV(path);
	}
	
	/**
	 * Execute the Q-learning formula.
	 * 
	 * @param previousState previous state.
	 * @param lastAction last action.
	 * @param currentState current state.
	 * @return next action.
	 */
	public ACTIONS learn(AgentState previousState, ACTIONS lastAction, AgentState currentState) {
		
		float sample = reward(previousState, lastAction, currentState) + gamma * qTable.getMaxQValue(currentState);
		float newQValue = (1-alpha)*qTable.get(previousState, lastAction) + alpha*sample;
		qTable.set(previousState, lastAction, newQValue);
		
		updateConstants();
		
		return nextAction(currentState);
	}
	
	/**
	 * Reward function.
	 * 
	 * @param previousState previous state.
	 * @param lastAction last action.
	 * @param currentState current state.
	 * @return reward.
	 */
	private float reward(AgentState previousState, ACTIONS lastAction, AgentState currentState) {
		
		float finalReward = 0;
		
		// Dead reward
		if(currentState.isAgentDead()) return DEADREWARD;
		
		// Win reward
		if(currentState.isAgentWinner() && !previousState.isFast() && !currentState.isFast() && currentState.isAgentOverPortal()) 
			finalReward += WINREWARD;
		
		// Ships moves reward
		if(!currentState.isFast()) finalReward += SIMPLEREWARD;
		else finalReward -= SIMPLEREWARD;
		
		if(currentState.isOrientationInGreenZone()) finalReward += SIMPLEREWARD;
		else finalReward -= SIMPLEREWARD;
		
		if(currentState.isDisplacementInGreenZone()) finalReward += SIMPLEREWARD;
		else finalReward -= SIMPLEREWARD;

		
		// Distance reward
		float previousDistanceAxisX = previousState.distanceToPortal(AgentState.AXISY);
		float distanceAxisX = currentState.distanceToPortal(AgentState.AXISX);
		int signo = 1;
//		if(distanceAxisX > previousDistanceAxisX) signo = -1;
		
		finalReward += signo*BIGREWARD/(DISTANCEFACTOR*distanceAxisX + 1);
		
		if(currentState.isAgentOverPortal()) {
			float distanceAxisY = currentState.distanceToPortal(AgentState.AXISY);
			finalReward += BIGREWARD/(DISTANCEFACTOR*distanceAxisY + 1);
		}
		
		// Compass recommendations reward
//		float previousWallDistance = previousState.distanceToNearestWall();
//		float currentWallDistance = currentState.distanceToNearestWall();
//		signo = 1;
//		//if(previousWallDistance > currentWallDistance) signo = -1;
//		
//		finalReward += signo*BIGREWARD*currentWallDistance;
		
		
//		int check = AgentState.obeyCompass(previousState, currentState, previousState.getCompass());
//		if(check == State.TRUE) finalReward += BIGREWARD;
//		if(check == State.FALSE) finalReward -= BIGREWARD;

		return finalReward;
	}
	
	/**
	 * Return the next action taking into acount an 
	 * exploration policity.
	 * 
	 * @param currentState current state.
	 * @return next action.
	 */
	private ACTIONS nextAction(AgentState currentState) {
		Random rd = new Random();
		float randomNumber = Math.abs(rd.nextFloat());

		if (randomNumber < epsilon) {
			return qTable.getRandomAction();
		} else {
			return qTable.getBestAction(currentState);
		}
	}
	
	/**
	 * Update Q-learning constants.
	 */
	private void updateConstants() {
		alpha = (float) (0.8*CONSTANT/(CONSTANT + time));
		epsilon = (float) (0.8*CONSTANT/(CONSTANT + time));
		
		time++;
	}
	
	/**
	 * @return learning factor.
	 */
	public float getAlpha() {
		return alpha;
	}
}
