package raulcastilla215alu.mytools;

import java.util.Random;

import ontology.Types.ACTIONS;
import raulcastilla215alu.matrix.QTable;

/**
 *  Defines the Q-learning algorithm.
 *  
 * @author Ricardo Manuel Ruiz Diaz.
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
	
	private final float CONSTANT = 80000;
	private final float WINREWARD = 2000f;
	private final float DEADREWARD = -1000f;
	private final float STOPREWARD = 0;
//	private final float STOPREWARD = -20;
	private final float DISTANCEFACTOR = 200f;
	private final float GOBACKREWARD = 0f;
	
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
		
		float currentDistance;
		float previousDistance;
		
		// Distance reward
		if(currentState.getCompass() == State.EAST || currentState.getCompass() == State.WEST) {
			currentDistance = currentState.getDistanceToPortal(0);
			previousDistance = previousState.getDistanceToPortal(0);
		}
		else {
			currentDistance = currentState.getDistanceToPortal(1);
			previousDistance = previousState.getDistanceToPortal(1);			
		}
		
		float distanceReward = 0;
		
		//System.out.println("Current distance = " + currentDistance + " Previous Distance = " + previousDistance);
		
		float difDistance = previousDistance - currentDistance;
		if (difDistance > 0)
			distanceReward += difDistance * DISTANCEFACTOR;
		else
			distanceReward += Math.abs(difDistance) * GOBACKREWARD;
		
		finalReward += distanceReward;
		
		// Dead reward
		if(currentState.isAgentDead()) {
			if(currentState.getCompass() == State.SOUTH) {
				finalReward += DEADREWARD * 4;
			}
			else {
				finalReward += DEADREWARD;
			}
		}
		
		// Win reward
		if(currentState.getScore() > previousState.getScore()) {
			finalReward += WINREWARD;
		}
		
		//Stop reward
		if(currentState.getAgentPos().equals(previousState.getAgentPos())) {
			finalReward += STOPREWARD;
		}
		
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
		alpha = (float) (0.9*CONSTANT/(CONSTANT + time));
		epsilon = (float) (0.9*CONSTANT/(CONSTANT + time));
		
		time++;
	}
	
	/**
	 * @return learning factor.
	 */
	public float getAlpha() {
		return alpha;
	}
}
