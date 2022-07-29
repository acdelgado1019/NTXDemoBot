package frc.robot.Subsystems;

import frc.robot.Constants;
import frc.robot.Commands.DrivetrainCom;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.kinematics.DifferentialDriveOdometry;
import edu.wpi.first.math.kinematics.DifferentialDriveWheelSpeeds;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import com.kauailabs.navx.frc.AHRS;
import java.util.HashMap;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

public class Drivetrain extends SubsystemBase{
    public CANSparkMax motorLeft0;
    public CANSparkMax motorLeft1;
    public CANSparkMax motorRight0;
    public CANSparkMax motorRight1;
    private RelativeEncoder rightDrivetrain;
    private RelativeEncoder leftDrivetrain;   
    private final MotorControllerGroup m_leftMotors;
    private final MotorControllerGroup m_rightMotors;
    public final DifferentialDrive m_drive;

    private AHRS gyro = new AHRS(SerialPort.Port.kUSB);

    public DifferentialDriveKinematics kinematics = new DifferentialDriveKinematics(Constants.kTrackwidthMeters);
    public DifferentialDriveOdometry odometry;

    public Drivetrain (int l0, int l1, int r0, int r1){
        motorLeft0 = new CANSparkMax(l0, MotorType.kBrushless);
        motorLeft1 = new CANSparkMax(l1, MotorType.kBrushless);
        motorRight0 = new CANSparkMax(r0, MotorType.kBrushless);
        motorRight1 = new CANSparkMax(r1, MotorType.kBrushless);

        m_leftMotors = new MotorControllerGroup(motorLeft0,motorLeft1);
        m_rightMotors = new MotorControllerGroup(motorRight0,motorRight1); 
        m_drive = new DifferentialDrive(m_leftMotors, m_rightMotors);

        m_rightMotors.setInverted(true);

        leftDrivetrain = motorLeft0.getEncoder();
        rightDrivetrain = motorRight0.getEncoder();
        rightDrivetrain.setPositionConversionFactor(Constants.kEncoderDistancePerPulse);
        leftDrivetrain.setPositionConversionFactor(Constants.kEncoderDistancePerPulse);

        resetEncoders();
        odometry = new DifferentialDriveOdometry(gyro.getRotation2d());
    }

    @Override
    public void periodic(){
        odometry.update(
            gyro.getRotation2d(), 
            leftDrivetrain.getPosition(), 
            rightDrivetrain.getPosition()
        );
        setDefaultCommand(new DrivetrainCom());
    }

    public Pose2d getPose(){
        return odometry.getPoseMeters();
    }

    public DifferentialDriveWheelSpeeds getWheelSpeeds(){
        return new DifferentialDriveWheelSpeeds(
            leftDrivetrain.getVelocity(), 
            rightDrivetrain.getVelocity()
        ); 
    }

    public void resetOdometry(Pose2d pose) {
        resetEncoders();
        odometry.resetPosition(pose, gyro.getRotation2d());
    }

    public void tankDriveVolts(double leftVolts, double rightVolts){
        m_leftMotors.setVoltage(leftVolts);
        m_rightMotors.setVoltage(rightVolts);
    }

    public void resetEncoders() {
        rightDrivetrain.setPosition(0);
        leftDrivetrain.setPosition(0);
    }

    public double getAverageEncoderDistance() {
        return (leftDrivetrain.getPosition() + rightDrivetrain.getPosition()) / 2.0;
    }

    public void setMaxOutput(double maxOutput) {
        m_drive.setMaxOutput(maxOutput);
    }

    public void zeroHeading(){
        gyro.reset();
    }

    public double getHeading(){
        return -gyro.getRotation2d().getDegrees();
    }

    public double getTurnRate() {
        return -gyro.getRate();
    }

    //Drive Methods
    public void setLeftDrivetrain(double speed){
        motorLeft0.set(speed);
        motorLeft1.set(speed);
        m_drive.feed();
    }

    public void setRightDrivetrain(double speed){
        motorRight0.set(-speed);
        motorRight1.set(-speed);
        m_drive.feed();
    }

    public HashMap<String, Double> getEncoderValues()
    {
        HashMap<String, Double> encoderMap = new HashMap<String, Double>();
        encoderMap.put("rightDrivetrain", rightDrivetrain.getPosition());
        encoderMap.put("leftDrivetrain", leftDrivetrain.getPosition());
        return encoderMap;
    }
}