package LearningRobots;

//import java.math.*;
import java.io.IOException;
import java.util.Random;
import java.io.PrintWriter;
import java.io.File;
import java.util.*;


public class NeuralNetRobocode {

  //hyperparameters for the neural network
  static int 	numHidden = 5;                  //Number of hidden neurons in network
  static double learningRate = 0.2;         //Controlling learning speed
  static double momentumTerm = 0.0;           //Controlling training time
  static double bias = 1;  						//Fixed bias parameter
  static double discountFactor;

  //Constant parameters
  static int 	numInputs = RobotStates.dimStates;              //Number of input values plus one bias value
  static int 	numOutputs = RobotActions.numActions;


  static double totalErrorThreshold = 0.05; //Total error threshold stopping criteria
  static int 	MAX_EPOCH = 100;
  double totalError;

  //Other parameters:
  static double argA = -0.5;
  static double argB = 0.5;

  int [] inputValues;
  double []   hiddenS     = new double[numHidden];
  double []   outputS     = new double[numOutputs];
  double [] singleError = new double[numOutputs];

  double [][] 	weightInputToHidden = new double[numInputs][numHidden];
  double [][] 	weightHiddenToOutput = new double[numHidden][numOutputs];
  double [][] 	deltaWeightHiddenInput = new double[numInputs][numHidden];
  double [][] 	deltaWeightHiddenOutput = new double[numHidden][numOutputs];
  double [] 	deltaOutputS = new double[numOutputs];
  double [] 	deltaHiddenS = new double[numHidden];
  
  int [] prevInputValues;
  double []	prevOutputS;
  int prevAction;
  double [] prevHiddenS;
  double [][] prevWeightInputToHidden;
  double [][] prevWeightHiddenToOutput;
  double [][] prevDeltaWeightHiddenInput;
  double [][] prevDeltaWeightHiddenOutput;
  double []	prevDeltaOutputS;
  double [] prevDeltaHiddenS;
  
  int bestMove;
  
  //Constructor
  public NeuralNetRobocode(int numInputs, int numOutputs, int [] initialInputValues) {
	this.inputValues = initialInputValues;
  	this.numInputs = numInputs + 1; //Add one for bias variable
  	this.numOutputs = numOutputs;
  	initializeWeights();
  	forwardFeed(0);
    System.out.print("Hei");

  }
  
  
  
  
  public void train(double reward, int [] newInputValues, int prevAction){
	  
	  this.prevInputValues = inputValues;
	  this.inputValues = newInputValues;
	  
	  this.prevOutputS = outputS;
	  this.prevAction = prevAction;
	  this.prevHiddenS = hiddenS;
	  this.prevWeightInputToHidden = weightInputToHidden;
	  this.prevWeightHiddenToOutput = weightHiddenToOutput;
	  this.prevDeltaWeightHiddenInput = deltaWeightHiddenInput;
	  this.prevDeltaWeightHiddenOutput = deltaWeightHiddenOutput;
	  this.prevDeltaOutputS = deltaOutputS;
	  this.prevDeltaHiddenS = deltaHiddenS;
	  
	  this.singleError = new double [numOutputs]; 
	  int epoch = 0;


      do{
          forwardFeed(reward);
          backPropagation();
          
          epoch = epoch + 1;
      } while(totalError > totalErrorThreshold & epoch < MAX_EPOCH);
  }

  
  
  
  
  public void forwardFeed(double reward){
	  
	  totalError = 0;
	  
	  double [] outputS = new double [numOutputs];
			  
      for(int j = 1; j < numHidden; j++){ //Keep the bias node unchanged SPØR ERIK ARNE OM DENNE
          hiddenS[j] = 0;
          for(int i = 0; i < numInputs; i++){
              hiddenS[j] += inputValues[i] * weightInputToHidden[i][j];
          }
          hiddenS[j] = sigmoid(hiddenS[j]); //implement RELU?
      }

      for(int k = 0; k < numOutputs; k++){
          outputS[k] = 0;
          for(int j = 0; j < numHidden; j++){
              outputS[k] += hiddenS[j] * weightHiddenToOutput[j][k];
          }
          outputS[k] = sigmoid(outputS[k]);
          //Calculate error using Bellman-equation
          singleError[k] = prevOutputS[prevAction] - (reward + discountFactor * outputS[k]);
          totalError += singleError[k];
      }
      
      double maxOutput = Double.NEGATIVE_INFINITY;
      int maxIndex = -1;
      for(int k = 0; k < numOutputs; k++) {
    	  if(outputS[k] > maxOutput) {
    		  maxOutput = outputS[k];
    		  maxIndex = k;
    	  }
      }
      
      this.bestMove = maxIndex;
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

              weightHiddenToOutput[i][j] = argA + (r * (argB - argA));
              deltaWeightHiddenOutput[i][j] = 0.0;
          }
      }

  }



  public void backPropagation(){
	  
      //Compute the delta values of output layer
      for(int k = 0; k < numOutputs; k++){
          prevDeltaOutputS[k] = 0;
          prevDeltaOutputS[k] = singleError[k] * prevOutputS[k] * (1 - prevOutputS[k]);
      }

      //Update weights between hidden layer and output layer
      for(int k = 0; k < numOutputs; k++){
          for(int j = 0; j < numHidden; j++){
              prevDeltaWeightHiddenOutput[j][k] = momentumTerm * prevDeltaWeightHiddenOutput[j][k]
                      + learningRate * prevDeltaOutputS[k] * prevHiddenS[j];
              prevWeightHiddenToOutput[j][k] += prevDeltaWeightHiddenOutput[j][k];
          }
      }

      //Compute the delta values of hidden layer
      for(int j = 0; j < numHidden; j++){
          prevDeltaHiddenS[j] = 0;
          for(int k = 0; k < numOutputs; k++){
              prevDeltaHiddenS[j] += prevDeltaOutputS[k] * prevWeightHiddenToOutput[j][k];
          }
          prevDeltaHiddenS[j] = prevDeltaHiddenS[j] * prevHiddenS[j] * (1 - prevHiddenS[j]);
      }

      //Update weights between input layer and hidden layer
      for(int j = 1; j < numHidden; j++){
          for(int i = 0; i < numInputs; i++){
              prevDeltaWeightHiddenInput[i][j] = momentumTerm * prevDeltaWeightHiddenInput[i][j]
                      + learningRate * prevDeltaHiddenS[j] * prevInputValues[i];
              prevWeightInputToHidden[i][j] += prevDeltaWeightHiddenInput[i][j];
          }
      }
  }
  
  

  public double sigmoid(double x){
      int upperBound = 1;
      int lowerBound = 0;
      return (upperBound - lowerBound) / (1 + Math.pow(Math.E, -x)) + lowerBound;
  }
  
  
  public double ReLU(double x) {
	  return Math.max(0, x);
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
