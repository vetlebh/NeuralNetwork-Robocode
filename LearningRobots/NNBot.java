package LearningRobots;


import robocode.*;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.BattleEndedEvent;

import java.awt.Color;
import java.awt.geom.Point2D;


import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;



public class NNBot extends AdvancedRobot {
	
	//Reinforcement learning objects
	NeuralNetRobocode NN;
    
    //Target object
    Enemy target;

    //Reward variable
    double reward;
    
    //Dimension variables
    int numInput = RobotStates.dimStates;
    int numOutput = RobotActions.numActions;

    //Status variables
    int hitWall = 0;
    int hitByEnemy = 0;
    
    //Learning variables
    double epsilon = 0.5;
    
    //Initial input values to Neural Net
    int [] initialInputValues = new int[numInput];


    //###################//
    //	 main function	 //
    //###################//
    
    public void run(){
        //Set colors of robot
        setColors(Color.green, Color.orange, Color.blue);

        //Independent moving of gun and radar
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        
        //Initialization of game objectives
        target = new Enemy();
        NN = new NeuralNetRobocode(numInput, numOutput, initialInputValues); 
        
        
        target.distance = 1000; 
        
        System.out.print("Hei");
        

        //Play game
        while(true){
        	System.out.print("Hei");
        	robotMovement();
        	radarMovement();
        	execute();
        }
    }
	
    //Movement of robot
	public void robotMovement(){
        
        int[] state = getState();
        System.out.print(state);
        int action = chooseMove();
        NN.train(reward, state, action);
        
        hitByEnemy = 0;
        hitWall = 0;
        reward = 0.0;
        
        switch (action)
        {
            case RobotActions.robotAhead:
                 setAhead(RobotActions.robotMoveDistanceLong);
                 break;
            case RobotActions.robotBack:
                 setBack(RobotActions.robotMoveDistanceShort);
                 break;
            case RobotActions.turnLeft:
                 setTurnLeft(RobotActions.robotTurnDeg);
                 break;
            case RobotActions.turnRight:
                 setTurnRight(RobotActions.robotTurnDeg);
                 break;
        }
    }
	
	public int chooseMove() {
		double rand = Math.random();
        if (epsilon > rand){
            return (int) Math.random() * numOutput; //verify this
        }
        else{
            return NN.bestMove;
        }
	}
	
	//Simple radar
    public void radarMovement() {
            setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
    }

    private int[] getState() {
    	int[] state = new int [numInput];
        state[0] = RobotStates.getHeading(getHeading());
        state[1] = RobotStates.getEnemyDistance(target.distance);
        state[2] = RobotStates.getEnemyBearing(target.bearing);
        state[3] = hitWall;
        state[4] = hitByEnemy;
        return state;
    }

    double normaliseBearing(double ang) {
    	if(ang > Math.PI) ang -= 2*Math.PI;
    	if(ang < -Math.PI)ang += 2*Math.PI;
    	return ang;
    }

	//returns the distance between two x,y coordinates
	public double getrange(double x1, double y1, double x2, double y2) {
	    double xo = x2 - x1;
	    double yo = y2 - y1;
	    double h = Math.sqrt(xo * xo + yo * yo);
	    return h;
}
	//Actions to do when enemy robot is scanned
	public void onScannedRobot(ScannedRobotEvent e) {
        
            //Gets the bearing to the point where the enemy is
            double absbearing_rad = (getHeadingRadians() + e.getBearingRadians()) % (2 * Math.PI);
           
            //Sets enemy information
            target.name = e.getName();
            double h = normaliseBearing(e.getHeadingRadians() - target.head);
            h = h / (getTime() - target.ctime);
            target.changehead = h;
            target.x = getX() + Math.sin(absbearing_rad) * e.getDistance();
            target.y = getY() + Math.cos(absbearing_rad) * e.getDistance();
            target.bearing = e.getBearingRadians();
            target.head = e.getHeadingRadians();
            target.ctime = getTime(); 
            target.speed = e.getVelocity();
            target.distance = e.getDistance();
            target.energy = e.getEnergy();
        
        basicShooter(e);
    }
	
