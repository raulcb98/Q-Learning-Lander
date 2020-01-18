package raulcastilla215alu.mytools;

import java.util.ArrayList;

import core.game.StateObservation;
import ontology.Types.ACTIONS;
import raulcastilla215alu.matrix.QTable;

/**
 * Defines the agent brain.
 * 
 * @author Ricardo Manuel Ruiz Diaz.
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
	private int deadCounter;
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
		
		deadCounter = 0;
	}
	
	/**
	 * Percieve the information of the game and learn.
	 * 
	 * @param stateObs game observations.
	 * @return next game action.
	 */
	public ACTIONS learn(StateObservation stateObs) {
		previousState = new AgentState(currentState);
		currentState.perceive(stateObs);
		lastAction = stateObs.getAvatarLastAction();
		
		if(currentState.isAgentDead()) {
			deadCounter++;
		} else {
			deadCounter = 0;
		}
		
		if(deadCounter > 1 || !currentState.portalExist() || !previousState.portalExist()) {
			return ACTIONS.ACTION_NIL;
		} else {
	        int ticks = stateObs.getGameTick();
	        //IOModule.write("./History.txt", ticks + "\n" + currentState.toString(), true);
			return qLearning.learn(previousState, lastAction, currentState);
		}
	}
	
	/**
	 * Percieve the information of the game and return the best action.
	 * 
	 * @param stateObs game observations.
	 * @return best action.
	 */
	public ACTIONS act(StateObservation stateObs) {

		currentState.perceive(stateObs);
        int ticks = stateObs.getGameTick();
        IOModule.write("./History.txt", ticks + "\n" + currentState.toString(), true);
		
		currentState.perceive(stateObs);
		if(currentState.portalExist() && !currentState.isAgentDead())
			return qTable.getBestAction(currentState);
		else
			return ACTIONS.ACTION_NIL; 
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
