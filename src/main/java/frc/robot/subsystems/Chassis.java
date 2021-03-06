package frc.robot.subsystems;

import com.revrobotics.*;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.components.*;
import frc.robot.*;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.geometry.*;
import edu.wpi.first.wpilibj.kinematics.*;

public class Chassis extends SubsystemBase{

    
    public CANEncoder lEncoder;
    public CANEncoder rEncoder;
    public CANSparkMax sparkMax1; // TBD
    public CANPIDController sparkMax1PID;
    public CANSparkMax sparkMax2;
    public CANPIDController sparkMax2PID;
    public CANSparkMax sparkMax3;
    public CANPIDController sparkMax3PID;
    public CANSparkMax sparkMax4;
    public CANPIDController sparkMax4PID;
    private double forwardInputSpeed = 0.25; //1;
    private double turningInputSpeed = 0.65;//0.65;
    public double robotSpeed = 0;

    
    
    private Solenoid gearShifter;
    private OperatorInterface oi;
    public DevilDifferential driveTrain;
    private IMU imu;
    private DifferentialDriveOdometry m_odometry;

    public double rampRate = 0.6;

    private double leftVelocity;
    private double rightVelocity;

    private double kP = 0.0002; //0.0001
    private double kI = 0.000000; //0.0 (We don't really need kI for now, maybe later)
    private double kD = 0.0000;
    private double kF = 0.00018; //0.000125
    private double deadband = 0.01;
    public double forwardSpeed = 0;
    public double sideSpeed = 0;
    private double currentRampRate = 0;
    private boolean highGear = false;
    public int timer = 0;
    public double LeftVolts;
    public double RightVolts;
    public double loggingForwardSpeed;
    public double loggingSideSpeed;

    public void setRampRate(double rr){
        if(rr != currentRampRate){
            currentRampRate = rr;
            sparkMax1.setClosedLoopRampRate(rr);
            sparkMax2.setClosedLoopRampRate(rr);
            sparkMax3.setClosedLoopRampRate(rr);
            sparkMax4.setClosedLoopRampRate(rr);
        }
    }
   
    public double distance(){
        return (Math.abs(lEncoder.getPosition()) + Math.abs(-rEncoder.getPosition())) / 2;
    }

    public void LogEncoders() {
        System.out.println(lEncoder.getPosition() + " " + rEncoder.getPosition());
    }
    public double[] getMotorTemps(){
        double temps[] = {sparkMax1.getMotorTemperature(), sparkMax2.getMotorTemperature(), sparkMax3.getMotorTemperature(), sparkMax4.getMotorTemperature()};
        return temps;
    }

    private boolean isSquaredInputs = false;

    
    public void Init(OperatorInterface oInterface, IMU Imu){
        imu = Imu;
        oi = oInterface;
        sparkMax1 = new CANSparkMax(11, MotorType.kBrushless); //ID 11
        sparkMax2 = new CANSparkMax(12, MotorType.kBrushless); //ID 12
        sparkMax3 = new CANSparkMax(13, MotorType.kBrushless); //ID 13
        sparkMax4 = new CANSparkMax(14, MotorType.kBrushless); //ID 14
        sparkMax1.restoreFactoryDefaults();
        sparkMax2.restoreFactoryDefaults();
        sparkMax3.restoreFactoryDefaults();
        sparkMax4.restoreFactoryDefaults();
        lEncoder = sparkMax2.getEncoder(); //old  new CANEncoder(sparkMax1);
        rEncoder = sparkMax1.getEncoder(); //old  new CANEncoder(sparkMax2);
        sparkMax3.follow(sparkMax1);
        sparkMax4.follow(sparkMax2);

    
        gearShifter = new Solenoid(Wiring.gearShifterSolenoid);
    

        sparkMax1PID = sparkMax1.getPIDController();
        sparkMax1PID.setReference(0, ControlType.kVelocity);
        sparkMax2PID = sparkMax2.getPIDController();
        sparkMax2PID.setReference(0, ControlType.kVelocity);
        sparkMax3PID = sparkMax3.getPIDController();
        sparkMax3PID.setReference(0, ControlType.kVelocity);
        sparkMax4PID = sparkMax4.getPIDController();
        sparkMax4PID.setReference(0, ControlType.kVelocity);

       setRampRate(0.6);

        sparkMax1PID.setP(kP);
        sparkMax1PID.setI(kI);
        sparkMax1PID.setD(kD);
        sparkMax1PID.setFF(kF);
        sparkMax1PID.setIZone(10, 0);
        sparkMax1PID.setIMaxAccum(20, 0);
        sparkMax1PID.setIAccum(0); //may need to be set\\

        sparkMax2PID.setP(kP);
        sparkMax2PID.setI(kI);
        sparkMax2PID.setD(kD);
        sparkMax2PID.setFF(kF);
        sparkMax2PID.setIZone(10, 0);
        sparkMax2PID.setIMaxAccum(20, 0);

        sparkMax3PID.setP(kP);
        sparkMax3PID.setI(kI);
        sparkMax3PID.setD(kD);
        sparkMax3PID.setFF(kF);
        sparkMax3PID.setIZone(10, 0);
        sparkMax3PID.setIMaxAccum(20, 0);

        sparkMax4PID.setP(kP);
        sparkMax4PID.setI(kI);
        sparkMax4PID.setD(kD);
        sparkMax4PID.setFF(kF);
        sparkMax4PID.setIZone(10, 0);
        sparkMax4PID.setIMaxAccum(20, 0);
        
        sparkMax1.setIdleMode(IdleMode.kBrake);
        sparkMax2.setIdleMode(IdleMode.kBrake);
        sparkMax3.setIdleMode(IdleMode.kBrake);
        sparkMax4.setIdleMode(IdleMode.kBrake);


        sparkMax1.setSmartCurrentLimit(40);
        sparkMax2.setSmartCurrentLimit(40);
        sparkMax3.setSmartCurrentLimit(40);
        sparkMax4.setSmartCurrentLimit(40);

        
        driveTrain = new DevilDifferential(sparkMax2PID, sparkMax1PID);
        driveTrain.setMaxOutput(5600);//5600 //NEO free speed 5700 RPM
        driveTrain.setExpiration(2.0);
    }

