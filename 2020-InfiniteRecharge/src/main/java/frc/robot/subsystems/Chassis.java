package frc.robot.subsystems;

import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANEncoder;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveOdometry;
import frc.robot.components.DevilDifferential;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import frc.robot.OperatorInterface;
import frc.robot.Wiring;
import frc.robot.widgets.MotorWidget;
import frc.robot.widgets.SCGWidget;
import frc.robot.components.IMU;
import edu.wpi.first.wpilibj.Solenoid;
import io.github.oblarg.oblog.Loggable;
import io.github.oblarg.oblog.annotations.Config;
import io.github.oblarg.oblog.annotations.Log;

public class Chassis implements Loggable {

    @Log
    private String Gear = "Low";
    private CANEncoder lEncoder;
    private CANEncoder rEncoder;
    private CANSparkMax sparkMax1;
    private CANPIDController sparkMax1PID;
    private CANSparkMax sparkMax2;
    private CANPIDController sparkMax2PID;
    private CANSparkMax sparkMax3;
    private CANPIDController sparkMax3PID;
    private CANSparkMax sparkMax4;
    private CANPIDController sparkMax4PID;
    @Log
    private double shiftUp;
    @Log
    public double ShiftDown;
    private boolean isShifted;
    @Log
    private boolean manualShift;
    @Log.Exclude
    private Solenoid gearShifter;
    private OperatorInterface oi;
    private IMU imu;
    private DifferentialDriveOdometry m_odometry;
    private DevilDifferential driveTrain;

    private ShuffleboardTab tab; // Is used, just isn't recognizing that it is being used

    private MotorWidget widget1;
    private MotorWidget widget2;
    private MotorWidget widget3;
    private MotorWidget widget4;
    private SCGWidget widget5;
    private SCGWidget widget6;
    private boolean button6 = false;

    private SpeedControllerGroup leftMotors;
    private SpeedControllerGroup rightMotors;
    @Log.Graph
    private double leftVelocity;
    @Log.Graph
    private double rightVelocity;
    @Log.Dial(max = 6000, min = 0)
    private double velocity;
    @Log.Graph
    public double motor1Current;
    @Log.Graph
    public double motor2Current;
    @Log.Graph
    public double motor3Current;
    @Log.Graph
    public double motor4Current;

    @Log.Dial
    private double motor1Temp;
    @Log.Dial
    private double motor2Temp;
    @Log.Dial
    private double motor3Temp;
    @Log.Dial
    private double motor4Temp;

    private boolean shift = true;

    @Log
    private double kP = 0.0002;
    @Log
    private double kI = 0.0;
    @Log
    private double kD = 0.0000;
    @Log
    private double kF = 0.00018;

    @Log
    private double deadband = 0.01;

   // @Config.ToggleSwitch(defaultValue = true)
    public void Enable_Shifting(boolean enable){
        shift = enable;
    }
    //@Config
    public void Shifting_points(double up, double down){
        shiftUp = up;
        ShiftDown = down;
    }

    //@Config
    public void set_PID(double P, double I, double D, double F, double imax, double izone)
    {
        
        kP = P;
        kI = I;
        kD = D;
        kF = F;

        sparkMax1PID.setP(P);
        sparkMax2PID.setP(P);
        sparkMax3PID.setP(P);
        sparkMax4PID.setP(P);

        sparkMax1PID.setI(I);
        sparkMax2PID.setI(I);
        sparkMax3PID.setI(I);
        sparkMax4PID.setI(I);

        sparkMax1PID.setD(D);
        sparkMax2PID.setD(D);
        sparkMax3PID.setD(D);
        sparkMax4PID.setD(D);

        sparkMax1PID.setFF(F);
        sparkMax2PID.setFF(F);
        sparkMax3PID.setFF(F);
        sparkMax4PID.setFF(F);

        sparkMax1PID.setIZone(izone, 0);
        sparkMax1PID.setIMaxAccum(imax, 0);
        sparkMax2PID.setIZone(izone, 0);
        sparkMax2PID.setIMaxAccum(imax, 0);
        sparkMax3PID.setIZone(izone, 0);
        sparkMax3PID.setIMaxAccum(imax, 0);
        sparkMax4PID.setIZone(izone, 0);
        sparkMax4PID.setIMaxAccum(imax, 0);
        
    }

