package raulcastilla215alu.vectorAgent;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

import core.game.Game;
import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import raulcastilla215alu.mytools.IOModule;
import raulcastilla215alu.mytools.QTable;
import tools.ElapsedCpuTimer;
import tools.Vector2d;;
 

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class VectorAgent extends AbstractPlayer {
	

    /**
     * Random generator for the agent.
     */
    protected Random randomGenerator;
    /**
     * List of available actions for the agent
     */
    protected ArrayList<Types.ACTIONS> actions;

    private ScalarBrain brain;
    private StateObservation stateObs;
    private boolean isLearning;
    
    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public VectorAgent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
    	String savePath = "./QTable/Qtable.txt";
        randomGenerator = new Random();
        brain = new ScalarBrain(stateObs, savePath);
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
        this.stateObs = stateObs;
        
//        isLearning = true;
        isLearning = false;
        
        if(isLearning) {
        	return brain.learn(stateObs);
        } else {
        	return brain.act(stateObs);
        }

    }
    
    public void close(double score) {
    	if(isLearning) {
        	brain.learnLastAction(score);
        	
        	brain.saveQTable();
        	System.out.println("QTable saved!");
        	
//        	brain.saveVisitedStates();
//        	
//        	double time = ScalarQLearning.time;
//        	double alpha = brain.getAlpha();
//        	System.out.println("Time = " + time + " Alpha = " + alpha);
//        	
//        	String row = Double.toString(time) + "," + Double.toString(alpha) + "," + Double.toString(score) + "\n";
//        	IOModule.write("./time_alpha_score.csv", row, true);
    	}
    }
}