    public void setControltype(ControlType controlTypE){
        driveTrain.controlType = controlTypE;
    }
    public void teleopInit(){
        sparkMax1.setIdleMode(IdleMode.kBrake);
        sparkMax2.setIdleMode(IdleMode.kBrake);
        sparkMax3.setIdleMode(IdleMode.kBrake);
        sparkMax4.setIdleMode(IdleMode.kBrake);
        sparkMax1PID.setReference(0, ControlType.kVelocity);
        sparkMax2PID.setReference(0, ControlType.kVelocity);
        sparkMax3PID.setReference(0, ControlType.kVelocity);
        sparkMax4PID.setReference(0, ControlType.kVelocity);
        sparkMax1PID.setP(kP);
        sparkMax1PID.setI(kI);
        sparkMax1PID.setD(kD);
        sparkMax1PID.setFF(kF);
        sparkMax1PID.setIZone(10, 0);
        sparkMax1PID.setIMaxAccum(20, 0);
        sparkMax1PID.setIAccum(0); //may need to be set\\

        sparkMax2PID.setP(kP);
        sparkMax2PID.setI(kI);
        sparkMax2PID.setD(kD);
        sparkMax2PID.setFF(kF);
        sparkMax2PID.setIZone(10, 0);
        sparkMax2PID.setIMaxAccum(20, 0);

        sparkMax3PID.setP(kP);
        sparkMax3PID.setI(kI);
        sparkMax3PID.setD(kD);
        sparkMax3PID.setFF(kF);
        sparkMax3PID.setIZone(10, 0);
        sparkMax3PID.setIMaxAccum(20, 0);

        sparkMax4PID.setP(kP);
        sparkMax4PID.setI(kI);
        sparkMax4PID.setD(kD);
        sparkMax4PID.setFF(kF);
        sparkMax4PID.setIZone(10, 0);
        sparkMax4PID.setIMaxAccum(20, 0);
    }
    public void autoInit(double kp){
        sparkMax1.setIdleMode(IdleMode.kBrake);
        sparkMax2.setIdleMode(IdleMode.kBrake);
        sparkMax3.setIdleMode(IdleMode.kBrake);
        sparkMax4.setIdleMode(IdleMode.kBrake);
        sparkMax1PID.setReference(0, ControlType.kPosition);
        sparkMax2PID.setReference(0, ControlType.kPosition);
        sparkMax3PID.setReference(0, ControlType.kPosition);
        sparkMax4PID.setReference(0, ControlType.kPosition);
        sparkMax1PID.setP(kp);
        sparkMax2PID.setP(kp);
        sparkMax3PID.setP(kp);
        sparkMax4PID.setP(kp);
        sparkMax1PID.setFF(0);
        sparkMax2PID.setFF(0);
        sparkMax3PID.setFF(0);
        sparkMax4PID.setFF(0);

    }

    public void setKF(double kF){
        sparkMax1PID.setFF(kF);
        sparkMax2PID.setFF(kF);
        sparkMax3PID.setFF(kF);
        sparkMax4PID.setFF(kF);
    }

    public void DriveSystem(Joystick drive)
    {   
        while(timer <1){
            setControltype(ControlType.kVelocity);
            timer++;
        }
        if(oi.pilot.getRawButton(Buttons.X)){
            forwardInputSpeed = 0.25;
            turningInputSpeed = 0.25;
            leftVelocity = lEncoder.getVelocity();
            rightVelocity = -(-rEncoder.getVelocity());
            //System.out.println("R: " + rightVelocity + " " + "L: " + leftVelocity);
            robotSpeed = Math.max(Math.abs(leftVelocity),Math.abs(rightVelocity));
            if(robotSpeed < 0.60042069 * 5600)
            {
            highGear = false;
            setRampRate(0.1);
            }
        }
        else{
            turningInputSpeed = 0.65;//0.65
            forwardInputSpeed = 1; //1;
            highGear = true;
            setRampRate(rampRate);
            gearShift();
        }
        
        
        if(highGear){
            gearShifter.set(true);
        }
        else{
            gearShifter.set(false);
        }
            //Axis 0 for left joystick left to right
            //Axis 2 for left Trigger
            //Axis 3 for right Trigger
            forwardSpeed = oi.pilot.getRawAxis(Buttons.leftJoystick_y) * forwardInputSpeed;
            sideSpeed = -oi.pilot.getRawAxis(Buttons.rightJoystick_x) *turningInputSpeed;
            if(Math.abs(forwardSpeed) <= deadband)

            {
                forwardSpeed = 0;
            }
            if(sideSpeed >= -deadband && sideSpeed <= deadband)
            {
                sideSpeed = 0;
            }
            driveTrain.arcadeDrive(forwardSpeed, sideSpeed, isSquaredInputs);
    }


