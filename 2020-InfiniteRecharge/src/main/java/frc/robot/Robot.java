/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.SparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.wpilibj.TimedRobot;

import frc.robot.Components.IMU;
import frc.robot.subsystems.PowerCell;
import com.ctre.phoenix.motorcontrol.*;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import frc.robot.subsystems.Chassis;
import io.github.oblarg.oblog.*;
import io.github.oblarg.oblog.annotations.Config;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

  private IMU imu;
  public OperatorInterface oi;
  private Chassis driveTrain;
  private boolean chassisEnable = false;
  private boolean ImuEnable = false;
  private boolean robotInitialized = false;
  private boolean powerCellEnable = false;
  private PowerCell powerCell;
  @Config 
  public void Enable_PowerCell(boolean enable){
    powerCellEnable = enable;
  }
  @Config
  public void Enable_IMU(boolean enable){
    ImuEnable = enable;
  }
  @Config
  public void Enable_Chassis(boolean enable){
    chassisEnable = enable;
  }
  
  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    imu = new IMU();
    oi = new OperatorInterface();
    powerCell = new PowerCell();
    Logger.configureLoggingAndConfig(this, false);
    
    
    
    
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    
    Logger.updateEntries();
    System.out.println(ImuEnable);
  }
    
  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    initialize();
    
    
    
    if(ImuEnable){
      imu.zeroYaw();
    }
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    if(ImuEnable){
      imu.getvalues();
    }
    
  }
    
    
  
  /**
   * This function is called periodically during operator control.
   */
  
  @Override
  public void teleopInit() {
    if(robotInitialized == false){
      initialize();
    }
  }

  @Override
  public void teleopPeriodic() {
    if(ImuEnable){
      imu.getvalues();
    }
    if(powerCellEnable){
      powerCell.intake();
      powerCell.shoot();
    }
  }
  
  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testInit() {
    initialize();
  }
  @Override
  public void testPeriodic() 
  {
    if(chassisEnable){
      driveTrain.DriveSystem(oi.pilot);
    }
  }

  @Override
  public void disabledInit(){

  }
  @Override
  public void disabledPeriodic(){

  }
  public void initialize(){
    robotInitialized = true;
    if(ImuEnable){
      imu.init();
    }
    System.out.println("Initilied");
  
  if(powerCellEnable){
      powerCell.init();
    }
  }
}
