package org.firstinspires.ftc.teamcode.auton;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.mechanisms.intake.Intake;
import org.firstinspires.ftc.teamcode.mechanisms.intake.IntakeConstants;
import org.firstinspires.ftc.teamcode.mechanisms.outtake.Outtake;
import org.firstinspires.ftc.teamcode.mechanisms.specimen.SpecimenClaw;
import org.firstinspires.ftc.teamcode.misc.gamepad.GamepadMapping;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;

@Autonomous
public class SampleAuton16 extends LinearOpMode {
    private GamepadMapping controls;
    private Robot robot;
    private Intake intake;
    private Outtake outtake;

    @Override
    public void runOpMode() throws InterruptedException {
        controls = new GamepadMapping(gamepad1, gamepad2);
        robot = new Robot(hardwareMap, telemetry, controls);
        intake = robot.intake;
        outtake = robot.outtake;

        robot.outtake.resetEncoders();

        moveLift(0);
        outtake.bucketToReadyForTransfer();
        intake.extendoFullRetract();
        intake.activeIntake.flipUp();

        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);

        Pose2d startPose = new Pose2d(-12, -63.5, Math.toRadians(0));

        drive.setPoseEstimate(startPose);

        TrajectorySequence trajSeq = drive.trajectorySequenceBuilder(startPose)
                //start by raising slides to go score
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    //raise slides
                    moveLift(2400);
                })
                .setReversed(true)
                //preload to bucket
                .splineToLinearHeading(new Pose2d(-55,-57,Math.toRadians(45)),Math.toRadians(225))
                .setReversed(false)
                .back(3.5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    //flip bucket
                    outtake.bucketDeposit();
                })
                .waitSeconds(0.75)
                .forward(7)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    outtake.bucketToReadyForTransfer();
                    moveLift(0);
                })

                //1st yellow to bucket
                .lineToConstantHeading(new Vector2d(-54, -52))
                .turn(Math.toRadians(35))
//                .splineToLinearHeading(new Pose2d(-50,-59,Math.toRadians(90)),Math.toRadians(45))

                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    extendoIntake();
                })
                .forward(3)
                .UNSTABLE_addTemporalMarkerOffset(0.15, () -> {
                    intake.activeIntake.flipDownFull();
//                    intake.motorRollerOnToIntake();
                })
                .waitSeconds(0.75)
                .forward(12)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    robot.intake.activeIntake.flipUp();
                })
                .UNSTABLE_addTemporalMarkerOffset(0.2, () -> {
                    deextend();
                })
                .setReversed(true)
                .splineToLinearHeading(new Pose2d(-52,-56,Math.toRadians(45)),Math.toRadians(225))

                .setReversed(false)
                .UNSTABLE_addTemporalMarkerOffset(0.2, () -> {
                    //transfer sample
                    intake.activeIntake.transferSample();
                })
                .UNSTABLE_addTemporalMarkerOffset(1, () -> {
                    intake.activeIntake.transferOff();
                    intake.activeIntake.flipUp();
                })
                .UNSTABLE_addTemporalMarkerOffset(0.75, () -> {
                        //raise slides
                        moveLift(2400);
                    })
                .waitSeconds(1)
                .back(4.75)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    //flip bucket
                    outtake.bucketDeposit();
                })
                .waitSeconds(0.75)
                .forward(7)
                .UNSTABLE_addTemporalMarkerOffset(0.1, () -> {
                    outtake.bucketToReadyForTransfer();
                    moveLift(0);
                })

                //MNEXT SAMPLE - second yellow
