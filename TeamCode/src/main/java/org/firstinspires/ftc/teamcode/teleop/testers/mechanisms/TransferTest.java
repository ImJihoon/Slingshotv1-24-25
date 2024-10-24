package org.firstinspires.ftc.teamcode.teleop.testers.mechanisms;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.mechanisms.Cycle;
import org.firstinspires.ftc.teamcode.mechanisms.DriveTrain;
import org.firstinspires.ftc.teamcode.mechanisms.intake.Intake;
import org.firstinspires.ftc.teamcode.mechanisms.intake.IntakeConstants;
import org.firstinspires.ftc.teamcode.mechanisms.outtake.Outtake;
import org.firstinspires.ftc.teamcode.misc.gamepad.GamepadMapping;

@Config
@TeleOp
public class TransferTest extends OpMode {
    // to extend, extend out slides a little bit then pivot down
    private Robot robot;
    private GamepadMapping controls;
    private Intake intake;
    private Outtake outtake;
    private DriveTrain dt;

    private ElapsedTime loopTime = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
    private double startTime;

    private boolean pushOut = false;

    @Override
    public void init() {
        controls = new GamepadMapping(gamepad1, gamepad2);
        robot = new Robot(hardwareMap, telemetry, controls);
        intake = robot.intake;
        outtake = robot.outtake;
        dt = robot.drivetrain;
//        intake.extendoFullRetract();
//        intake.flipUp();
        outtake.resetEncoders();
        outtake.returnToRetracted();
        startTime = loopTime.milliseconds();
    }

    @Override
    public void init_loop() {
        controls.isBlue.update(gamepad1.x);
        if (controls.isBlue.value()) {
            intake.colorSensor.setIsBlue(true);
        } else {
            intake.colorSensor.setIsBlue(false);
        }
        telemetry.addData("Color Sensor Is Blue", intake.colorSensor.getIsBlue());
        telemetry.addData("Is Blue", controls.isBlue.value());
    }

    @Override
    public void loop() {
        controls.update();
        if (controls.intakeOnToIntake.locked()) {
            intake.motorRollerOnToIntake();
        } else {
            intake.motorRollerOff();
        }
        if (intake.colorSensor.checkSample().equals(IntakeConstants.SampleTypes.BLUE) && !intake.colorSensor.isBlue) {
            // add motor to pushOutSample to make it faster
            pushOut = true;
        } else if (intake.colorSensor.checkSample().equals(IntakeConstants.SampleTypes.RED) && intake.colorSensor.isBlue) {
            pushOut = true;
        } else if (intake.colorSensor.checkSample().equals(IntakeConstants.SampleTypes.BLUE) && intake.colorSensor.isBlue
                || intake.colorSensor.checkSample().equals(IntakeConstants.SampleTypes.RED) && !intake.colorSensor.isBlue) {
            pushOut = false;
        }

        telemetry.addData("Sample: ", intake.colorSensor.checkSample());
        telemetry.addData("Color Sensor Is Blue", intake.colorSensor.getIsBlue());
        telemetry.addData("Is Blue", controls.isBlue.value());
        telemetry.addData("loop time", loopTime.milliseconds());
        telemetry.addData("start time", startTime);

            if (loopTime.milliseconds() - startTime <= 5000) {
                intake.backRollerServo.setPosition(1);
                intake.motorRollerOnToIntake();
            } else {
                intake.motorRollerOff();
                intake.backRollerIdle();
            }

    }
}
