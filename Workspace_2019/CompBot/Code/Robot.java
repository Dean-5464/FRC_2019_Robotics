/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

  //Camera Server
  

  //Joystick / Controller
  Joystick X_Box = new Joystick(0);
  Joystick Secondary = new Joystick(1);

  //Line Break Sensor
  DigitalInput Line_Break = new DigitalInput(0);

  //Line Sensors
  AnalogInput Line_Right = new AnalogInput(0);
  AnalogInput Line_Middle = new AnalogInput(2);
  AnalogInput Line_Left = new AnalogInput(1);

  //(Depreciated)
  //AnalogInput UltraSonic = new AnalogInput(3);

  //Drive Motors
  WPI_TalonSRX Front_Right = new WPI_TalonSRX(1);
  WPI_TalonSRX Front_Left = new WPI_TalonSRX(6);
  WPI_TalonSRX Back_Right = new WPI_TalonSRX(4);
  WPI_TalonSRX Back_Left = new WPI_TalonSRX(5);

  //Other Motor(s)
  WPI_TalonSRX Lift = new WPI_TalonSRX(3);
  WPI_TalonSRX Climb_Drive = new WPI_TalonSRX(0);
  WPI_TalonSRX Climb_Assist = new WPI_TalonSRX(2);

  //Solenoid(s)
  DoubleSolenoid Hatch_Mech = new DoubleSolenoid(1, 4, 5);
  DoubleSolenoid Wrist = new DoubleSolenoid(1, 6, 7);
  DoubleSolenoid Climbing = new DoubleSolenoid(0, 0, 1);
