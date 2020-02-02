package raulcastilla215alu.vectorAgent;

import java.util.ArrayList;
import java.util.Random;

import core.game.StateObservation;
import ontology.Types.ACTIONS;
import raulcastilla215alu.mytools.QTable;

/**
 * Defines the agent brain.
 * 
 * @author Raul Castilla Bravo.
 *
 */
public class ScalarBrain {
	
	/**
	 * Privates attributes.
	 */
	private VectorQLearning qLearning;
	private ScalarAgentState currentState;
	private ScalarAgentState previousState;
	private ACTIONS lastAction;
	private String savePath;
	private QTable qTable;
	private QTable visitedStates;
	
	/**
	 * Constructor. Initializes the brain with the observations introduced by parameters.
	 * 
	 * @param stateObs game observations.
	 * @param savePath CSV file path to load the information of the Qtable.
	 */
	public ScalarBrain(StateObservation stateObs, String savePath) {
		this.savePath = savePath;
        currentState = new ScalarAgentState(stateObs);
        previousState = new ScalarAgentState(stateObs);
        lastAction = stateObs.getAvatarLastAction();
        
        ArrayList<ScalarState> states = ScalarStateGenerator.generate();
        ArrayList<ACTIONS> actions = stateObs.getAvailableActions(true);
		qTable = new QTable(states , actions, savePath);
		visitedStates = new QTable(states, actions);
		qLearning = new VectorQLearning(qTable, visitedStates);
		
		//deadCounter = 0;
	}
	
	/**
	 * Perceive the information of the game and learn.
	 * 
	 * @param stateObs game observations.
	 * @return next game action.
	 */
	public ACTIONS learn(StateObservation stateObs) {
		previousState = new ScalarAgentState(currentState);
		currentState.perceive(stateObs);
		lastAction = stateObs.getAvatarLastAction();
		lastAction = qLearning.learn(previousState, lastAction, currentState);
		return lastAction;
	}
	
	public void learnLastAction(double score) {
		if(score == 0) {
			currentState.setAgentDead(true);
			currentState.setAgentWinner(false);
		} else {
			currentState.setAgentDead(false);
			currentState.setAgentWinner(true);
		}
		qLearning.learn(previousState, lastAction, currentState);
	}
	
	/**
	 * Perceive the information of the game and return the best action.
	 * 
	 * @param stateObs game observations.
	 * @return best action.
	 */
	public ACTIONS act(StateObservation stateObs) {

		currentState.perceive(stateObs);
		
//		String content = "Ticks = " + stateObs.getGameTick() + "\n" + currentState.toString();
//		IOModule.write("./History.txt", content, true);
		
		return qTable.getBestAction(currentState);
	}
	
	/**
	 * Save the Qtable information.
	 */
	public void saveQTable() {
		qLearning.saveQTable(savePath);
	}
	
	/**
	 * Save visited states information.
	 */
	public void saveVisitedStates() {
		qLearning.saveVisitedStates("./VisitedStates.csv");
	}
	

	/**
	 * @return alpha value of the Q-learning.
	 */
	public float getAlpha() {
		return this.qLearning.getAlpha();
	}
	
}