    //@Config
    public void setDeadband(double dB)
    {
        deadband = dB;
    }

    @Log
    private double setSpeed = 0;

    //@Config
    public void setMySpeed(double sS)
    {
        setSpeed = sS;
    }

    @Log
    private boolean isSquaredInputs = true;

    //@Config
    public void wantInputsSquared(boolean iS)
    {
        isSquaredInputs = iS;
    }

    public void Init(OperatorInterface oInterface, IMU Imu) {
        imu = Imu;
        shiftUp = 5000;
        // setSpeed = sS;
        shiftUp = 5000;
        ShiftDown = 3500;
        oi = oInterface;
        sparkMax1 = new CANSparkMax(11, MotorType.kBrushless); // ID 11
        sparkMax2 = new CANSparkMax(12, MotorType.kBrushless); // ID 12
        sparkMax3 = new CANSparkMax(13, MotorType.kBrushless); // ID 13
        sparkMax4 = new CANSparkMax(14, MotorType.kBrushless); // ID 14
        lEncoder = new CANEncoder(sparkMax1);
        rEncoder = new CANEncoder(sparkMax2);
        sparkMax3.follow(sparkMax1);
        sparkMax4.follow(sparkMax2);

        leftMotors = new SpeedControllerGroup(sparkMax1); // (sparkMax1, sparkMax3) for working code
        rightMotors = new SpeedControllerGroup(sparkMax2); // (sparkMax2, sparkMax4) for working code

        widget1 = new MotorWidget(sparkMax1, "Motor 1");
        widget2 = new MotorWidget(sparkMax2, "Motor 2");
        widget3 = new MotorWidget(sparkMax3, "Motor 3");
        widget4 = new MotorWidget(sparkMax4, "Motor 4");
        widget5 = new SCGWidget(leftMotors, "Left Motors");
        widget6 = new SCGWidget(rightMotors, "Right Motors");

        gearShifter = new Solenoid(Wiring.gearShifterSolenoid);

        sparkMax1PID = sparkMax1.getPIDController();
        sparkMax1PID.setReference(0, ControlType.kVelocity);
        sparkMax2PID = sparkMax2.getPIDController();
        sparkMax2PID.setReference(0, ControlType.kVelocity);
        sparkMax3PID = sparkMax3.getPIDController();
        sparkMax3PID.setReference(0, ControlType.kVelocity);
        sparkMax4PID = sparkMax4.getPIDController();
        sparkMax4PID.setReference(0, ControlType.kVelocity);

        sparkMax1.setOpenLoopRampRate(0.4);
        sparkMax2.setOpenLoopRampRate(0.4);
        sparkMax3.setOpenLoopRampRate(0.4);
        sparkMax4.setOpenLoopRampRate(0.4);

        sparkMax1.setClosedLoopRampRate(1);
        sparkMax2.setClosedLoopRampRate(1);
        sparkMax3.setClosedLoopRampRate(1);
        sparkMax4.setClosedLoopRampRate(1);

        sparkMax1.setClosedLoopRampRate(1);
        sparkMax2.setClosedLoopRampRate(1);
        sparkMax3.setClosedLoopRampRate(1);
        sparkMax4.setClosedLoopRampRate(1);

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

        leftMotors.setInverted(true);
        rightMotors.setInverted(true);

        sparkMax1.setSmartCurrentLimit(40);
        sparkMax2.setSmartCurrentLimit(40);
        sparkMax3.setSmartCurrentLimit(40);
        sparkMax4.setSmartCurrentLimit(40);

        driveTrain = new DevilDifferential(sparkMax1PID, sparkMax2PID);
        driveTrain.setMaxOutput(5600); // NEO free speed 5700 RPM

        tab = Shuffleboard.getTab("Chassis");

    }

    public void DriveSystem(Joystick drive) {
        DriveSystem(drive, "Arcade Drive");
    }

