package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import frc.robot.OperatorInterface;
import frc.robot.Wiring;
import io.github.oblarg.oblog.Loggable;
import io.github.oblarg.oblog.annotations.Config;
import io.github.oblarg.oblog.annotations.Log;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;


public class Climber implements Loggable {
    private OperatorInterface oi;
    private TalonSRX barRider;
    private TalonFX winch;
    @Log
    private double winchRpms = 0;
    @Log
    private double balancerPercent;

    private final int TIMEOUT = 0;
    private final double cLR = 0.1;
    
	//Shuffleboard configs for winch and bar rider
  
	
	@Config
    private void winch_PID(double Rpms){
        
        winchRpms = Rpms;
    }
    @Config
    private void Balancer_RPMS( double OutputPercent){
        balancerPercent = OutputPercent;
    }

    public void ClimberInit(OperatorInterface operatorinterface)
    {
        barRider = new WPI_TalonSRX(Wiring.barRider);
        winch = new TalonFX(Wiring.winch);
        oi = operatorinterface;

    winch.set(TalonFXControlMode.PercentOutput, 0);	
    winch.configClosedloopRamp(cLR, TIMEOUT);
    winch.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder);
    winch.configNominalOutputForward(0, TIMEOUT);
    winch.configNominalOutputReverse(0, TIMEOUT);
    winch.configPeakOutputForward(+1, TIMEOUT);
    winch.configPeakOutputReverse(-1, TIMEOUT);
    winch.setNeutralMode(NeutralMode.Brake);

    barRider.set(ControlMode.PercentOutput, 0);	
    barRider.configClosedloopRamp(cLR, TIMEOUT);
    barRider.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder);
    barRider.configNominalOutputForward(0, TIMEOUT);
    barRider.configNominalOutputReverse(0, TIMEOUT);
    barRider.configPeakOutputForward(+1, TIMEOUT);
    barRider.configPeakOutputReverse(-1, TIMEOUT);
    barRider.setNeutralMode(NeutralMode.Brake);
    barRider.enableCurrentLimit(true);
	barRider.configPeakCurrentLimit(75,TIMEOUT);
	barRider.configContinuousCurrentLimit(40, TIMEOUT);
	barRider.configPeakCurrentDuration(1800,TIMEOUT);

    }

    public void stopBarrider(){
        barRider.set(ControlMode.PercentOutput, 0);
    }

    public void holdwinch(){
       
        winch.set(TalonFXControlMode.PercentOutput, 0);
    }
    /*Main method of climber class operates all functions of climber*/
    public void drive()    {
        Balance();

         

        if(oi.copilot.getRawButton(3)) { //Told you Operator Interface button controls didn't work!
            unwindWinch();
            System.out.println("unwinding");
        }
        else if(oi.copilot.getRawButton(2)){
            climbup();
            System.out.println("winding");
        }
        else{         
           holdwinch();
        }      
    
    }
    
    /*Initializes robot's departure from the ground*/
    public void climbup(){
        winch.set(TalonFXControlMode.PercentOutput, winchRpms);
    }
    public void unwindWinch(){
        winch.set(TalonFXControlMode.PercentOutput, -winchRpms);
    }
    
    /*Drives wheels on the bar to allow robot to balance the bar*/
    public void Balance(){
        if(oi.DPad() == 90)
        {
            barRider.set(ControlMode.PercentOutput, balancerPercent);
        }
        else if(oi.DPad() == 270)
        {
            barRider.set(ControlMode.PercentOutput, -balancerPercent);
        }
        else
        {
            stopBarrider();
        }
    
        
    }
}