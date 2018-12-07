package LearningRobots;



public class RobotStates
{
	 //Game states
     public static final int numHeading = 4; 
     public static final int numEnemyDistance = 5; 
     public static final int numEnemyBearing = 4;
     public static final int numHitWall = 2;
     public static final int numHitByBullet = 2;
     public static final int numStates;
     public static final int dimStates;
     public static final int states[][][][][];
  
     //Initialize the state matrix
     static{
       states = new int[numHeading][numEnemyDistance][numEnemyBearing][numHitWall][numHitByBullet];
       int state = 0;
       for (int a = 0; a < numHeading; a++)
         for (int b = 0; b < numEnemyDistance; b++)
           for (int c = 0; c < numEnemyBearing; c++)
             for (int d = 0; d < numHitWall; d++)
               for (int e = 0; e < numHitByBullet; e++)
                    states[a][b][c][d][e] = state++;

       numStates = state;
       dimStates = 5;
   }
  
   //Transfer the heading into four directions
   public static int getHeading(double heading)
   {
       double angle = 360 / numHeading;
       double newHeading = heading + angle / 2;
       if (newHeading > 360.0)
         newHeading -= 360.0;
       return (int)(newHeading / angle);
       
   }
  
   //Transfer the bearing into four directions
   public static int getEnemyBearing(double bearing)
   {
       double PIx2 = Math.PI * 2;
       if (bearing < 0)
         bearing = PIx2 + bearing;
       double angle = PIx2 / numEnemyBearing;
       double newBearing = bearing + angle / 2;
       if (newBearing > PIx2)
         newBearing -= PIx2;
       return (int)(newBearing / angle); 
   }
   
   
   //Transfer the distance into five segments
   public static int getEnemyDistance(double val)
   {
       int distance = (int)(val / 100.0);
       if (distance > numEnemyDistance - 1)
         distance = numEnemyDistance - 1;
       return distance;
   }   
  
}

