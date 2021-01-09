package frc.robot.subsystems;

//Imports
import frc.robot.components.Limelight;
import frc.robot.subsystems.Chassis;
import frc.robot.subsystems.PowerCell;
import frc.robot.components.IMU;
import frc.robot.components.DistSensor;
import com.revrobotics.ControlType;

public class Vision{
    //Objects
    private Limelight limeLight;
    private IMU imu;
    private Chassis Drivetrain;
    private DistSensor distSensor; 
    //Variables
    public double kP= -0.1f;
    public double min_command = 0.05f;
    public double leftSide = 0;
    public double rightSide = 0;
    public double yaw;
    public double tx = 0;
    public double heading_error = 0;
    public double steering_adjust = 0;
    public double distance = 0;
    public boolean moveTheRobot = false;// used for testing will eventually be removed along with the if statement
    public int timer = 0;
    

    public void init(IMU inertialMessurmentUnit, Chassis ChassiS, Limelight limelighT, DistSensor distSensoR){
        imu = inertialMessurmentUnit;
        Drivetrain = ChassiS;
        limeLight = limelighT;
        distSensor = distSensoR;
    }
    public double getDistance(){
        if(limeLight.getDistance()>=distSensor.distSensorMaxDist){
        return limeLight.getDistance();
        }
        else{
            return distSensor.getRange();
        }
    }

    public void driveToTarget(double targetdistance) { // we should look into adding a pathfinding algorithem to allow for a more
        // efficiant approach, currently this is example code from the lielight website,
        // the code was written for C++ and was hopefully converted to java.
        while (timer < 1) {
            Drivetrain.timer = 0;
            Drivetrain.setControltype(ControlType.kPosition);
            timer++;
        }
        distance = distSensor.getRange();
        yaw = imu.yaw;
        tx = limeLight.getTx(); // the target xValue
        heading_error = -tx;
        steering_adjust = 0.0f;

        if (tx > 1.0) {
            steering_adjust = kP * heading_error - min_command;
        } else if (tx < 1.0) {
            steering_adjust = kP * heading_error + min_command;
        }

        leftSide += steering_adjust;
        rightSide -= steering_adjust;

        // check for if the imu is valid and if the drivetrain is able to drive
        if (moveTheRobot && getDistance() >= targetdistance && imu.isYawValid()) {
            Drivetrain.driveTrain.tankDrive(leftSide, rightSide);
        } 
        else {
            Drivetrain.driveTrain.tankDrive(0, 0);
        }
    }
}