    public void DriveSystem(Joystick drive, String mode) {
        // graphing the motor parameters
        motor1Temp = sparkMax1.getMotorTemperature();
        motor2Temp = sparkMax2.getMotorTemperature();
        motor3Temp = sparkMax3.getMotorTemperature();
        motor4Temp = sparkMax4.getMotorTemperature();
        motor1Current = sparkMax1.getOutputCurrent();
        motor2Current = sparkMax2.getOutputCurrent();
        motor3Current = sparkMax3.getOutputCurrent();
        motor4Current = sparkMax4.getOutputCurrent();
        if(shift){
        gearShift();        
        }
        else{gearShifter.set(false);
        }
        switch(mode)
        {
             case "Tank Drive":
            driveTrain.tankDrive(-(oi.getPilotX()),-(oi.pilot.getRawAxis(5)), isSquaredInputs);
            
             break;

             case "Arcade Drive":
             if(drive == null)
             {
                 System.out.println("Your controller is Null dimwit!!!!");
             }
             else
             {
                double forwardSpeed = (oi.getPilotY());
                double turnSpeed = -(oi.getPilotZ());
     
                driveTrain.arcadeDrive(forwardSpeed, turnSpeed, isSquaredInputs);

            }

            break;

        case "Curvature Drive":
            driveTrain.curvatureDrive(-(oi.getPilotY()), oi.getPilotZ(), true);

            break;

        case "Shuffle Drive Individual":
            widget1.changeOutput();
            widget2.changeOutput();
            widget3.changeOutput();
            widget4.changeOutput();
            break;

        case "Shuffle Drive Control Groups":
            widget5.changeOutput();
            widget6.changeOutput();
            break;

        case "Scott Drive":
            // Axis 0 for left joystick left to right
            // Axis 2 for left bumper
            // Axis 3 for right bumper
            double forwardSpeed = oi.pilot.getRawAxis(3);
            double backwardSpeed = oi.pilot.getRawAxis(2);
            double sideSpeed = -oi.getPilotX();
            if (forwardSpeed <= deadband) {
                forwardSpeed = 0;
            }
            if (backwardSpeed <= deadband) {
                backwardSpeed = 0;
            }
            if (sideSpeed >= -deadband && sideSpeed <= deadband) {
                sideSpeed = 0;
            }
            if (backwardSpeed > forwardSpeed) {
                sideSpeed = -sideSpeed;
            }
            driveTrain.arcadeDrive(forwardSpeed - backwardSpeed, sideSpeed);
            break;
        }
    }

    public void manualShift() {
        button6 = oi.pilot.getRawButtonPressed(6);
        if (button6 && isShifted) {
            isShifted = false;
        } else if (button6) {
            isShifted = true;
        }
        if (isShifted) {
            gearShifter.set(true);
        } else {
            gearShifter.set(false);
        }
    }

    public void gearShift() {
        leftVelocity = lEncoder.getVelocity();
        rightVelocity = -(rEncoder.getVelocity());

        velocity = Math.abs((leftVelocity + rightVelocity) / 2);

        if (velocity >= shiftUp) {
            gearShifter.set(true);
            Gear = "High";
        } else if (velocity <= ShiftDown) {
            gearShifter.set(false);
            Gear = "Low";

        }
    }

    public void move(double speed, double rotation) {
        sparkMax1.set(speed);
        sparkMax2.set(-speed);
    }

    public void initOdometry() {
        imu.zeroYaw();
        Rotation2d yaw = new Rotation2d(imu.getYaw());
        m_odometry = new DifferentialDriveOdometry(yaw);
        // Neccessary for advanced auto new Pose2d(0,0 new Rotation2d())
    }

    public double R2M(double rotations) // gear ratio: 15.2:1
    { // wheel diameter 5.8in
        double out = rotations / 15.2 * 5.8 * Math.PI / 39.37;
        return out;
    }

    public Pose2d updateOdometry() {
        return m_odometry.update(new Rotation2d(imu.getYaw()), R2M(lEncoder.getPosition()),
                R2M(rEncoder.getPosition()));
    }
}
