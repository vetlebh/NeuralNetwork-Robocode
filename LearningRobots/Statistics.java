package LearningRobots;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import robocode.RobocodeFileOutputStream;

public class Statistics {
	public static int[] scoreTable;
	private static int iterator = 0;
	private static int tableSize;
	
	public Statistics(int nrOfBattles) {
		if (iterator == 0) {
			scoreTable = new int[nrOfBattles];
		}
		this.tableSize = nrOfBattles;
	}
	
	public void updateTable(int win) {
		System.out.println("win: " + win);
		scoreTable[iterator] = win;
		
		iterator ++;
	}
	
	public int getValue(int index) {
		return scoreTable[index];
	}
	
	public void printValues() {
		for (int i = 0; i < tableSize; i++) {
        	System.out.println(i + ": " + scoreTable[i]);
		}
	}
	
	public void saveStat(File file)
    {
        PrintStream w = null;
        try
        {
            w = new PrintStream(new RobocodeFileOutputStream(file));
            for (int i = 0; i < tableSize; i++)
            	w.println(new Double(scoreTable[i]));
 
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
