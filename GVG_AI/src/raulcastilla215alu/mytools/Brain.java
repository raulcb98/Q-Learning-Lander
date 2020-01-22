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
	//private int deadCounter;
	private QTable qTable;
	
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
		qLearning = new QLearning(qTable);
		
		//deadCounter = 0;
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
		
		return qLearning.learn(previousState, lastAction, currentState);
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
		/*
        int ticks = stateObs.getGameTick();
        IOModule.write("./History.txt", ticks + "\n" + currentState.toString(), true);
		
		currentState.perceive(stateObs);
		if(currentState.portalExist() && !currentState.isAgentDead())
			return qTable.getBestAction(currentState);
		else
			return ACTIONS.ACTION_NIL; 
		
		
		String content = "Ticks = " + stateObs.getGameTick() + "\n" + currentState.toString();
		IOModule.write("./History.txt", content, true);
		*/
		
		return qTable.getBestAction(currentState);
	}
	
	/**
	 * Save the Qtable information.
	 */
	public void saveQTable() {
		qLearning.saveQTable(savePath);
	}

	/**
	 * @return alpha value of the Q-learning.
	 */
	public float getAlpha() {
		return this.qLearning.getAlpha();
	}
	
}