//  DoubleSolenoid Climbing_Alt =  new DoubleSolenoid(0, 2, 3);

  //(Depreciated)
  //DoubleSolenoid Hatch = new DoubleSolenoid(1, 0, 1);
  //DoubleSolenoid Hatch_Position = new DoubleSolenoid(1, 2, 3);

  //Compressor
 // Compressor Compress = new Compressor();
  
  //Speed Controller Group(s)
  SpeedControllerGroup Left_Motors = new SpeedControllerGroup(Front_Left, Back_Left);
  SpeedControllerGroup Right_Motors = new SpeedControllerGroup(Front_Right, Back_Right);

  //Robot Drive
  DifferentialDrive Drive = new DifferentialDrive(Left_Motors, Right_Motors);

  //
  //Variables Start
  //

  //Auto Lift Variables
  int Lift_Level = 0;
  int Driver_Lift_Selection = 0;

  //Line Sensor Max and Min Values
  double left_max = 1.50;
  double left_min = 0.24;
  double middle_max = 1.14;
  double middle_min = 0.18;
  double right_max = 1.09;
  double right_min = 0.27;

  //Line Sensor Debug Variables
  double Debug_Left_Max = 0.0;
  double Debug_Left_Min = 9.0;
  double Debug_Middle_Max = 0.0;
  double Debug_Middle_Min = 9.0;
  double Debug_Right_Max = 0.0;
  double Debug_Right_Min = 9.0;

  //Sensor Values (Used Later In Line Follower Method)
  double left_value = 0.0;
  double middle_value = 0.0;
  double right_value = 0.0;

  //LineSensor Function Variables
  double left_speed = 0.0;
  double right_speed = 0.0;
  double p_speed;

  //Button Release Booleans (Controller)
  boolean X_Realease = true;
  boolean Y_Release = true;
  boolean A_Release = true;
  boolean B_Release = true;
  boolean RB_Release = true;
  boolean LB_Release = true;

  //Button Release Booleans (Joystick)
  boolean Lift_Buttons_Release = true;
  boolean Button_6_Release = true;
  boolean Button_5_Release = true;
  boolean Button_4_Release = true;
  boolean Button_3_Release = true;
  boolean Button_2_Release = true;
  boolean Trigger_Release = true;

  //Solenoid Booleans
  boolean Hatch_Boolean = false;
  boolean Hatch_Position_Boolean = false;
  boolean Claw_Boolean = false;
  boolean Wrist_Booelan = false;
  boolean Climbing_Solenoid_Boolean = false;

  //Drive Boolean
  boolean Speed_Selection = true;

  //Climbing Toggle
  boolean Climbing_Toggle = false;

  //Manual Overrides
  boolean Lift_Override = false;
  boolean Claw_Manual_Override = true;

  //Climb "Alarm"
  boolean Climb_Time = false;

  //Line Sensor Lineup
  boolean Line_Up = false;

  //Average Max / Min (Depreciated)
  double sensor_min = 0.40;
  double sensor_max = 1.82;

  //Line Follower Booleans (Depreciated)
  boolean left_turn;
  boolean right_turn;

  //
  //Variables End
  //

  //Auto Sendable Chooser
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  @Override
  public void robotInit() {

    //Camera Server
    CameraServer.getInstance().startAutomaticCapture();

    //Speed Selection Initalize
    SmartDashboard.putString("Speed", "NULL");

    //Axis Camera (DEPRECIATED)
    //cs.addAxisCamera("10.54.64.19");

    //Compressor Start (Not Needed)
    //Compress.start();

  }

  @Override
  public void robotPeriodic() {
    //
    //Smart Dashboard Outputs
    //
    SmartDashboard.putNumber("Match Time", DriverStation.getInstance().getMatchTime());
    SmartDashboard.putNumber("Battery Voltage", DriverStation.getInstance().getBatteryVoltage());
    SmartDashboard.putBoolean("CLIMB ALARM", Climb_Time);
    SmartDashboard.putBoolean("Line Up", Line_Up);

    if(Speed_Selection){
      SmartDashboard.putString("Speed", "Full Speed");
    }else{
      SmartDashboard.putString("Speed", "Slow Speed");
    }

    if(!Claw_Boolean){
      SmartDashboard.putString("Hatch Mech", "Open");
      }else{
        SmartDashboard.putString("Hatch Mech", "Closed");
    }

    //DEPRECIATED
   // SmartDashboard.putBoolean("Claw Manual Override", Claw_Manual_Override);

   // 
   //DEBUG SMARTDASHBOARD INPUTS
   //
/*
    SmartDashboard.putNumber("Left Sensor", Line_Left.getAverageVoltage());
    SmartDashboard.putNumber("Middle Sensor", Line_Middle.getAverageVoltage());
    SmartDashboard.putNumber("Right Sensor", Line_Right.getAverageVoltage());
    SmartDashboard.putNumber("Left Max", left_max);
    SmartDashboard.putNumber("Left Min", left_min);
    SmartDashboard.putNumber("Middle Max", middle_max);
    SmartDashboard.putNumber("Middle Min", middle_min);
    SmartDashboard.putNumber("Right Max", right_max);
    SmartDashboard.putNumber("Right Min", right_min);
*/
  }
  @Override

  //Solenoid Default Positions Automode Check
  public void autonomousInit() {
    Wrist.set(DoubleSolenoid.Value.kReverse);
    Hatch_Mech.set(DoubleSolenoid.Value.kForward);
    Climbing.set(DoubleSolenoid.Value.kForward);
  //  Climbing_Alt.set(DoubleSolenoid.Value.kForward);

  }

  @Override
  //
  //SandStorm
  //
  public void autonomousPeriodic() {

    //Sandstorm Code (Teleop Method)
    teleopPeriodic();

  }

  @Override
  public void teleopPeriodic() {

     // Get the proportional speed from the line sensor method
     p_speed = proportional_speed();

     //
     //Auto Lift (Depreciated)
     //
     /*
    //Auto Lift Selection
    if(Secondary.getRawButton(5) && !Lift_Override && Lift_Buttons_Release && Driver_Lift_Selection != 2){
      Driver_Lift_Selection++;
      Lift_Buttons_Release = false;

    }else if(Secondary.getRawButton(3) && Lift_Buttons_Release && !Lift_Override && Driver_Lift_Selection !=0){
      Driver_Lift_Selection--;
      Lift_Buttons_Release = false;

    }else if(Secondary.getRawButton(5) == false && Secondary.getRawButton(3) == false){
      Lift_Buttons_Release = true;

    }

    //Auto Lift Command
    if(Driver_Lift_Selection > Lift_Level && !Lift_Override && Ball_Claw.get() != DoubleSolenoid.Value.kReverse){
      Lift.set(1.0);

    }else if (Driver_Lift_Selection < Lift_Level && !Lift_Override && Ball_Claw.get() != DoubleSolenoid.Value.kReverse){
      Lift.set(-1.0);

    }else if(Driver_Lift_Selection == Lift_Level && !Lift_Override){
      Lift.set(0.0);
    }

    //Lift Override(Toggle)
    if(Secondary.getRawButton(2) && Button_2_Release && !Lift_Override){
      Lift_Override = true;
      Button_2_Release = false;

    }else if (Secondary.getRawButton(2) && Button_2_Release && Lift_Override){
      Lift_Override = false;
      Button_2_Release = false;

    }else if(Secondary.getRawButton(2) == false){
      Button_2_Release = true;
    }

    //Lift Override (Command)
      if(Secondary.getRawButton(5) && Ball_Claw.get() != DoubleSolenoid.Value.kReverse && Lift_Override == true){
        Lift.set(1.0);

      }else if(Secondary.getRawButton(3) && Ball_Claw.get() != DoubleSolenoid.Value.kReverse && Lift_Override == true){
        Lift.set(-1.0);

      }else if (Secondary.getRawButton(5) == false && X_Box.getRawButton(3) == false && Lift_Override == true){
        Lift.set(0.0);
      }
*/

//
//Current Lift
//

if(Secondary.getRawButton(5)){
  Lift.set(1.0);
}else if (Secondary.getRawButton(3)){
  Lift.set(-1.0);
}else{
  Lift.set(0.0);
}

    //
    //Solenoid Commands
    //

  //Claw Commands

  //Unused Command
  if(Claw_Manual_Override == false){

    if(Line_Break.get() == false){
      Hatch_Mech.set(DoubleSolenoid.Value.kForward);
    }else if(Line_Break.get() == true){
      Hatch_Mech.set(DoubleSolenoid.Value.kReverse);
    }

  }else if(Claw_Manual_Override == true){
    //
    //Hatch Intake
    //
    if(X_Box.getRawButton(1) && A_Release && Claw_Boolean){
      Hatch_Mech.set(DoubleSolenoid.Value.kForward);
      A_Release = false;
      Claw_Boolean = false;

    }else if(X_Box.getRawButton(1) && A_Release && !Claw_Boolean){
      Hatch_Mech.set(DoubleSolenoid.Value.kReverse);
      A_Release = false;
      Claw_Boolean = true;

    }else if (X_Box.getRawButton(1) == false){
      Hatch_Mech.set(DoubleSolenoid.Value.kOff);
      A_Release = true;
    }
  }
  //
  //End of Claw Commands
  //

    //Wrist Action (For the frame perimeter)
    if(Secondary.getRawButton(4) && Button_4_Release == true && !Wrist_Booelan){
      Wrist.set(DoubleSolenoid.Value.kForward);
      Button_4_Release = false;
      Wrist_Booelan = true;

    }else if(Secondary.getRawButton(4) && Button_4_Release == true && Wrist_Booelan){
      Wrist.set(DoubleSolenoid.Value.kReverse);
      Wrist_Booelan = false;
      Button_4_Release = false;

    }else if(Secondary.getRawButton(4) == false){
      Wrist.set(DoubleSolenoid.Value.kOff);
      Button_4_Release = true;
    }

    //Climbing Solenoids Command
    if(X_Box.getRawButton(5) && LB_Release && !Climbing_Solenoid_Boolean){
      Climbing.set(DoubleSolenoid.Value.kForward);
    //  Climbing_Alt.set(DoubleSolenoid.Value.kForward);
      Climbing_Solenoid_Boolean = true;
      LB_Release = false;

    }else if (X_Box.getRawButton(5) && LB_Release && Climbing_Solenoid_Boolean){
      Climbing.set(DoubleSolenoid.Value.kReverse);
    //  Climbing_Alt.set(DoubleSolenoid.Value.kReverse);
      Climbing_Solenoid_Boolean = false;
      LB_Release = false;

    }else if (X_Box.getRawButton(5) == false){
      Climbing.set(DoubleSolenoid.Value.kOff);
    //  Climbing_Alt.set(DoubleSolenoid.Value.kOff);
      LB_Release = true;
    }

    //
    //Driving Command(s)
    //

     // If You Press The Button The Robot Drives Based On Its Position On The Line (Line Follower)
     if (X_Box.getRawButton(3) == true) {
 
       // if line is on left sensor drive left
       if(left_value > right_value && left_value > middle_value){
       left_speed = (0.5 * (1 - p_speed));
       right_speed = (0.5 * (1 + p_speed));
       Drive.tankDrive(left_speed, right_speed);
       }

       // if line is on middle sensor drive forward (Unused for testing)
      // else if (middle_value > right_value && middle_value > left_value){
      //  Drive.tankDrive(0.5, 0.5);
      // }
 
       // if line is on right sensor
       else if(right_value > middle_value && right_value > middle_value){        
         left_speed = (0.5 * (1 + p_speed));
         right_speed = (0.5 * (1 - p_speed));
         Drive.tankDrive(left_speed, right_speed);
       }
       //
       //Line Follower End
       //
     }else if (X_Box.getRawButton(3) == false){

        //Speed Selection
        if(Secondary.getRawButton(1) && Trigger_Release && !Speed_Selection){
          Speed_Selection = true;
          Trigger_Release = false;
        }else if(Secondary.getRawButton(1) && Trigger_Release && Speed_Selection){
          Speed_Selection = false;
          Trigger_Release = false;
        }else if(Secondary.getRawButton(1) == false){
          Trigger_Release = true;
        }

       //Defualt Drive Function (Nested if Statement & Speed Selection)
         if(X_Box.getRawAxis(3) > 0.2 && Speed_Selection){
           Drive.arcadeDrive(X_Box.getRawAxis(3) * 1.0, X_Box.getRawAxis(0) * 1.0);
         }else if(X_Box.getRawAxis(2) > 0.2 && X_Box.getRawAxis(3) < 0.2 && Speed_Selection){
           Drive.arcadeDrive(-X_Box.getRawAxis(2) * 1.0, X_Box.getRawAxis(0) * 1.0);
         }else if(X_Box.getRawAxis(3) > 0.2 && !Speed_Selection){
          Drive.arcadeDrive(X_Box.getRawAxis(3) * 0.65, X_Box.getRawAxis(0) * 0.65);
         }else if(X_Box.getRawAxis(2) > 0.2 && !Speed_Selection){
          Drive.arcadeDrive(-X_Box.getRawAxis(2) * 0.65, X_Box.getRawAxis(0) * 0.65);
         }else{
         Drive.arcadeDrive(0.0, X_Box.getRawAxis(0));
       }
       
//
//End of Driving Commands
//
      }

       //Climb Assist Motor
       if(Secondary.getRawButton(12)){
         Climb_Assist.set(1.0); 
       }else if(Secondary.getRawButton(11)){
         Climb_Assist.set(-1.0);
       }else{
         Climb_Assist.set(0.0);
       }

       //Climb drive command
       if(X_Box.getRawButton(6)){
         Climb_Drive.set(1.0);
       }else{
         Climb_Drive.set(0.0);
       }
       
       //Climb Alarm
       if(DriverStation.getInstance().getMatchTime() < 35 && Climb_Time == false){
         Climb_Time = true;
       }else if (DriverStation.getInstance().getMatchTime() < 35 && Climb_Time == true){
         Climb_Time = false;
       }

       if(left_value > 0.15 || right_value > 0.15 || middle_value > 0.15){
         Line_Up = true;

       }else{
         Line_Up = false;
       }


       //
       //Teleop End
       //
  }
  //
  //Test Mode
  //
  @Override
  public void testPeriodic() {

    //Motor Inputs
    LiveWindow.add(Front_Left);
    LiveWindow.add(Front_Right);
    LiveWindow.add(Back_Left);
    LiveWindow.add(Back_Right);
    LiveWindow.add(Climb_Assist);
    LiveWindow.add(Lift);
    LiveWindow.add(Climb_Drive);

    //
    // Line Sensor Debug
    //

    //Max Debug Variables
    if(Line_Left.getAverageVoltage() > left_max){
      left_max = Line_Left.getAverageVoltage();
    }

    if(Line_Middle.getAverageVoltage() > middle_max){
      middle_max = Line_Middle.getAverageVoltage();
    }

    if(Line_Right.getAverageVoltage() > right_max){
      right_max = Line_Right.getAverageVoltage();
    }

   //Min Debug Variables 
    if(Line_Left.getAverageVoltage() < left_min){
      left_min = Line_Left.getAverageVoltage();
    }

    if(Line_Middle.getAverageVoltage() < middle_min){
      middle_min = Line_Middle.getAverageVoltage();
    }

    if(Line_Right.getAverageVoltage() < right_min){
      right_min = Line_Right.getAverageVoltage();
    }

  }

  //////////////////////////////////Line Follower Function (Private Function)////////////////////////////////////////////////////////

  double proportional_speed() {

    //Largest Sensor Values & Final Value
    double first_value = 0.0;
    double second_value = 0.0;
    double final_value = 0.0;

    //Middle Boolean
    boolean middle = false;

    // Calculate the scaled value of each line sensor between 0 and 1
    left_value = (Line_Left.getAverageVoltage() - left_min) / (left_max - left_min);
    middle_value = (Line_Middle.getAverageVoltage() - middle_min) / (middle_max - middle_min);
    right_value = (Line_Right.getAverageVoltage() - right_min) / (right_max - right_min);

    // Determine the largest value between the three sensors
    if (left_value > middle_value && left_value > right_value) {
      first_value = left_value;
      middle = false;
    } else if (middle_value > left_value && middle_value > right_value) {
      first_value = middle_value;
      middle = true;
    } else if (right_value > left_value && right_value > middle_value) {
      first_value = right_value;
      middle = false;
    }

    // If the largest value was middle, see if the left value or right value is larger.
    // If the largest value was not middle. Set the second value to the middle value.
    if (middle) {
      // second_value = (left_value > right_value) ? left_value : right_value;
      if(left_value > right_value){
        second_value = left_value;
      } else {
        second_value = right_value;
      }
    } else{
     second_value = middle_value;
    }

    // Calculate the final value
    final_value = (first_value - second_value) / (first_value + second_value);

    //Final Value to add to motors
    final_value = final_value * 2;
    
    return final_value;
  }
}
