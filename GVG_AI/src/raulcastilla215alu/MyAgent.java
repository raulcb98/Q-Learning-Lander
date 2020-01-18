package raulcastilla215alu;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

import core.game.Game;
import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import raulcastilla215alu.matrix.QTable;
import raulcastilla215alu.mytools.AgentState;
import raulcastilla215alu.mytools.Brain;
import raulcastilla215alu.mytools.IOModule;
import raulcastilla215alu.mytools.QLearning;
import raulcastilla215alu.mytools.StateGenerator;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import raulcastilla215alu.mytools.State;;
 

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class MyAgent extends AbstractPlayer {
	

    /**
     * Random generator for the agent.
     */
    protected Random randomGenerator;
    /**
     * List of available actions for the agent
     */
    protected ArrayList<Types.ACTIONS> actions;

    private Brain brain;
    private StateObservation stateObs;
    
    
    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public MyAgent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
    	String savePath = "./QTable/Qtable.txt";
        randomGenerator = new Random();
        brain = new Brain(stateObs, savePath);
        actions = stateObs.getAvailableActions(true);
        this.stateObs = stateObs;
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
    	
//    	for(int i = 0; i < 10000; i++) {
//    		System.out.println("");
//    	}
    	
        this.stateObs = stateObs;
    	return brain.act(stateObs);
    	
//    	return brain.learn(stateObs);
    }
    
    public void close() {
    	brain.saveQTable();
//    	System.out.println("QTable saved!");
//    	
//    	double time = QLearning.time;
//    	double alpha = brain.getAlpha();
//    	double score = stateObs.getGameScore();
//    	String row = Double.toString(time) + "," + Double.toString(alpha) + "," + Double.toString(score) + "\n";
//    	
//    	IOModule.write("./time_alpha_score.csv", row, true);
//    	
//		System.out.println("Time = " + time + " Alpha = " + alpha);
    }
}
