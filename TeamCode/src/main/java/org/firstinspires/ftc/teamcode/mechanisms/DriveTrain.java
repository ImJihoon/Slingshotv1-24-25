package org.firstinspires.ftc.teamcode.mechanisms;

import com.arcrobotics.ftclib.controller.PIDFController;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class DriveTrain {

    // 435 -> TODO: Number Souren wants us to remember
    private DcMotorEx leftFront;
    private DcMotorEx rightFront;
    private DcMotorEx leftBack;
    private DcMotorEx rightBack;
    private IMU imu;

    private Telemetry telemetry;
    private double newX = 0;
    private double newY = 0;

    private double targetAngle = 0;


    // public while being tuned on dashboard
    public static double turnP = 0.04;
    public static double turnI = 0;
    public static double turnD = 0.003;
    public static double turnF = 0.00001;
    private PIDFController turnController = new PIDFController(turnP, turnI, turnD, turnF);

    public DriveTrain(HardwareMap hardwareMap, IMU imu, Telemetry telemetry){

        // motors for slingshot bot
        leftFront = hardwareMap.get(DcMotorEx.class, "leftFront");
        leftFront.setDirection(DcMotorEx.Direction.FORWARD);
        leftFront.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);

        rightFront = hardwareMap.get(DcMotorEx.class, "rightFront");
        rightFront.setDirection(DcMotorEx.Direction.REVERSE);
        rightFront.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);

        leftBack = hardwareMap.get(DcMotorEx.class, "leftBack");
        leftBack.setDirection(DcMotorEx.Direction.FORWARD);
        leftBack.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);

        rightBack = hardwareMap.get(DcMotorEx.class, "rightBack");
        rightBack.setDirection(DcMotorEx.Direction.REVERSE);
        rightBack.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);

        // motors for papaya (test bot)
//        leftFront = hardwareMap.get(DcMotorEx.class, "leftFront");
//        leftFront.setDirection(DcMotorEx.Direction.REVERSE);
//        leftFront.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
//
//        rightFront = hardwareMap.get(DcMotorEx.class, "rightFront");
//        rightFront.setDirection(DcMotorEx.Direction.FORWARD);
//        rightFront.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
//
//        leftBack = hardwareMap.get(DcMotorEx.class, "leftBack");
//        leftBack.setDirection(DcMotorEx.Direction.REVERSE);
//        leftBack.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
//
//        rightBack = hardwareMap.get(DcMotorEx.class, "rightBack");
//        rightBack.setDirection(DcMotorEx.Direction.FORWARD);
//        rightBack.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);

        this.imu = imu;

        this.telemetry = telemetry;
    }

    // this is for testing, only used by testing methods
    public DriveTrain(DcMotorEx lF, DcMotorEx rF, DcMotorEx lB, DcMotorEx rB, IMU imu) {
        leftFront = lF;
        rightFront = rF;
        leftBack = lB;
        rightBack = rB;
        this.imu = imu;
    }


    public void moveRoboCentric(double strafe, double drive, double turn){
        targetAngle = changeTargetAngleWithJoystick(turn);

        double currentAngle = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
        turn = lockHeading(targetAngle, currentAngle);

        // Denominator is the largest motor power (absolute value) or 1
        // This ensures all the powers maintain the same ratio,
        // but only if at least one is out of the range [-1, 1]
        double denominator = Math.max(Math.abs(drive) + Math.abs(strafe) + Math.abs(turn), 1);

        leftFront.setPower((drive + strafe + turn) / denominator);
        leftBack.setPower((drive - strafe + turn) / denominator);
        rightFront.setPower((drive - strafe - turn) / denominator);
        rightBack.setPower((drive + strafe - turn) / denominator);
    }

    // this really should be called driver centric, but whatevs

    public void moveFieldCentric(double inX, double inY, double turn, double currentAngle){
        currentAngle += 90;
        double radian = Math.toRadians(currentAngle);
        double cosTheta = Math.cos(radian);
        double sinTheta = Math.sin(radian);
        newX = (-inX * sinTheta) - (inY * cosTheta);
        newY = (-inX * cosTheta) + (inY * sinTheta);

        moveRoboCentric(newX,newY,-turn); // may need to get rid of this - on turn
    }

    public double getHeading() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
    }

    public double angleWrap(double angle) {
        angle = Math.toRadians(angle);
        // Changes any angle between [-179,180] degrees
        // If rotation is greater than half a full rotation, it would be more efficient to turn the other way
        while (Math.abs(angle) > Math.PI) {
            angle -= 2 * Math.PI * (angle > 0 ? 1 : -1); // if angle > 0 * 1, < 0 * -1
        }
        if (angle == -Math.PI) { // this should cover the test case where the target angle is 180 (and it tries to wrap it & goes back and forth btwn 180 & -180)
            angle *= -1;
        }
        return Math.toDegrees(angle);
    }

    public double lockHeading(double targetAngle, double currentHeading) {
        double pid = turnController.calculate(angleWrap(targetAngle), angleWrap(currentHeading)); // test that adding angle wrap still works
        double power = pid + turnF;
        return -power;
    }

    public void changePID(double inP, double inI, double inD, double inF){
        turnP = inP; turnI = inI; turnD = inD; turnF = inF;
        turnController.setPIDF(inP, inI, inD, inF);
    }

    public void setTargetAngle(double targetAngle) {
        this.targetAngle = targetAngle;
    }

    public double changeTargetAngleWithJoystick(double joystickTurn) {
        if (joystickTurn == 0) {
            return targetAngle; // no change to target angle if joysticks aren't moving
        }
        return targetAngle -= joystickTurn * 10; // tune 10 depending on speed
    }
}