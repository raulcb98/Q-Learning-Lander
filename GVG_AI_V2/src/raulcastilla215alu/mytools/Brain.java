package raulcastilla215alu.mytools;

import java.util.ArrayList;
import java.util.Random;

import core.game.StateObservation;
import ontology.Types.ACTIONS;
import raulcastilla215alu.matrix.QTable;

/**
 * Defines the agent brain.
 * 
 * @author Raul Castilla Bravo.
 *
 */
public class Brain {
	
	/**
	 * Privates attributes.
	 */
	private QLearning qLearning;
	private AgentState currentState;
	private AgentState previousState;
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
	public Brain(StateObservation stateObs, String savePath) {
		this.savePath = savePath;
        currentState = new AgentState(stateObs);
        previousState = new AgentState(stateObs);
        lastAction = stateObs.getAvatarLastAction();
        
        ArrayList<State> states = StateGenerator.generate();
        ArrayList<ACTIONS> actions = stateObs.getAvailableActions(true);
		qTable = new QTable(states , actions, savePath);
		visitedStates = new QTable(states, actions);
		qLearning = new QLearning(qTable, visitedStates);
	}
	
	/**
	 * Perceive the information of the game and learn.
	 * 
	 * @param stateObs game observations.
	 * @return next game action.
	 */
	public ACTIONS learn(StateObservation stateObs) {
		previousState = new AgentState(currentState);
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
		
		String content = "Ticks = " + stateObs.getGameTick() + "\n" + currentState.toString();
		IOModule.write("./History.txt", content, true);
		
//		return qTable.getBestAction(currentState);
		return ACTIONS.ACTION_RIGHT;
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