    //Basic gun
    public void basicShooter(ScannedRobotEvent e) {
    	
    	double firePower = 400 / target.distance;
    	
    	long time;
	    long nextTime;
	    Point2D.Double p;
	    p = new Point2D.Double(target.x, target.y);
	    
	    for (int i = 0; i < 20; i++) {
	    	nextTime = (int)Math.round((getrange(getX(),getY(),p.x,p.y)/(20-(3*firePower))));
	        time = getTime() + nextTime - 10;
	        p = target.guessPosition(time);
	    }
	    
	    //Offsets the gun by the angle to the next shot based on linear targeting provided by the enemy class
	    double gunOffset = getGunHeadingRadians() - (Math.PI/2 - Math.atan2(p.y - getY(),p.x -  getX()));
	    setTurnGunLeftRadians(normaliseBearing(gunOffset));
	    setFire(firePower);
    }
    
    
    
    //ACTION FUNCTIONS / Using constant rewards
    
    public void onHitWall(HitWallEvent e) {
    	hitWall = 1;
        reward += -2;
    }

    public void onBulletHit(BulletHitEvent e) {
    	reward += 6;
    }
    
    public void onBulletMissed(BulletMissedEvent e) {
        reward -= 3;
    }
    
    public void onHitByBullet(HitByBulletEvent e) {
        hitByEnemy  = 1;
        reward -= 6;
    }

    public void onHitRobot(HitRobotEvent e) {
        reward += -3.0;
    }
    
    public void onWin(WinEvent event) {
    	reward += 15;
    }

    public void onDeath(DeathEvent event) {
    	reward -= 15;
    }
    
    public void onRobotDeath(RobotDeathEvent e)	{
        target.distance = 10000;
    }
   
    
    public void onBattleEnded(BattleEndedEvent event) {
    }
    
    //Advanced gun function, NOT IN USE
    public void circularTargeting(ScannedRobotEvent e){
        double bulletPower = Math.min(3.0, getEnergy());
        double myX = getX();
        double myY = getY();
        double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
        double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
        double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);
        double oldEnemyHeading = 0; //correct this
        double enemyHeading = e.getHeadingRadians();
        double enemyHeadingChange = enemyHeading - oldEnemyHeading;
        double enemyVelocity = e.getVelocity();
        oldEnemyHeading = enemyHeading;

        double deltaTime = 0;
        double battleFieldHeight = getBattleFieldHeight(),
                battleFieldWidth = getBattleFieldWidth();
        double predictedX = enemyX, predictedY = enemyY;
        
        while((++deltaTime) * (20.0 - 3.0 * bulletPower) <
                Point2D.Double.distance(myX, myY, predictedX, predictedY)){
        	
            predictedX += Math.sin(enemyHeading) * enemyVelocity;
            predictedY += Math.cos(enemyHeading) * enemyVelocity;
            enemyHeading += enemyHeadingChange;
            if(	predictedX < 18.0
                    || predictedY < 18.0
                    || predictedX > battleFieldWidth - 18.0
                    || predictedY > battleFieldHeight - 18.0){

                predictedX = Math.min(Math.max(18.0, predictedX),
                        battleFieldWidth - 18.0);
                predictedY = Math.min(Math.max(18.0, predictedY),
                        battleFieldHeight - 18.0);
                break;
            }
        }
        double theta = Utils.normalAbsoluteAngle(Math.atan2(
                predictedX - getX(), predictedY - getY()));

        setTurnRadarRightRadians(Utils.normalRelativeAngle(
                absoluteBearing - getRadarHeadingRadians()));
        setTurnGunRightRadians(Utils.normalRelativeAngle(
                theta - getGunHeadingRadians()));
        fire(3); 
    }
}	

