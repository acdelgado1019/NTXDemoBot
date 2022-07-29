package frc.robot.Autonomous;

import java.util.HashMap;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.RamseteController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.math.trajectory.constraint.DifferentialDriveVoltageConstraint;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RamseteCommand;
import java.util.List;
import frc.robot.Constants;
import frc.robot.Robot;


public class AutoCommand {
    public static DifferentialDriveVoltageConstraint autoVoltageConstraint;
    public static TrajectoryConfig config;
    public static Trajectory trajectory;
    public static RamseteCommand ramseteCommand;

    private static HashMap<String, Double> drivetrainEncoderValues;

    public static void limelightShoot(double power)
    {
        double degOff = Robot.limelight.getTX();
        Robot.shooter.setShooterMotor(power);
        while(Math.abs(degOff) > 1 && Robot.limelight.getTV() != 0)
        {
            double speed = .15 * degOff/(Math.abs(degOff));
            Robot.drivetrain.setLeftDrivetrain(-speed);
            Robot.drivetrain.setRightDrivetrain(speed);
            degOff = Robot.limelight.getTX();
        }
        Robot.drivetrain.setLeftDrivetrain(0);
        Robot.drivetrain.setRightDrivetrain(0);
        Timer.delay(4);
        Robot.shooterIntake.setVerticalIntake(-1);
        Timer.delay(.5);
        Robot.shooterIntake.setVerticalIntake(0);
        Robot.shooter.setShooterMotor(0.2);
    }

    public static void runIntake(double speed){
        Robot.shooterIntake.setHorizontalIntake(speed);
    }

    public static void lowerIntake(){
        Robot.shooterIntake.setIntakeLift(-Constants.INTAKE_LIFT_SPEED);
        Timer.delay(1);
        Robot.shooterIntake.setIntakeLift(0);
    }
    
    public static void timerDrive(double power, double time) {
        Robot.drivetrain.setRightDrivetrain(power);
        Robot.drivetrain.setLeftDrivetrain(power);
        Timer.delay(time);
        Robot.drivetrain.setRightDrivetrain(0);
        Robot.drivetrain.setLeftDrivetrain(0);
    }

    public static void delay(double delayTime){
        Timer.delay(delayTime);
    }

    public static void drive(double distanceInInches){
        updateEncoderValues();
        //6:1, 4 inch radius
        double current = 0;
        double target = (distanceInInches / (2 * Math.PI * 4) * 6) + drivetrainEncoderValues.get("leftDrivetrain");
        while(current < target){
            updateEncoderValues();
            current = drivetrainEncoderValues.get("leftDrivetrain");
            double speed = target / current * .1;
            speed = speed < .15 ? .15 : speed;
            Robot.drivetrain.setLeftDrivetrain(speed);
            Robot.drivetrain.setRightDrivetrain(speed);
        }
        Robot.drivetrain.setRightDrivetrain(0);
        Robot.drivetrain.setLeftDrivetrain(0);
    }

    public static void rotate(double angle){
        Robot.drivetrain.zeroHeading();
        double degOff = Math.abs(angle);
        while(Math.abs(degOff) > 1)
        {
            double speed = .3 * degOff/(Math.abs(degOff));
            Robot.drivetrain.setLeftDrivetrain(speed);
            Robot.drivetrain.setRightDrivetrain(-speed);
            degOff = Math.abs(angle) - Math.abs(Robot.drivetrain.getHeading());
        }
    }   

    public static void updateEncoderValues() {
        drivetrainEncoderValues = Robot.drivetrain.getEncoderValues();
    }

    // Create a voltage constraint to ensure we don't accelerate too fast
    public static DifferentialDriveVoltageConstraint getConstraint(){
        autoVoltageConstraint =
        new DifferentialDriveVoltageConstraint(
            new SimpleMotorFeedforward(
                Constants.ksVolts,
                Constants.kvVoltSecondsPerMeter,
                Constants.kaVoltSecondsSquaredPerMeter),
                Robot.drivetrain.kinematics,
                10
        );   
        return autoVoltageConstraint;
    }

    // Create config for trajectory
    public static TrajectoryConfig getTrajectoryConfig(){
        config =
        new TrajectoryConfig(
                Constants.kMaxSpeedMetersPerSecond,
                Constants.kMaxAccelerationMetersPerSecondSquared)
            // Add kinematics to ensure max speed is actually obeyed
            .setKinematics(Robot.drivetrain.kinematics)
            // Apply the voltage constraint
            .addConstraint(autoVoltageConstraint);
        return config;
    }

