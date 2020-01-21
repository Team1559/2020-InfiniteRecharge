package frc.robot;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;

import io.github.oblarg.oblog.annotations.Config;
import io.github.oblarg.oblog.annotations.Log;
import io.github.oblarg.oblog.Loggable;

public class velocityControlTest implements Loggable{
public TalonFX falcon21; 
private  final int TIMEOUT = 0;
    private  final double kF = 0; //F-gain = (100% X 1023) / 7350 F-gain = 0.139183673 - (7350 is max speed)
    @Config
	private final double kP = 5; // P-gain = (.1*1023)/(155) = 0.66 - (350 is average error)
	private  final double kD = (0);
	private  final double cLR = 0.1;


public void start(){
falcon21 = new TalonFX(21);

falcon21.set(TalonFXControlMode.Velocity, 0);	
falcon21.configClosedloopRamp(cLR, TIMEOUT);
falcon21.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder);
falcon21.config_kF(0, kF);
falcon21.config_kP(0, kP);
falcon21.config_kD(0, kD);
falcon21.config_kI(0, 0);
falcon21.configNominalOutputForward(0, TIMEOUT);
falcon21.configNominalOutputReverse(0, TIMEOUT);
falcon21.configPeakOutputForward(+1, TIMEOUT);
falcon21.configPeakOutputReverse(-1, TIMEOUT);

}

public void velocity(){
    falcon21.set(TalonFXControlMode.Velocity, 1000);

}
public void stop(){
falcon21.set(TalonFXControlMode.Velocity, 0);

}
}