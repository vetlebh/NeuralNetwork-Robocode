package LearningRobots;


import robocode.*;
import java.io.*;
import robocode.*;

public class LUT {

    int argNumInputs;
    int[] argVariableFloor;
    int[] argVariableCeiling;

    int numActions;
    int numStates;

    public static double[][] table;
    
    public static double[][] prevTable;
    public static double[] Qloss;
    private static int iterator = 0;
    private static int QTableSize;

    public LUT(int numActions, int numStates, int nrOfBattles) {

    	//Setting variable values
        this.numActions = numActions;
        this.numStates = numStates;
        table = new double[numStates][numActions];
        
        
        this.QTableSize = nrOfBattles;
        
        if (iterator == 0) {
        	prevTable = new double[numStates][numActions];
        	Qloss = new double[nrOfBattles];
        }
        //Initializing table when constructor is called
        initialiseLUT();
        
    }


    //Initialize entries to arbitrary value
    void initialiseLUT() {
        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numActions; j++) {
                table[i][j] = 100.0;
            }
        }
    }

    //return specified table value
    public double getValue(int state, int action){
        return table[state][action];
    }
    //set specified table value
    public void setValue(int state, int action, double QVal){
        table[state][action] = QVal;
    }


    double getMaxValue(int state){

        double maxValue = -100000;
        for (int j = 0; j < numActions; j++){
            if (table[state][j] > maxValue){
                maxValue = table[state][j];
                }
            }
        return maxValue;
    }

    int getBestAction(int state){
        double maxValue = -100000;
        int bestAction = 0;             //return this action by default
        for (int i = 0; i < table[state].length; i ++){
            if (getValue(state, i) > maxValue){
                maxValue = getValue(state, i);
                bestAction = i;
            }
        }
        return bestAction;
    }

    public void load(File file)
    {
        BufferedReader r = null;
        try
        {
            r = new BufferedReader(new FileReader(file));
            for (int i = 0; i < numStates; i++) {
                for (int j = 0; j < numActions; j++) {
                    table[i][j] = Double.parseDouble(r.readLine());
                }
            }
        }
        catch (IOException e)
        {
            System.out.println("IOException trying to open reader: " + e);
            initialiseLUT();
        }
        catch (NumberFormatException e)
        {
            initialiseLUT();
        }
        finally
        {
            try
            {
                if (r != null)
                r.close();
            }
            catch (IOException e)
            {
                System.out.println("IOException trying to close reader: " + e);
            }
        }
    }
    
	public void save(File file)
    {
        PrintStream w = null;
        try
        {
            w = new PrintStream(new RobocodeFileOutputStream(file));
            for (int i = 0; i < numStates; i++)
                for (int j = 0; j < numActions; j++)
                    w.println(new Double(table[i][j]));
 
            if (w.checkError())
                System.out.println("Could not save the data!");
            w.close();
        }
        catch (IOException e)
        {
            System.out.println("IOException trying to write: " + e);
        }
        finally
        {
            try
            {
                if (w != null)
                    w.close();
            }
            catch (Exception e)
            {
                System.out.println("Exception trying to close witer: " + e);
            }
        }
    }
    
    public void QLoss() {
    	double sumSquareDiff = 0;
    	for (int i = 0; i < numStates; i++)
            for (int j = 0; j < numActions; j++)
                sumSquareDiff += Math.pow(table[i][j]-prevTable[i][j], 2);
    	Qloss[iterator] = sumSquareDiff;
    	iterator ++;
    	prevTable = table;
    }
    
    public void saveQloss(File file)
    {
        PrintStream w = null;
        try
        {
            w = new PrintStream(new RobocodeFileOutputStream(file));
            for (int i = 0; i < QTableSize; i++)
            	w.println(new Double(Qloss[i]));
 
            if (w.checkError())
                System.out.println("Could not save the data!");
            w.close();
        }
        catch (IOException e)
        {
            System.out.println("IOException trying to write: " + e);
        }
        finally
        {
            try
            {
                if (w != null)
                    w.close();
            }
            catch (Exception e)
            {
                System.out.println("Exception trying to close witer: " + e);
            }
        }
    }
    


}