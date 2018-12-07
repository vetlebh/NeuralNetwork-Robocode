package LearningRobots;



//import java.math.*;
import java.io.IOException;
import java.util.Random;
import java.io.PrintWriter;
import java.io.File;
import java.util.*;



class ReturnValues {

    static double[] errorList = new double[10000];
    static int epoch;
    ReturnValues(double[] errList, int ep){
        errorList = errList;
        epoch = ep;
    }
}

public class NeuralNet {

    //Choose the type of sigmoid function
    static boolean binary = false;            //true for binary training set, false for bipolar training set


    //hyperparameters for the neural network
    static int numHidden = 5;                  //Number of hidden neurons in network
    static double learningRate = 0.2;         //Controlling learning speed
    static double momentumTerm = 0.0;           //Controlling training time
    static double bias = 1;                     //Fixed bias parameter

    //Constant parameters
    static int numInputs = 3;              //Number of input values plus one bias value
    static int    numOutputs = 1;
    static int numTrainingSet = 4;       //Number of items in the training set
    static int currentTrainingSet = 0;       //Current data set used for training
    static double totalErrorThreshold = 0.05; //Total error threshold stopping criteria
    static int MAX_EPOCH = 10000;

    //Other parameters:
    static double argA = -0.5;
    static double argB = 0.5;

    double [][] inputValues      = new double[numTrainingSet][numInputs];
    double [][] testOutput = new double[numTrainingSet][numOutputs];
    double []   hiddenS     = new double[numHidden];
    double []   outputS     = new double[numOutputs];
    double [] singleError = new double[numOutputs];
    double []   totalError  = new double[numOutputs];

    double [][] weightInputToHidden = new double[numInputs][numHidden];
    double [][] WeightHiddenToOutput = new double[numHidden][numOutputs];
    double [][] deltaWeightHiddenInput = new double[numInputs][numHidden];
    double [][] deltaWeightHiddenOutput = new double[numHidden][numOutputs];
    double [] deltaOutputS = new double[numOutputs];
    double [] deltaHiddenS = new double[numHidden];


    //Main function