    // Trajectory to follow.  All units in meters.
    public static Trajectory getTrajectory(int path){
        if (path == 1){
            trajectory =
            TrajectoryGenerator.generateTrajectory(
            // Start at the origin facing ball
            new Pose2d(6.1, 5.1, new Rotation2d(136.5*Math.PI*2/360)),
            // pass through ball location
            List.of(new Translation2d(4.5, 6.7), new Translation2d(5, 7.2)),
            // Loop back around, facing goal
            new Pose2d(6.1, 6.2, new Rotation2d(-43.5*Math.PI*2/360)),
            // Pass config
            config);
        } else if (path == 2){
            trajectory =
            TrajectoryGenerator.generateTrajectory(
            // Start at the origin facing goal
            new Pose2d(8, 1.9, new Rotation2d(80*Math.PI*2/360)),
            // Pass through ball 2 and 3
            List.of(new Translation2d(7.6, 0.6), new Translation2d(5.3, 1.8)),
            // Turn to goal and come in range
            new Pose2d(5.6, 2.1, new Rotation2d(-320*Math.PI*2/360)),
            // Pass config
            config);
        } else if (path == 3){
            trajectory =
            TrajectoryGenerator.generateTrajectory(
            // Start at the origin facing ball
            new Pose2d(10.4, 3.2, new Rotation2d(-43.5*Math.PI*2/360)),
            // pass through ball location
            List.of(new Translation2d(12, 1.5), new Translation2d(11.5, 1)),
            // Loop back around, facing goal
            new Pose2d(10.5, 1.8, new Rotation2d(136.5*Math.PI*2/360)),
            // Pass config
            config);
        } else if (path == 4){
            trajectory =
            TrajectoryGenerator.generateTrajectory(
            // Start at the origin facing goal
            new Pose2d(8.6, 6.4, new Rotation2d(-100*Math.PI*2/360)),
            // Pass through ball 2 and 3
            List.of(new Translation2d(8.8, 7.6), new Translation2d(11.3, 6.6)),
            // Turn to goal and come in range
            new Pose2d(11, 6.3, new Rotation2d(-140*Math.PI*2/360)),
            // Pass config
            config);
        } else if (path == 5){
            trajectory =
            TrajectoryGenerator.generateTrajectory(
            // Start at the origin facing goal
            new Pose2d(6, 4.2, new Rotation2d(0)),
            // Pass through ball 2 and 3
            List.of(new Translation2d(4, 3.4), new Translation2d(1.5, 5)),
            // Turn to goal and come in range
            new Pose2d(1.5, 7, new Rotation2d(Math.PI/2)),
            // Pass config
            config);
        } else {
            trajectory =
            TrajectoryGenerator.generateTrajectory(
            // Start at the origin facing goal
            new Pose2d(10.5, 4.2, new Rotation2d(Math.PI)),
            // Pass through ball 2 and 3
            List.of(new Translation2d(12.5, 5.2), new Translation2d(14.5, 4)),
            // Turn to goal and come in range
            new Pose2d(14.5, 1, new Rotation2d(-Math.PI/2)),
            // Pass config
            config);
        }
        return trajectory;
    }

    public static RamseteCommand getRamsete(){
        ramseteCommand =
        new RamseteCommand(
            trajectory,
            Robot.drivetrain::getPose,
            new RamseteController(Constants.kRamseteB, Constants.kRamseteZeta),
            new SimpleMotorFeedforward(
                Constants.ksVolts,
                Constants.kvVoltSecondsPerMeter,
                Constants.kaVoltSecondsSquaredPerMeter),
            Robot.drivetrain.kinematics,
            Robot.drivetrain::getWheelSpeeds,
            new PIDController(Constants.kPDriveVel, 0, 0),
            new PIDController(Constants.kPDriveVel, 0, 0),
            // RamseteCommand passes volts to the callback
            Robot.drivetrain::tankDriveVolts,
            Robot.drivetrain);
        return ramseteCommand;
    }

    // Reset odometry to the starting pose of the trajectory.
    public static void resetOdometry(Trajectory trajectory){
        Pose2d pose = trajectory.getInitialPose();
        Robot.drivetrain.resetOdometry(pose);
    }

    public static Command runRamsete(int path){
            autoVoltageConstraint = getConstraint();
            config = getTrajectoryConfig();
            trajectory = getTrajectory(path);
            ramseteCommand = getRamsete();
            resetOdometry(trajectory);
            return ramseteCommand.andThen(() -> Robot.drivetrain.tankDriveVolts(0, 0));
        }
}