package raulcastilla215alu.mytools;

import java.util.Random;

import ontology.Types.ACTIONS;
import raulcastilla215alu.matrix.QTable;
import tools.Vector2d;

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
	
	private QTable visitedStates;
	
	private final float CONSTANT = 40000;
	
	private final float WINREWARD = 2000f;
	private final float DEADREWARD = -2000f;
//	private final float SIMPLEREWARD = 100f;
	private final float BIGREWARD = 200f;
	
//	private final int DISTANCEFACTOR = 10;
	private final int MINANGLEDIF = 12;
	
	private static int matchCounter = 0;
	private static int deadCounter = 0;
	private static int winCounter = 0;
	private static int fastMacroCounter = 0;
	private static int displacementMacroCounter = 0;
	private static int correctDispMacroCounter = 0;
	
	
	/**
	 * Constructor. Initializes the Qtable.
	 * @param qTable initial Qtable.
	 */
	public QLearning(QTable qTable, QTable visitedStates) {
		this.qTable = qTable;
		this.visitedStates = visitedStates;
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
	 * Save the visitedStates information into CSV format.
	 * 
	 * @param path path to save the visitedStates information.
	 */
	public void saveVisitedStates(String path) {
		visitedStates.toCSV(path);
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
		
		visitedStates.set(previousState, lastAction, visitedStates.get(previousState, lastAction) + 1);
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
		
		if(currentState.isAgentDead()) {
			deadCounter++;
			return DEADREWARD;
		}

		if(currentState.isAgentWinner() && !previousState.isFast() && !currentState.isFast() && currentState.isAgentOverPortal()) {
			winCounter++;
			return WINREWARD;
		}

		if(previousState.isFast()) {
			fastMacroCounter++;
			return fastMacroStateReward(previousState, lastAction, currentState);
		}

		
		if(!previousState.isDisplacementCorrect()) {
			displacementMacroCounter++;
			return displacementMacroStateReward(previousState, lastAction, currentState);
		}

		correctDispMacroCounter++;
		return 3*BIGREWARD;
	}
	
	
	/**
	 * Reward if the agent is fast.
	 * 
	 * @param previousState previous state.
	 * @param lastAction last action.
	 * @param currentState current state.
	 * @return reward.
	 */
	private float fastMacroStateReward(AgentState previousState, ACTIONS lastAction, AgentState currentState) {
		Vector2d orientationVector = currentState.getOrientationVector();
		Vector2d displacementVector = currentState.getDisplacementVector();
		
		float angle = AgentState.angleBetweenVectors(orientationVector, displacementVector);
		float dif180 = Math.abs(angle - 180);
		
		if(dif180 < MINANGLEDIF) {
			float currentSpeed = currentState.getSpeed();
			float previousSpeed = previousState.getSpeed();
			int signo = 1;
			
			if(currentSpeed > previousSpeed) signo = -1;
			
			return BIGREWARD/(dif180+1) + signo*3*BIGREWARD/(currentSpeed+1);
		}
		
		return BIGREWARD/(dif180+1);
	}
	
	
	/**
	 * Reward if the agent displacement is wrong.
	 * 
	 * @param previousState previous state.
	 * @param lastAction last action.
	 * @param currentState current state.
	 * @return reward.
	 */
	private float displacementMacroStateReward(AgentState previousState, ACTIONS lastAction, AgentState currentState) {
//		Vector2d orientationVector = currentState.getOrientationVector();
		Vector2d displacementVector = currentState.getDisplacementVector();
		Vector2d goalVector = currentState.getGoalVector();
		
//		float angleOrientationGoal = AgentState.angleBetweenVectors(orientationVector, goalVector);
		float angledisplacementGoal = AgentState.angleBetweenVectors(displacementVector, goalVector);
		
//		return BIGREWARD/(angleOrientationGoal+1) + BIGREWARD/(angledisplacementGoal+1);
		return BIGREWARD/(angledisplacementGoal+1);
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
	
	
	/**
	 * Show Q-Learning counters.
	 */
	public void showCounters() {
		matchCounter++;
		System.out.println("Match counter = " + matchCounter + "\n" +
						   "Dead counter = " + deadCounter + "\n" +
						   "Win counter = " + winCounter + "\n" + 
						   "Fast macro counter = " + fastMacroCounter + "\n" + 
						   "Displacement macro counter = " + displacementMacroCounter + "\n" + 
						   "Correct displacement counter = " + correctDispMacroCounter + "\n");
	}
}