    public static void main(String[] args)
    {

        //Initialization of network
        NeuralNet XORdataset = new NeuralNet();
        XORdataset.initializeWeights();
        XORdataset.initializeTrainingSet();

        //Training process
        XORdataset.train();

        //repeated training of neural networks
        int numberOfNetworks = 10;


        double[][] results = new double[numberOfNetworks][MAX_EPOCH];
        double[] epochList = new double[numberOfNetworks];


        for (int i = 0; i < numberOfNetworks; i ++){
            NeuralNet XOR = new NeuralNet();
            XOR.initializeWeights();
            XOR.initializeTrainingSet();

            //Training process

            ReturnValues r = XOR.train();


            results[i] = r.errorList;
            epochList[i] = r.epoch;


        }
        try {
            saveToFile("errortest.txt", epochList, results) ;
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        //averaging several simulations

        int numberOfSimul = 1000;
        int[] epochListForAvg = new int[numberOfSimul];
        for (int i = 0; i < numberOfSimul; i ++){
            NeuralNet XOR = new NeuralNet();
            XOR.initializeWeights();
            XOR.initializeTrainingSet();

            //Training process

            ReturnValues r = XOR.train();



            epochListForAvg[i] = r.epoch;


        }
        //averaging

        double sum = 0;
        double avg;
        for (int i = 0; i < numberOfSimul; i ++){
            sum = sum + epochListForAvg[i];
        }
        avg = sum / numberOfSimul;

        System.out.println("Average of " + numberOfSimul + " simulations: " + avg);



    }

    //train function
    public ReturnValues train(){
        int epoch = 0;

        double[] errorList = new double[MAX_EPOCH];

        do{
            currentTrainingSet = 0;
            for(int k = 0; k < numOutputs; k++){
                totalError[k] = 0;
            }

            for(int i = 0; i < numTrainingSet; i++){
                forwardFeed();
                backPropagation();
                for(int k = 0; k < numOutputs; k++){
                    totalError[k] += Math.pow(singleError[k], 2);
                }
            }

            for(int k = 0; k < numOutputs; k++){
                totalError[k] /= 2;
                //System.out.println("Total error for output number " + (k + 1) + ": " + totalError[k]);
            }
            //errorList.add(totalError);
            errorList[epoch] = totalError[0]; //hardcode


            epoch = epoch + 1;
        } while(totalError[0] > totalErrorThreshold & epoch < MAX_EPOCH);

        //System.out.println("Number of epochs until error threshold reached: " + epoch);

        ReturnValues R = new ReturnValues(errorList, epoch);
        return R;

    }

    public void forwardFeed(){
        for(int j = 1; j < numHidden; j++){ //Keep the bias node unchanged
            hiddenS[j] = 0;
            for(int i = 0; i < numInputs; i++){
                hiddenS[j] += inputValues[currentTrainingSet][i] * weightInputToHidden[i][j];
            }
            hiddenS[j] = customSigmoid(hiddenS[j]); //implement RELU?
        }

        for(int k = 0; k < numOutputs; k++){
            outputS[k] = 0;
            for(int j = 0; j < numHidden; j++){
                outputS[k] += hiddenS[j] * WeightHiddenToOutput[j][k];
            }
            outputS[k] = customSigmoid(outputS[k]);
            singleError[k] = testOutput[currentTrainingSet][k] - outputS[k];
        }

    }


    public void initializeWeights() {

        for (int i = 0; i < numInputs; i++) {
            for (int j = 1; j < numHidden; j++) { //unchanged bias node
                double r = new Random().nextDouble();

                weightInputToHidden[i][j] = argA + (r * (argB - argA));
                deltaWeightHiddenInput[i][j] = 0.0;
            }
        }

        for (int i = 0; i < numHidden; i++) {
            for (int j = 0; j < numOutputs; j++) {
                double r = new Random().nextDouble();

                WeightHiddenToOutput[i][j] = argA + (r * (argB - argA));
                deltaWeightHiddenOutput[i][j] = 0.0;
            }
        }

    }



    public void backPropagation(){
        //Compute the delta values of output layer
        for(int k = 0; k < numOutputs; k++){
            deltaOutputS[k] = 0;
            if(binary){
                deltaOutputS[k] = singleError[k] * outputS[k] * (1 - outputS[k]);
            }
            else if(!binary){
                deltaOutputS[k] = singleError[k] * (outputS[k] + 1) * 0.5 * (1 - outputS[k]);
            }
        }

        //Update weights between hidden layer and output layer
        for(int k = 0; k < numOutputs; k++){
            for(int j = 0; j < numHidden; j++){
                deltaWeightHiddenOutput[j][k] = momentumTerm * deltaWeightHiddenOutput[j][k]
                        + learningRate * deltaOutputS[k] * hiddenS[j];
                WeightHiddenToOutput[j][k] += deltaWeightHiddenOutput[j][k];
            }
        }

        //Compute the delta values of hidden layer
        for(int j = 0; j < numHidden; j++){
            deltaHiddenS[j] = 0;
            for(int k = 0; k < numOutputs; k++){
                deltaHiddenS[j] += deltaOutputS[k] * WeightHiddenToOutput[j][k];
            }
            if(binary){
                deltaHiddenS[j] = deltaHiddenS[j] * hiddenS[j] * (1 - hiddenS[j]);
            }
            else if(!binary){
                deltaHiddenS[j] = deltaHiddenS[j] * (hiddenS[j] + 1) * 0.5 * (1 - hiddenS[j]);
            }
        }

        //Update weights between input layer and hidden layer
        for(int j = 1; j < numHidden; j++){
            for(int i = 0; i < numInputs; i++){
                deltaWeightHiddenInput[i][j] = momentumTerm * deltaWeightHiddenInput[i][j]
                        + learningRate * deltaHiddenS[j] * inputValues[currentTrainingSet][i];
                weightInputToHidden[i][j] += deltaWeightHiddenInput[i][j];
            }
        }

        //Next input of the same epoch
        currentTrainingSet = (currentTrainingSet + 1) % numTrainingSet;

    }



    public double customSigmoid(double x){

        //setting bounds to bipolar case
        double upperBound = 1;
        double lowerBound = - 1;

        //The bounds for binary inputs
        if(binary){
            upperBound = 1;
            lowerBound = 0;
        }

        return (upperBound - lowerBound) / (1 + Math.pow(Math.E, -x)) + lowerBound;
    }


    public void initializeTrainingSet(){

        //add bias to first hidden neuron
        hiddenS[0] = bias;

        if (binary){
            testOutput[0][0] = 0;
            testOutput[1][0] = 1;
            testOutput[2][0] = 1;
            testOutput[3][0] = 0;

            inputValues[0][0] = bias;
            inputValues[0][1] = 0;
            inputValues[0][2] = 0;

            inputValues[1][0] = bias;
            inputValues[1][1] = 0;
            inputValues[1][2] = 1;

            inputValues[2][0] = bias;
            inputValues[2][1] = 1;
            inputValues[2][2] = 0;

            inputValues[3][0] = bias;
            inputValues[3][1] = 1;
            inputValues[3][2] = 1;

        }
        else if (!binary){
            testOutput[0][0] = -1;
            testOutput[1][0] = 1;
            testOutput[2][0] = 1;
            testOutput[3][0] = -1;

            inputValues[0][0] = bias;
            inputValues[0][1] = -1;
            inputValues[0][2] = -1;

            inputValues[1][0] = bias;
            inputValues[1][1] = -1;
            inputValues[1][2] = 1;

            inputValues[2][0] = bias;
            inputValues[2][1] = 1;
            inputValues[2][2] = -1;

            inputValues[3][0] = bias;
            inputValues[3][1] = 1;
            inputValues[3][2] = 1;

        }


    }

    public static void saveToFile(String filename, double[] list, double[][] matrix) throws IOException { //hardcode ish / generalize later



        PrintWriter pw = new PrintWriter(new File(filename));
        for (int i = 0; i < list.length; i++){
            pw.printf(Double.toString(list[i]) + ',');
        }
        pw.printf("\n");

        for (int i = 0; i < MAX_EPOCH; i ++){

            for (int j = 0; j < list.length; j ++) {
                pw.printf(Double.toString(matrix[j][i]) + ',');
                //if (matrix[j][i] != 0.0){
                //  pw.printf(Double.toString(matrix[j][i])+ ',');
                //}


            }
            pw.printf("\n");

        }
        pw.close();
    }





}