    public void gearShift(){
        if(oi.pilot.getRawAxis(Buttons.rightTrigger) >= 0.5) {
            highGear = true;
        }
        else{
            highGear = false;        }
    }

    public void move(double leftPosition, double rightPosition){
           sparkMax1PID.setReference(rightPosition, ControlType.kPosition);
           sparkMax2PID.setReference(leftPosition, ControlType.kPosition);

            // loggingForwardSpeed = speed;
            // loggingSideSpeed = rotation;
    }
    public void stopMove(){
        sparkMax1PID.setReference(0, ControlType.kVelocity);
        sparkMax2PID.setReference(0, ControlType.kVelocity);

         // loggingForwardSpeed = speed;
         // loggingSideSpeed = rotation;
 }
    public void resetEncoders(){
        lEncoder.setPosition(0);
        rEncoder.setPosition(0);
    }

    public void initOdometry()
    {
        lEncoder.setPosition(0);
        rEncoder.setPosition(0);
        imu.zeroYaw();
        
        Rotation2d yaw = new Rotation2d(imu.yaw);
        m_odometry = new DifferentialDriveOdometry(yaw);
        //Neccessary for advanced auto new Pose2d(0,0 new Rotation2d())
        //lEncoder.setDistancePerPulse(DriveConstants.kEncoderDistancePerPulse); This don't exist                                                                       k
        //rEncoder.setDistancePerPulse(DriveConstants.kEncoderDistancePerPulse); This doesn't exist                                                                      k
        
    }

    public double R2M(double rotations){  //gear ratio: 15.2:1, wheel diameter 5.8in
        double out = rotations/15.2*5.8*Math.PI/39.37;
        return out;
    }

    public Pose2d updateOdometry()
    {
        return m_odometry.update(new Rotation2d(imu.yaw), R2M(lEncoder.getPosition()), R2M(-rEncoder.getPosition()));
    }

    public Pose2d getPose() {
        return m_odometry.getPoseMeters();
      }

    public DifferentialDriveWheelSpeeds getWheelSpeeds() {
        //return new DifferentialDriveWheelSpeeds(lEncoder.getVelocity(), rEncoder.getVelocity());
        return new DifferentialDriveWheelSpeeds(R2M(lEncoder.getVelocity())/60, R2M(-rEncoder.getVelocity())/60);
    }
    public void tankDriveVolts(double leftVolts, double rightVolts) {
        sparkMax1.setVoltage(leftVolts);
        sparkMax2.setVoltage(-rightVolts);
        LeftVolts = leftVolts;
        RightVolts = rightVolts;
        driveTrain.feed();
      }
    public void resetOdometry(Pose2d pose) {
        resetEncoders();
         m_odometry.resetPosition(pose, new Rotation2d(imu.yaw));
    }
      
    public void disabled()
    {
        sparkMax1.setIdleMode(IdleMode.kCoast);
        sparkMax2.setIdleMode(IdleMode.kCoast);
        sparkMax3.setIdleMode(IdleMode.kCoast);
        sparkMax4.setIdleMode(IdleMode.kCoast);
    }

    
      public double getAverageEncoderDistance() {
        return (Math.abs(lEncoder.getPosition()) + Math.abs(-rEncoder.getPosition())) / 2.0; 
      }
    
      /**
       * Gets the left drive encoder.
       *
       * @return the left drive encoder
       */
      public CANEncoder getLeftEncoder() {
        return lEncoder;
      }
    
      /**
       * Gets the right drive encoder.
       *
       * @return the right drive encoder
       */
      public CANEncoder getRightEncoder() {
        return rEncoder;
      }
    
      /**
       * Sets the max output of the drive.  Useful for scaling the drive to drive more slowly.
       *
       * @param maxOutput the maximum output to which the drive will be constrained
       */
      public void setMaxOutput(double maxOutput) {
        driveTrain.setMaxOutput(maxOutput);
      }
    
      /**
       * Zeroes the heading of the robot.
       */

    
      /**
       * Returns the heading of the robot.
       *
       * @return the robot's heading in degrees, from -180 to 180
       */
      public double getHeading() {
        return imu.yaw;
      }
    
      /**
       * Returns the turn rate of the robot.
       *
       * @return The turn rate of the robot, in degrees per second
       */
      public double getTurnRate() {
        return -imu.turnRate;
      }
}
