package LearningRobots;

import java.io.*;



public class Qlearning {

    //hyperparameters
    private double learningRate = 0.1;
    private double discountFactor;

    private double epsilon; //exploration rate
    private double tau = 0.3; //exploitation rate

    //reward variable
    //double reward;
    
    boolean firstState = true;

    //Look-up table
    LUT table;
    
    //Updated action and state variables
    int prevState = 0;
    int prevAction = 0;

    //FUNCTIONS
    public Qlearning(LUT table, double epsilon, double discountFactor){
        this.table = table;
        this.epsilon = epsilon;
        this.discountFactor = discountFactor;
    }

    public void Qlearn(boolean Qlearn, int state, int action, double reward){
    	if (firstState) firstState = false;
    	else {
    		if (Qlearn) {
    			double oldQVal = table.getValue(prevState, prevAction);
                double learnedValue = reward + discountFactor * table.getMaxValue(state);
                double newQVal = (1 - learningRate) * oldQVal + learningRate * learnedValue;

                //Set new Q-value in previous state
                table.setValue(prevState, prevAction, newQVal);

                //update state and action
                prevAction = action;
                prevState = state;
    		}
    		else {
    			double oldQVal = table.getValue(prevState, prevAction);
    			double learnedValue = reward + discountFactor * table.getValue(state, action) - oldQVal;
    			double newQVal = oldQVal + learningRate * learnedValue;
    			
    			//Set new Q-value in previous state
    			table.setValue(prevState, prevAction, newQVal);

                //Update state and action
                prevAction = action;
                prevState = state;
    		}
    	}
    }
    
    //Implement both Softmax and epsilon-greedy action selection
    
    //Epsilon-greedy strategy
    public int selectActionEGreedy(int state){
        int action;
        double rand = Math.random();
        if (epsilon > rand){
            action = (int) Math.random() * table.numActions; //verify this
        }
        else{
            action = table.getBestAction(state);
        }
        return action;
    }

    //Softmax selection strategy
    int selectActionSoftMax(int state){
        int action = 0;
        double Qsum = 0;
        double[] Qprob = new double[table.numActions];

        for (int i = 0; i < table.numActions; i ++) {
            Qprob[i] = Math.exp(table.getValue(state, i) / tau);
            Qsum += Qprob[i];
        }
        if (Qsum != 0) {
            for (int i = 0; i < table.numActions; i ++) {
                Qprob[i] /= Qsum;
            }

        } else {
            action = table.getBestAction(state);
            return action;
        }

        //Look into this
        double cumulativeProb = 0.0;
        double randomNum = Math.random();
        
        while (randomNum > cumulativeProb && action < table.numActions) {
            cumulativeProb += Qprob[action];
            action ++;
        }
        
        return action - 1;
    }
}
