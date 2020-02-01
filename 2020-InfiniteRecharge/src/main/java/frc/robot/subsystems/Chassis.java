package frc.robot.subsystems;

import javax.rmi.ssl.SslRMIClientSocketFactory;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import frc.robot.widgets.MotorWidget;
import frc.robot.widgets.SCGWidget;
import io.github.oblarg.oblog.annotations.Config;
import io.github.oblarg.oblog.annotations.Log;

public class Chassis {
    // @Log.SpeedController
    // private WPI_TalonSRX motorFL;
    // @Log.SpeedController
    // private WPI_TalonSRX motorFR;
    private CANSparkMax sparkMax1; // TBD
    private CANPIDController sparkMax1PID;
    private CANSparkMax sparkMax2;
    private CANPIDController sparkMax2PID;
    private CANSparkMax sparkMax3;
    private CANPIDController sparkMax3PID;
    private CANSparkMax sparkMax4;
    private CANPIDController sparkMax4PID;
    
    private DifferentialDrive driveTrain;

    private ShuffleboardTab tab;

    private MotorWidget widget1;
    private MotorWidget widget2;
    private MotorWidget widget3;
    private MotorWidget widget4;
    private SCGWidget widget5;
    private SCGWidget widget6;

    private SpeedControllerGroup leftMotors;
    private SpeedControllerGroup rightMotors;
    
    public Chassis()
    {
        sparkMax1 = new CANSparkMax(11, MotorType.kBrushless);
        sparkMax2 = new CANSparkMax(12, MotorType.kBrushless);
        sparkMax3 = new CANSparkMax(13, MotorType.kBrushless);
        sparkMax4 = new CANSparkMax(14, MotorType.kBrushless);

        leftMotors = new SpeedControllerGroup(sparkMax1, sparkMax3);
        rightMotors = new SpeedControllerGroup(sparkMax2, sparkMax4);
        
        widget1 = new MotorWidget(sparkMax1, "Motor 1");
        widget2 = new MotorWidget(sparkMax2, "Motor 2");
        widget3 = new MotorWidget(sparkMax3, "Motor 3");
        widget4 = new MotorWidget(sparkMax4, "Motor 4");
        widget5 = new SCGWidget(leftMotors, "Left Motors");
        widget6 = new SCGWidget(rightMotors, "Right Motors");
    }

    public void Init() {
        
        sparkMax1PID = sparkMax1.getPIDController();
        
        sparkMax2PID = sparkMax2.getPIDController();
        
        sparkMax3PID = sparkMax3.getPIDController();
        
        sparkMax4PID = sparkMax4.getPIDController();

        sparkMax1PID.setP(1);
        sparkMax1PID.setI(0);
        sparkMax1PID.setD(0);

        sparkMax2PID.setP(1);
        sparkMax2PID.setI(0);
        sparkMax2PID.setD(0);

        sparkMax3PID.setP(1);
        sparkMax3PID.setI(0);
        sparkMax3PID.setD(0);

        sparkMax4PID.setP(1);
        sparkMax4PID.setI(0);
        sparkMax4PID.setD(0);

        
        leftMotors.setInverted(true);
        rightMotors.setInverted(true);
        
        driveTrain = new DifferentialDrive(leftMotors, rightMotors);

        tab = Shuffleboard.getTab("Drive Train");

        
    }
    public void DriveSystem(Joystick drive)
    {
        DriveSystem(drive, "Arcade Drive");
    }

    public void DriveSystem(Joystick drive, String mode)
    {
        //System.out.println(mode);
        switch(mode)
        {
             case "Tank Drive":
            driveTrain.tankDrive(-(drive.getRawAxis(1)),-(drive.getRawAxis(5)));
            System.out.println(drive.getRawAxis(1));
            System.out.println(drive.getRawAxis(5));
             break;

             case "Arcade Drive":
             if(drive == null)
             {
                 System.out.println("Your controller is Null dimwit!!!!");
             }
             else
             {
                driveTrain.arcadeDrive(-(drive.getRawAxis(1)), drive.getRawAxis(4));
                System.out.println(drive.getRawAxis(1));
                System.out.println(drive.getRawAxis(4));
             }
             
             break;

             case "Curvature Drive":
             driveTrain.curvatureDrive(-(drive.getRawAxis(1)), drive.getRawAxis(4), true);
             System.out.println(drive.getRawAxis(1));
             System.out.println(drive.getRawAxis(4));
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
        }
    }
}