//                .splineToLinearHeading(new Pose2d(-55,-60,Math.toRadians(90)),Math.toRadians(45))
                .lineToConstantHeading(new Vector2d(-56, -52))
                .turn(Math.toRadians(50))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    extendoIntake();
                })
                .forward(3)
                .UNSTABLE_addTemporalMarkerOffset(0.15, () -> {
                    intake.activeIntake.flipDownFull();
                })
                .waitSeconds(0.75)
                .forward(15)
                .waitSeconds(0.2)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    robot.intake.activeIntake.flipUp();
                })
                .UNSTABLE_addTemporalMarkerOffset(0.2, () -> {
                    deextend();
                })

                .setReversed(true)
                .splineToLinearHeading(new Pose2d(-50,-54,Math.toRadians(45)),Math.toRadians(225))
                .setReversed(false)
                .UNSTABLE_addTemporalMarkerOffset(0.3, () -> {
                    //transfer sample
                    intake.activeIntake.transferSample();
                })
                .UNSTABLE_addTemporalMarkerOffset(1, () -> {
                    intake.activeIntake.transferOff();
                    intake.activeIntake.flipUp();
                })
                .UNSTABLE_addTemporalMarkerOffset(0.8, () -> {
                    //raise slides
                    moveLift(2400);
                })
                .waitSeconds(1)
                .back(7)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    //flip bucket
                    outtake.bucketDeposit();
                })
                .waitSeconds(0.75)
                .forward(7)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    outtake.bucketToReadyForTransfer();
                    moveLift(0);
                })


                //NEXT SAMPLE - third yellow
//                .splineToLinearHeading(new Pose2d(-36,-29,Math.toRadians(180)),Math.toRadians(90))
                .splineToLinearHeading(new Pose2d(-40, -40, Math.toRadians(154)), Math.toRadians(225))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    extendoIntake();
                })
                .UNSTABLE_addTemporalMarkerOffset(0.2, () -> {
                    intake.activeIntake.flipDownFull();
                })
                // TODO try commenting this out to change the last sample intake
                .waitSeconds(1)
                .forward(13)
                .waitSeconds(0.75)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    robot.intake.activeIntake.flipUp();
                })
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    robot.intake.extendoFullRetract();
                })
                .UNSTABLE_addTemporalMarkerOffset(0.2, () -> {
                    intake.activeIntake.motorRollerOff();
                })

                .setReversed(true)
                .splineToLinearHeading(new Pose2d(-49,-52,Math.toRadians(45)),Math.toRadians(225))
                .setReversed(false)
                .UNSTABLE_addTemporalMarkerOffset(0.2, () -> {
                    //transfer sample
                    intake.activeIntake.transferSample();
                })
                .UNSTABLE_addTemporalMarkerOffset(.8, () -> {
                    intake.activeIntake.transferOff();
                    intake.activeIntake.flipUp();
                })
                .UNSTABLE_addTemporalMarkerOffset(0.75, () -> {
                    //raise slides
                    moveLift(2400);
                })
                .waitSeconds(1)
                .back(6.5)

                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    //flip bucket
                    outtake.bucketDeposit();
                })
                .waitSeconds(0.75)
                .forward(3)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    outtake.bucketToReadyForTransfer();
                    moveLift(0);
                })


                // park
                .setReversed(false)
                //.splineToLinearHeading(new Pose2d(-48, -36, Math.toRadians(90)),Math.toRadians(90))
                .splineToLinearHeading(new Pose2d(-28, -12, Math.toRadians(180)), Math.toRadians(0))
                .build();

        waitForStart();

        if (!isStopRequested())
            drive.followTrajectorySequence(trajSeq);
        }

        public void extendoIntake(){
            //extendo out
            //robot.intake.extendoFullExtend();
            robot.intake.rightExtendo.setPosition(.2);
            robot.intake.leftExtendo.setPosition(.2);
            //run intake
            intake.activeIntake.motorRollerOnToIntake();
        }
        public void deextend(){
            //deeextend
            robot.intake.extendoFullRetract();
            intake.activeIntake.motorRollerOff();
        }
        public void moveLift(int ticks){
            robot.outtake.outtakeSlideLeft.setTargetPosition(ticks);
            robot.outtake.outtakeSlideRight.setTargetPosition(ticks);
            robot.outtake.outtakeSlideLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.outtake.outtakeSlideRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.outtake.outtakeSlideLeft.setPower(1);
            robot.outtake.outtakeSlideRight.setPower(1);
        }
}