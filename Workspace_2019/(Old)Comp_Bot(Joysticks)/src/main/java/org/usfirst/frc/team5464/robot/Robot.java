/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team5464.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */
public class Robot extends IterativeRobot {
	
	//Variables
	double P_Coef = 0.16;
	
	double Steer = 0;
	double Error = 0;
	double DriveSpeed = -0.8;
	double Distance;
	boolean Auto_Stop = false;
	int step = 0;
	
	//Color Logic Variables
	boolean right_Color_Switch = false;
	boolean left_Color_Switch = false;
	boolean right_Color_Scale = false;
	boolean left_Color_Scale = false;
	boolean mid_Color_Scale = false;
	boolean mid_Color_Switch = false;
	boolean BlueSide = false;
	boolean RedSide = false;
	
	//Driver Station and FMS stuff
	boolean FMSData = false;
	DriverStation.Alliance color;
	
	//Timers
	Timer Time = new Timer();
		
	//Encoders
	Encoder Right_Encoder = new Encoder(0, 1);
	Encoder Left_Encoder = new Encoder(2, 3);
		
	//Gyro
	ADXRS450_Gyro Gyro = new ADXRS450_Gyro();
	
	//Limit Switch
	DigitalInput Limit = new DigitalInput (4);
	
	//Joysticks / Controllers
	
	//Joystick Arcade = new Joystick(0);

	Joystick Right = new Joystick(1);
	Joystick Left = new Joystick(0);
	Joystick Actuators = new Joystick(2);
	
	//Joystick Controller = new Joystick(0);

	
	//Drive Motors
	WPI_TalonSRX FR = new WPI_TalonSRX(7);
	WPI_TalonSRX FL = new WPI_TalonSRX(6);
	WPI_TalonSRX RR = new WPI_TalonSRX(3);
	WPI_TalonSRX RL = new WPI_TalonSRX(5);
	
	//Lift Motor(s)
	WPI_TalonSRX Lift_Motor = new WPI_TalonSRX(2);
	
	
	//Intake Motors
	//WPI_TalonSRX Intake_Left = new WPI_TalonSRX(0);
	//WPI_TalonSRX Intake_Right = new WPI_TalonSRX(0);
	
	//climber Motors
	WPI_TalonSRX Climb_One = new WPI_TalonSRX(0);
	WPI_TalonSRX Climb_Two = new WPI_TalonSRX(1);
	
	//Speed Controller Groups
	SpeedControllerGroup Right_Motors = new SpeedControllerGroup(FR, RR);
	SpeedControllerGroup Left_Motors = new SpeedControllerGroup(FL, RL);
	//SpeedControllerGroup Intake_Motors = new SpeedControllerGroup(Intake_Left, Intake_Right);
	
	//Robot Drive
	DifferentialDrive Drive = new DifferentialDrive(Right_Motors, Left_Motors);
	
	//Compressor
	Compressor Comp = new Compressor();
	
	//Solenoids
	DoubleSolenoid Shifting = new DoubleSolenoid(0, 1);
	DoubleSolenoid Intake_Arm = new DoubleSolenoid(3, 4);
	DoubleSolenoid Intake = new DoubleSolenoid(7, 8);
	
	
	//Auto Choices
	private static final String kDefaultAuto = "Default";
	private static final String kCustomAuto = "My Auto";
	private static final String kAutoLeft = "Left Auto";
	private static final String kAutoRight = "Right Auto";
	private static final String kAutoMidSwitch = "Mid Auto Switch";
	private static final String kAutoMidScale = "Mid Auto Scale";
	private String m_autoSelected;
	private SendableChooser<String> m_chooser = new SendableChooser<>();
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {	
		//Auto Stuff
		SmartDashboard.putData(Gyro);
		
		//Solenoid Default Position
		Intake.set(DoubleSolenoid.Value.kReverse);
		
		//Gyro calibrate
		Gyro.calibrate();
		
		//Camera
		CameraServer.getInstance().startAutomaticCapture();
		
		m_chooser.addDefault("Default Auto", kDefaultAuto);
		m_chooser.addObject("My Auto", kCustomAuto);
		m_chooser.addObject("Left Auto", kAutoLeft);
		m_chooser.addObject("Right Auto", kAutoRight);
		m_chooser.addObject("Mid Auto Switch", kAutoMidSwitch);
		m_chooser.addObject("Mid Auto Scale", kAutoMidScale);
		SmartDashboard.putData("Auto choices", m_chooser);
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
		//Comp
		Comp.start();
		
		//Gyro Reset
		Gyro.reset();
		
		//MotorSafety Disable
		Drive.setSafetyEnabled(false);
		
		//Default Positions
		Intake.set(DoubleSolenoid.Value.kReverse);
		Shifting.set(DoubleSolenoid.Value.kForward);
		Intake_Arm.set(DoubleSolenoid.Value.kForward);
				
		//Temp Gyro calibrate
//		Gyro.calibrate();
		
		m_autoSelected = m_chooser.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + m_autoSelected);
		
		//Reset Encoders
		Left_Encoder.reset();
		Right_Encoder.reset();
		
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		//Comp
		Comp.start();
		
		    //Get Color
		    color = DriverStation.getInstance().getAlliance();
		    
		    //Color Oriented commands
		    if(color == DriverStation.Alliance.Blue) {
		    	BlueSide = true;
		    	RedSide = false;
		    }else if(color == DriverStation.Alliance.Red) {
		    	RedSide = true;
		    	BlueSide = false;
		    }else {
		    	RedSide = false;
		    	BlueSide = false;
		    }
		    
		    
		    //FMS Retrieval
			  Match_Data.OwnedSide Switch = Match_Data.getOwnedSide(Match_Data.GameFeature.SWITCH_NEAR);
			    if (Switch == Match_Data.OwnedSide.LEFT) {
			        // Do something with the left
			    	left_Color_Switch = true;
			    	right_Color_Switch = false;
			    } else if (Switch == Match_Data.OwnedSide.RIGHT) {
			        // Do something with the right
			    	left_Color_Switch = false;
			    	right_Color_Switch = true;
			    } else {
			        // Unknown
			    	left_Color_Switch = false;
			    	right_Color_Switch = false;
			    }
			    
			    Match_Data.OwnedSide Scale = Match_Data.getOwnedSide(Match_Data.GameFeature.SCALE);
			    if (Scale == Match_Data.OwnedSide.LEFT) {
			    	//Do something with Left
			    	left_Color_Scale = true;
			    	right_Color_Scale = false;
			    }else if (Scale == Match_Data.OwnedSide.RIGHT) {
			    	//Do something with Right
			    	left_Color_Scale = false;
			    	right_Color_Scale = true;
			    }else {
			    	//unknown
			    	left_Color_Scale = false;
			    	right_Color_Scale = false;
			    }
			    //
			    //Default To This Code In Case Of Emergency
			    //
		switch (m_autoSelected) {
			case kCustomAuto:
				//Drive Forwards And Stop
				if (Right_Encoder.getRaw() < 5500 && Left_Encoder.getRaw() < 5500) {
					Drive.tankDrive(DriveSpeed, DriveSpeed);
				}else if (Right_Encoder.getRaw() > 5500 && Left_Encoder.getRaw() > 5500) {
					Drive.tankDrive(0.0, 0.0);
				}
				
				break;
			case kAutoLeft:
					//
					//Case selection for LEFT
					//
				
			
			//Scale Priority
				/*
					if (left_Color_Scale == true && step == 0) {
						step = 2;
					}else if (left_Color_Switch == true && left_Color_Scale == false && step == 0) {
						step = 7;
					}else if (left_Color_Switch == false && left_Color_Scale == false && step == 0) {
						step = 11;
					}
				*/
					
			//Switch Priority
				
				if (left_Color_Switch == true && step == 0) {
					step = 7;
				}else if (left_Color_Switch == false && left_Color_Scale == true && step == 0) {
					step = 2;
				}else if(left_Color_Switch == false && left_Color_Scale == false && step == 0) {
					step = 11;
				}
				
				while(Auto_Stop == false && DriverStation.getInstance().getMatchTime() > 0.5) {
					//Auto Testing Stuff
					/*
					SmartDashboard.putNumber("Right",Right_Encoder.getRaw());
					SmartDashboard.putNumber("Left",Left_Encoder.getRaw());					
					SmartDashboard.putNumber("Angle", Gyro.getAngle());
					*/
					
					//Error Checking Variables
					Error = Gyro.getAngle();
					Steer = Error * P_Coef;
					

				
					
				switch(step) {
				//
				//Left Scale Auto
				//
				
				//Drive Forward From The Starting Position To A Position Perpendicular To The Scale
				case 2:
					if (Right_Encoder.getRaw() <= 14000 && -Left_Encoder.getRaw() <= 14000) {
						Drive.tankDrive(DriveSpeed - Steer, DriveSpeed + Steer);
					}else {
						Drive.tankDrive(0.0, 0.0);
						step++;
					}
					break;
					
				// Turn Turn To Face Scale
				case 3:
					if (Gyro.getAngle() < 70) {
						Drive.tankDrive(-DriveSpeed, DriveSpeed);
						
					}else if(Gyro.getAngle() < 90 && Gyro.getAngle() >= 70) {
						Drive.tankDrive(0.55, -0.55);
						
					}else if (Gyro.getAngle() >= 90){
						Drive.tankDrive(0.0, 0.0);
						Right_Encoder.reset();
						Left_Encoder.reset();
						step = 5;
					}
					break;
					
					//Drive The Remaining Distance To The Scale
				case 4:
					if (Right_Encoder.getRaw() > -5 && Left_Encoder.getRaw() > -5) {
						Drive.tankDrive(0.75 , 0.75);
					}else {
						Drive.tankDrive(0.0, 0.0);
						step++;
					}
					break;
					
					//Score In The Scale Using Timer Based Commands
				case 5:
					if (Time.get() == 0) {
						Time.start();
					}
					
					if(Time.get() < 2.8) {
					Lift_Motor.set(1.0);
					}else if(Time.get() < 3.8 && Time.get() > 2.9) {
						Intake_Arm.set(DoubleSolenoid.Value.kReverse);
						Lift_Motor.set(0.0);
					}else if (Time.get() < 5.2 && Time.get() > 3.9) {
						Intake.set(DoubleSolenoid.Value.kForward);
					}else if(Time.get() >= 5.3) {
						Intake_Arm.set(DoubleSolenoid.Value.kForward);
						Time.stop();
						step = -1;
					}
					break;
				// Turn Around
				case 6:
					if (Gyro.getAngle() < 180) {
						Drive.tankDrive(-0.55, 0.55);
					}else if (Gyro.getAngle() >= 180) {
					Drive.tankDrive(0.0, 0.0);
					step = -1;
					}
					break;
					//
					// Left Switch
					//
					
					//Drive Forwards From The Starting Position To A Position Perpendicular To The Switch
				case 7:
					if(Right_Encoder.getRaw() < 6500 && Left_Encoder.getRaw() < 6500 && Gyro.getAngle() < 20) {
						Drive.tankDrive(DriveSpeed - Steer, DriveSpeed + Steer);
					}else {
						Drive.tankDrive(0.0, 0.0);
						step = 1;
					}
					break;
					//Turn Towards The Switch
				case 1:
					if (Gyro.getAngle() < 70) {
						Drive.tankDrive(-DriveSpeed, DriveSpeed);
						
					}else if (Gyro.getAngle() < 90 && Gyro.getAngle() >= 70) {
						Drive.tankDrive(0.55, -0.55);
						
					}else if (Gyro.getAngle() >= 90) {
						Drive.tankDrive(0.0, 0.0);
						Right_Encoder.reset();
						Left_Encoder.reset();
						step = 8;
						}
					break;
					//Drive Forwards To Eliminate The Rest Of The Distance From The Switch
				case 8:
					if (Time.get() == 0) {
						Time.start();
					}
					if (Time.get() < 1) {
						Drive.tankDrive(-0.7, -0.7);
					}else if (Time.get() >= 0.25){
						Drive.tankDrive(0.0, 0.0);
						Right_Encoder.reset();
						Left_Encoder.reset();
						Time.stop();
						step = 14;
					}
					break;
					//Reset Timer
				case 14:
					Time.reset();
					Time.stop();
					step = 9;
					break;

					//Score In Switch Using Timer Based Commands
				case 9:
					//Time Start and Termination
					if (Time.get()  == 0) {
					Time.start();
					}
					
					if (Time.get() < 1) {
						Lift_Motor.set(1.0);
					}else if (Time.get() <= 1.5 && Time.get() >= 1.1) {
						Intake_Arm.set(DoubleSolenoid.Value.kReverse);
						Lift_Motor.set(0.0);
					}else if(Time.get() <= 2.5 && Time.get() >= 2.0) {
							Intake.set(DoubleSolenoid.Value.kForward);
							Intake_Arm.set(DoubleSolenoid.Value.kReverse);
					}else if(Time.get() >= 3.5) {
						Lift_Motor.set(0.0);
						Intake_Arm.set(DoubleSolenoid.Value.kForward);
						Time.stop();
						step = -1;
					}
					break;
					
					//
					//Neither Left switch or Scale
					//
					
					//Drive Forwards Past The Switch 
				case 11:
					if (Right_Encoder.getRaw() <= 10000 && Left_Encoder.getRaw() <= 10000) {
						Drive.tankDrive(DriveSpeed - Steer, DriveSpeed + Steer);
					}else {
						Drive.tankDrive(0.0, 0.0);
						step = 13;
					}
					break;
					//Turn Towards The Center Of The Field
				case 13:
					if (Gyro.getAngle() < 70) {
						Drive.tankDrive(-DriveSpeed, DriveSpeed);
						
					}else if (Gyro.getAngle() < 90 && Gyro.getAngle() >= 70) {
						Drive.tankDrive(0.55, -0.55);
						
					}else if (Gyro.getAngle() >= 90) {
						Drive.tankDrive(0.0, 0.0);
						Right_Encoder.reset();
						Left_Encoder.reset();
						step = 12;
					}
					break;
					
					//Cover Some Of The Distance To The Power Cubes And Terminate The Case
				case 12:
					if (Right_Encoder.getRaw() < 1000 && Left_Encoder.getRaw() < 1000) {
						Drive.tankDrive(DriveSpeed, DriveSpeed);
					}else{
						Drive.tankDrive(0.0, 0.0);
						step = -1;
					}
					break;
					
				default:
					Drive.stopMotor();
					Lift_Motor.stopMotor();
					Auto_Stop = true;
					break;
				}	
				}
				break;
				
				
			case kAutoRight:
				//
				//Case selection for RIGHT
				//
				
				
			//Scale Priority	
				/*
				if (right_Color_Scale == true && step == 0) {
					step = 2;
				}else if (right_Color_Switch == true && right_Color_Scale == false && step == 0) {
					step = 7;
				}else if (right_Color_Switch == false && right_Color_Scale == false && step == 0) {
					step = 11;
				}
			*/
				
				
			//Switch Priority
			
				if (right_Color_Switch == true && step == 0) {
					step = 7;
				}else if (right_Color_Scale == true && right_Color_Switch == false && step == 0) {
					step = 2;
				}else if (right_Color_Switch == false && right_Color_Scale == false && step == 0) {
					step = 11;
				}
				
				
			while(Auto_Stop == false && DriverStation.getInstance().getMatchTime() > 0.5) {
				//Auto Testing Stuff
				/*
				SmartDashboard.putNumber("Right",Right_Encoder.getRaw());
				SmartDashboard.putNumber("Left",Left_Encoder.getRaw());					
				SmartDashboard.putNumber("Angle", Gyro.getAngle());
				*/
				
				//Error Checking Variables
				Error = Gyro.getAngle();
				Steer = Error * P_Coef;
				

				
				
			switch(step) {
			//
			//Right Scale Auto
			//
			
			//Drive Forward From The Starting Position To A Position Perpendicular To The Scale
			case 2:
				if (Right_Encoder.getRaw() <= 14000 && -Left_Encoder.getRaw() <= 14000) {
					Drive.tankDrive(DriveSpeed - Steer, DriveSpeed + Steer);
				}else {
					Drive.tankDrive(0.0, 0.0);
					step++;
				}
				break;
				
			// Turn Turn To Face Scale
			case 3:
				if (Gyro.getAngle() > -70) {
					Drive.tankDrive(DriveSpeed, -DriveSpeed);
					
				}else if (Gyro.getAngle() > -90 && Gyro.getAngle() <= -70) {
					Drive.tankDrive(-0.55, 0.55);
					
				}else if ( Gyro.getAngle() <= -90){
					Drive.tankDrive(0.0, 0.0);
					Right_Encoder.reset();
					Left_Encoder.reset();
					step = 5;
				}
				break;
				
				//Drive The Remaining Distance To The Scale
			case 4:
				if (Right_Encoder.getRaw() > -5 && Left_Encoder.getRaw() > -5) {
					Drive.tankDrive(0.75 , 0.75);
				}else {
					Drive.tankDrive(0.0, 0.0);
					step++;
				}
				break;
				
				//Score In The Scale Using Timer Based Commands
			case 5:
				if (Time.get() == 0) {
					Time.start();
				}
				
				if(Time.get() < 2.8) {
				Lift_Motor.set(1.0);
				}else if(Time.get() < 3.8 && Time.get() > 2.9) {
					Intake_Arm.set(DoubleSolenoid.Value.kReverse);
					Lift_Motor.set(0.0);
				}else if (Time.get() < 4.8 && Time.get() > 4.3) {
					Intake.set(DoubleSolenoid.Value.kForward);
				}else if(Time.get() >= 5) {
					Intake_Arm.set(DoubleSolenoid.Value.kForward);
					Time.stop();
					step = -1;
				}
				break;
			// Turn Around
			case 6:
				if (Gyro.getAngle() > -180) {
					Drive.tankDrive(DriveSpeed, -DriveSpeed);
				}else if (Gyro.getAngle() <= -180) {
				Drive.tankDrive(0.0, 0.0);
				step = -1;
				}
				break;
				//
				// Right Switch
				//
				
				//Drive Forwards From The Starting Position To A Position Perpendicular To The Switch
			case 7:
				if(Right_Encoder.getRaw() < 6500 && Left_Encoder.getRaw() < 6500 && Gyro.getAngle() < 20) {
					Drive.tankDrive(DriveSpeed - Steer, DriveSpeed + Steer);
				}else {
					Drive.tankDrive(0.0, 0.0);
					step = 1;
				}
				break;
				//Turn Towards The Switch
			case 1:
				if (Gyro.getAngle() > -70) {
					Drive.tankDrive(DriveSpeed, -DriveSpeed);
					
				}else if (Gyro.getAngle() > -90 && Gyro.getAngle() <= -70) {
					Drive.tankDrive(-0.55, 0.55);
					
				}else if (Gyro.getAngle() <= -90) {
					Drive.tankDrive(0.0, 0.0);
					Right_Encoder.reset();
					Left_Encoder.reset();
					step = 8;
					}
				break;
				//Drive Forwards To Eliminate The Rest Of The Distance From The Switch
			case 8:
				if (Time.get() == 0) {
					Time.start();
				}
				if (Time.get() < 1) {
					Drive.tankDrive(-0.7, -0.7);
				}else if (Time.get() >= 0.25){
					Drive.tankDrive(0.0, 0.0);
					Time.stop();
					Left_Encoder.reset();
					Right_Encoder.reset();
					step = 9;
				}
				break;
				
				//Time Reset
			case 14:
				Time.reset();
				Time.stop();
				step = 9;
				break;
				
				//Score In Switch Using Timer Based Commands
			case 9:
				//Time Start and Termination
				if (Time.get()  == 0) {
					Time.start();
					}
					
					if (Time.get() < 1) {
						Lift_Motor.set(1.0);
					}else if (Time.get() <= 1.5 && Time.get() >= 1.1) {
						Intake_Arm.set(DoubleSolenoid.Value.kReverse);
						Lift_Motor.set(0.0);
					}else if(Time.get() <= 2.5 && Time.get() >= 2.0) {
							Intake.set(DoubleSolenoid.Value.kForward);
							Intake_Arm.set(DoubleSolenoid.Value.kReverse);
					}else if(Time.get() >= 3.5) {
						Lift_Motor.set(0.0);
						Intake_Arm.set(DoubleSolenoid.Value.kForward);
						Time.stop();
						step = -1;
				}
				break;
			
				//
				//Neither Right switch or Scale
				//
				
				//Drive Forwards Past The Switch 
			case 11:
				if (Right_Encoder.getRaw() <= 10000 && Left_Encoder.getRaw() <= 10000) {
					Drive.tankDrive(DriveSpeed - Steer, DriveSpeed + Steer);
				}else {
					Drive.tankDrive(0.0, 0.0);
					step = 13;
				}
				break;
				//Turn Towards The Center Of The Field
			case 13:
				if (Gyro.getAngle() > -70) {
					Drive.tankDrive(DriveSpeed, -DriveSpeed);
					
				}else if (Gyro.getAngle() > -90 && Gyro.getAngle() <= -70) {
					Drive.tankDrive(-0.55, 0.55);
					
				}else if (Gyro.getAngle() <= -90) {
					Right_Encoder.reset();
					Left_Encoder.reset();
					step = 12;
				}
				break;
				
				//Cover Some Of The Distance To The Power Cubes And Terminate The Case
			case 12:
				if (Right_Encoder.getRaw() < 1000 && Left_Encoder.getRaw() < 1000) {
					Drive.tankDrive(DriveSpeed, DriveSpeed);
				}else{
					Drive.tankDrive(0.0, 0.0);
					step = -1;
				}
				break;
				
			default:
				Drive.stopMotor();
				Lift_Motor.stopMotor();
				Auto_Stop = true;
				break;
			}	
			}
			break;
			
			
			case kAutoMidSwitch:
				//
				//Case Selection for MID SWITCH
				//
				if (right_Color_Switch == true && left_Color_Switch == false && step == 0) {
					step = 7;
				}
				if (right_Color_Switch == false && left_Color_Switch == true && step == 0) {
					step = 1;
				}
				
				
				
				while(Auto_Stop == false && DriverStation.getInstance().getMatchTime() > 0.5) {
					//Smart DashBoard Auto Stuff
					/*
					SmartDashboard.putNumber("Right",Right_Encoder.getRaw());
					SmartDashboard.putNumber("Left",Left_Encoder.getRaw());					
					SmartDashboard.putNumber("Angle", Gyro.getAngle());
					*/
					
					//Error Checking Variables
					Error = Gyro.getAngle();
					Steer = Error * P_Coef;
					
	
				switch(step) {
				//
				//Mid Switch Left
				//
				case 1:
					//Drive Forward
					if (Right_Encoder.getRaw() < 1000 && Left_Encoder.getRaw() < 1000) {
						Drive.tankDrive(DriveSpeed - Steer, DriveSpeed + Steer);
					}else{
						Drive.tankDrive(0.0, 0.0);
						step = 12;
					}
				break;
				//Turn To -90 Degrees
				case 12:
					if (Gyro.getAngle() > -70) {
						Drive.tankDrive(-0.7, 0.7);
						
					}else if (Gyro.getAngle() > -90 && Gyro.getAngle() <= -70) {
						Drive.tankDrive(-0.7, 0.7);
						
					}else if (Gyro.getAngle() <= -90){
						Drive.tankDrive(0.0, 0.0);
						Right_Encoder.reset();
						Left_Encoder.reset();
						step = 2;
					}
					break;
				//Drive Forward Again 
				case 2:
					if(Right_Encoder.getRaw() < 2000 && Left_Encoder.getRaw() < 2000) {
						Drive.tankDrive(DriveSpeed, DriveSpeed);
					}else {
						Drive.tankDrive(0.0, 0.0);
						step = 13;
					}
					break;
				//Turn Again Towards Switch
				case 13:
					if (Gyro.getAngle() < -30) {
						Drive.tankDrive(0.7, -0.7);
						
					}else if (Gyro.getAngle() < 0 && Gyro.getAngle() > -30) {
						Drive.tankDrive(0.7, -0.7);
						
					}else if (Gyro.getAngle() >= 0){
						Drive.tankDrive(0.0, 0.0);
						Right_Encoder.reset();
						Left_Encoder.reset();
						step = 3;
					}
					break;
					
				case 3:
					//Drive Forward To Switch
					if (Right_Encoder.getRaw() < 2600 && Left_Encoder.getRaw() < 2600) {
						Drive.tankDrive(DriveSpeed - Steer, DriveSpeed + Steer);
					}else {
						Drive.tankDrive(0.0, 0.0);
						step = 4;
					}
	
					//Score In The Switch And Termination
				case 4:
					//Time Start and Termination
					if (Time.get()  == 0) {
						Time.start();
						}
						
						if (Time.get() < 1) {
							Lift_Motor.set(1.0);
						}else if (Time.get() <= 1.5 && Time.get() >= 1.1) {
							Intake_Arm.set(DoubleSolenoid.Value.kReverse);
							Lift_Motor.set(0.0);
						}else if(Time.get() <= 2.5 && Time.get() >= 2.0) {
								Intake.set(DoubleSolenoid.Value.kForward);
						}else if(Time.get() >= 3.5) {
							Lift_Motor.set(0.0);
							Intake_Arm.set(DoubleSolenoid.Value.kForward);
							Time.stop();
							step = -1;
						}
					break;
				
				case 28:
					if (Right_Encoder.getRaw() > 1000 && Left_Encoder.getRaw() > 1000) {
						Drive.tankDrive(-DriveSpeed - Steer, DriveSpeed + Steer);
					}else {
						Drive.tankDrive(0.0, 0.0);
						step = -1;
					}
					break;
				//
				//Mid Right Switch
				//
					
				//Drive Forward From start
				case 7:
					if (Right_Encoder.getRaw() < 1000 && Left_Encoder.getRaw() < 1000) {
						Drive.tankDrive(DriveSpeed - Steer, DriveSpeed + Steer);
					}else {
						Drive.tankDrive(0.0, 0.0);
					step++;
					}
				break;
				
				//Turn To 90 Degrees
				case 8:
					if (Gyro.getAngle() < 70) {
						Drive.tankDrive(0.7, -0.7);
						
					}else if (Gyro.getAngle() < 90 && Gyro.getAngle() >= 70) {
						Drive.tankDrive(0.7, -0.7);
						
					}else if (Gyro.getAngle() >= 90){
						Drive.tankDrive(0.0, 0.0);
						Right_Encoder.reset();
						Left_Encoder.reset();
						step++;
					}
					break;

					//Drive Forward A Little Bit More
					case 9:
					if (Right_Encoder.getRaw() <  1800 && Left_Encoder.getRaw() < 1800) {
						Drive.tankDrive(DriveSpeed, DriveSpeed);
					}else {
						Drive.tankDrive(0.0, 0.0);
						step++;
					}
						break;
						
					//Turn To Face The Switch
					case 10:
						if (Gyro.getAngle() > 30) {
							Drive.tankDrive(-0.7, 0.7);
							
						}else if (Gyro.getAngle() > 0 && Gyro.getAngle() <= 30) {
							Drive.tankDrive(-0.7, 0.7);
							
						}else if (Gyro.getAngle() <= 0){
							Drive.tankDrive(0.0, 0.0);
							Right_Encoder.reset();
							Left_Encoder.reset();
							step++;
						}
						break;
						
					//Drive To The Switch
					case 11:
						if (Right_Encoder.getRaw() < 2600 && Left_Encoder.getRaw() < 2600){
							Drive.tankDrive(DriveSpeed - Steer, DriveSpeed + Steer);
						}else {
							Drive.tankDrive(0.0, 0.0);
							step = 24;
 						}
						break;
					//Score In The Switch Using Timer Based Commands And Termination
					case 24:
						//Time Start
						if (Time.get()  == 0) {
							Time.start();
							}
							
							if (Time.get() < 1) {
								Lift_Motor.set(1.0);
							}else if (Time.get() <= 1.5 && Time.get() >= 1.1) {
								Intake_Arm.set(DoubleSolenoid.Value.kReverse);
								Lift_Motor.set(0.0);
							}else if(Time.get() <= 2.5 && Time.get() >= 2.0) {
									Intake.set(DoubleSolenoid.Value.kForward);
									Intake_Arm.set(DoubleSolenoid.Value.kReverse);
							}else if(Time.get() >= 3.5) {
								Lift_Motor.set(0.0);
								Intake_Arm.set(DoubleSolenoid.Value.kForward);
								Time.stop();
								step = -1;
						}
						break;
						//Default
						default:
							Drive.tankDrive(0.0, 0.0);
							Lift_Motor.stopMotor();
							Auto_Stop = true;
							break;
				}
				}
				break;
			
			case kAutoMidScale:
			//
			//Case Selection for MID SCALE
			//
			if (left_Color_Scale == true && right_Color_Scale == false && step == 0) {
				step = 1;
			}
			if (left_Color_Scale == false && right_Color_Scale == true && step == 0) {
				step = 10;
			}
				
				while(Auto_Stop == false && DriverStation.getInstance().getMatchTime() > 0.5) {
					//Auto Testing Stuff
					/*
					SmartDashboard.putNumber("Right",Right_Encoder.getRaw());
					SmartDashboard.putNumber("Left",Left_Encoder.getRaw());					
					SmartDashboard.putNumber("Angle", Gyro.getAngle());
					*/
					
					//Error Checking Variables
					Error = Gyro.getAngle();
					Steer = Error * P_Coef;
				//
				//Mid Left Scale 
				//
				switch(step) {
				
				//Drive Forwards From The Start
				case 1:
				if (Right_Encoder.getRaw() < 1000 && Left_Encoder.getRaw() < 1000) {
					Drive.tankDrive(DriveSpeed - Steer, DriveSpeed + Steer);
				}else {
					Drive.tankDrive(0.0, 0.0);
					step++;
				}
					break;
				//Turn To -90 Degrees
				case 2:
				if (Gyro.getAngle() > -90) {
					Drive.tankDrive(-0.55, 0.55);
				}else {
					Drive.tankDrive(0.0, 0.0);
					Right_Encoder.reset();
					Left_Encoder.reset();
					step++;
				}
					break;
				//Drive To The Edge Of The Field
				case 3:
					if (Right_Encoder.getRaw() < 5000 && Left_Encoder.getRaw() < 5000) {
						Drive.tankDrive(DriveSpeed, DriveSpeed);
					}else {
						Drive.tankDrive(0.0, 0.0);
						step++;
					}
					break;
				//Turn To 0 Degrees On The Edge Of The Field
				case 4:
					if (Gyro.getAngle() < 0) {
						Drive.tankDrive(0.55, -0.55);
					}else {
						Drive.tankDrive(0.0, 0.0);
						Right_Encoder.reset();
						Left_Encoder.reset();
						step++;
					}
					break;
				//Drive To A Position Parallel The Scale
				case 5:
					if (Right_Encoder.getRaw() < 12000 && Left_Encoder.getRaw() < 12000) {
						Drive.tankDrive(DriveSpeed - Steer, DriveSpeed + Steer);
					}else {
						Drive.tankDrive(0.0, 0.0);
						step++;
					}
					break;
				//Turn To Face The Scale
				case 6:
					if (Gyro.getAngle() < 80) {
						Drive.tankDrive(0.55, -0.55);
					}else {
						Drive.tankDrive(0.0, 0.0);
						Right_Encoder.reset();
						Left_Encoder.reset();
						step++;
					}
					break;
				//Drive The Remaining Distance To The Scale
				case 7:
					if (Right_Encoder.getRaw() < 0 && Left_Encoder.getRaw() < 0) {
						Drive.tankDrive(-0.75, -0.75);
					}else {
						Drive.tankDrive(0.0, 0.0);
						step++;
					}
					break;
				//Score In The Scale Using Timer Based Commands
				case 8:
					if (Time.get() == 0) {
						Time.start();
					}
					
					if(Time.get() < 2.8) {
						Lift_Motor.set(1.0);
						}else if(Time.get() < 3.8) {
							Intake_Arm.set(DoubleSolenoid.Value.kReverse);
							Lift_Motor.set(0.0);
						}else if (Time.get() < 4.8) {
							Intake.set(DoubleSolenoid.Value.kForward);
							Intake_Arm.set(DoubleSolenoid.Value.kReverse);
						}else if(Time.get() < 5) {
							Intake_Arm.set(DoubleSolenoid.Value.kForward);
							Time.stop();
							step = -1;
					}
					break;
					//Turn Around And Terminate The Case
				case 9:
					if (Gyro.getAngle() < 180) {
						Drive.tankDrive(0.55, -0.55);
					}else {
						Drive.tankDrive(0.0, 0.0);
						Right_Encoder.reset();
						Left_Encoder.reset();
						step = -1;
					}
					break;
				//
				//Right Scale Auto
				//
					
				//Drive Forward From Starting Position
				case 10:
					if (Right_Encoder.getRaw() < 1000 && Left_Encoder.getRaw() < 1000) {
						Drive.tankDrive(DriveSpeed - Steer, DriveSpeed + Steer);
					}else {
						Drive.tankDrive(0.0, 0.0);
						step++;
					}					
					break;
				//Turn To 90 Degrees
				case 11:
					if (Gyro.getAngle() < 80) {
						Drive.tankDrive(0.55, -0.55);
					}else {
						Drive.tankDrive(0.0, 0.0);
						Right_Encoder.reset();
						Left_Encoder.reset();
						step++;
					}					
					break;
				//Drive Forwards To The Edge Of The Field
				case 12:
					if (Right_Encoder.getRaw() < 4600 && Left_Encoder.getRaw() < 4600) {
						Drive.tankDrive(DriveSpeed, DriveSpeed);
					}else {
						Drive.tankDrive(0.0, 0.0);
						step++;
					}
					break;
				//Turn On The Edge Of The Field To 0 Degrees
				case 13:
					if (Gyro.getAngle() > 0) {
						Drive.tankDrive(-0.55, 0.55);
					}else {
						Drive.tankDrive(0.0, 0.0);
						Right_Encoder.reset();
						Left_Encoder.reset();
						step++;
					}
						break;
				//Drive To A Position Parallel To The Scale
				case 14:
					if (Right_Encoder.getRaw() < 12000 && Left_Encoder.getRaw() < 12000) {
						Drive.tankDrive(DriveSpeed - Steer, DriveSpeed + Steer);
					}else {
						Drive.tankDrive(0.0, 0.0);
						step++;
					}
					break;
				//Turn To Face Scale
				case 15:
					if (Gyro.getAngle() > -90) {
						Drive.tankDrive(-0.55, 0.55);
					}else {
						Drive.tankDrive(0.0, 0.0);
						Right_Encoder.reset();
						Left_Encoder.reset();
						step++;
					}
					break;
				//Drive To The Scale
				case 16:
					if (Right_Encoder.getRaw() > -300 && Left_Encoder.getRaw() > -300) {
						Drive.tankDrive(0.75, 0.75);
					}else {
						Drive.tankDrive(0.0, 0.0);
						step++;
					}
					break;
				//Score In The Scale Using Timer Based commands
				case 17:
					if (Time.get() == 0) {
						Time.start();
					}
					
					if(Time.get() < 2.8) {
						Lift_Motor.set(1.0);
						}else if(Time.get() < 3.8) {
							Intake.set(DoubleSolenoid.Value.kReverse);
							Lift_Motor.set(0.0);
						}else if (Time.get() < 4.8) {
							Intake.set(DoubleSolenoid.Value.kForward);
							Intake_Arm.set(DoubleSolenoid.Value.kReverse);
						}else if(Time.get() < 5) {
							Intake_Arm.set(DoubleSolenoid.Value.kForward);
							Time.stop();
							step = -1;
					}
					break;
				//Turn And Terminate The Case
				case 18:
					if (Gyro.getAngle() > -180) {
						Drive.tankDrive(-0.55, 0.55);
					}else {
						Drive.tankDrive(0.0, 0.0);
						Right_Encoder.reset();
						Left_Encoder.reset();
						step = -1;
					}
					break;
					//Default
					default:
						Drive.tankDrive(0.0, 0.0);
						Lift_Motor.stopMotor();
						Auto_Stop = true;
						break;
					
				}
				}
				break;
				
			//
			//Auto End
			//
				
			case kDefaultAuto:
			default:
				SmartDashboard.putNumber("Match Time", DriverStation.getInstance().getMatchTime());
				Drive.tankDrive(0.0, 0.0);
				break;
		}
		Drive.setSafetyEnabled(true);
	}

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		
		//Match Time
		//SmartDashboard.putNumber("Match Time", DriverStation.getInstance().getMatchTime());
		
		//Comp
		Comp.start();
		
		//Drive(s)
		
			//Arcade Drive
		//Drive.arcadeDrive(Arcade.getRawAxis(1), Arcade.getRawAxis(0));
		
			//tank Drive
		
		Drive.tankDrive(Left.getRawAxis(5)  * 0.8, Right.getRawAxis(1) * 0.8);
		
		//Shifting Command
		 if(Right.getRawAxis(7) >= 0.5) {
			Shifting.set(DoubleSolenoid.Value.kForward);
		}else if (Left.getRawAxis(7) >= 0.5) {
			Shifting.set(DoubleSolenoid.Value.kReverse);
		}else {
			Shifting.set(DoubleSolenoid.Value.kOff);
		}
		
		//Intake Claw Command
		if (Right.getRawButton(2)) {
			Intake.set(DoubleSolenoid.Value.kForward);
		}else if (Left.getRawButton(2)) {
			Intake.set(DoubleSolenoid.Value.kReverse);
		}else {
			Intake.set(DoubleSolenoid.Value.kOff);
		}

		//Lift (Up / Down) Intake Claw Commands
		if (Actuators.getRawButton(3)) {
	Intake_Arm.set(DoubleSolenoid.Value.kForward);
		}else if (Actuators.getRawButton(4)) {
			Intake_Arm.set(DoubleSolenoid.Value.kReverse);
		}else {
			Intake_Arm.set(DoubleSolenoid.Value.kOff);
		}	
		
		//Lift Command
		if (Left.getRawButton(1) == true) {
			Lift_Motor.set(-0.75);
		}else if (Right.getRawButton(1) == true && Limit.get() == false) {
			Lift_Motor.set(0.75);
		}else if (Right.getRawButton(1) == true && Limit.get() == true) {
			Lift_Motor.set(0.0);
		}else {
			Lift_Motor.set(0.0);
		}
	
		//Limit Switch Test
//		SmartDashboard.putBoolean("limit", Limit.get());
		
		
		//Climb Command
	/*	
	if (Actuators.getRawButton(12)) {
		Climb_One.set(1.0);
		Climb_Two.set(1.0);
		}else if (Actuators.getRawButton(1)) {
			Climb_One.set(-1.0);
			Climb_Two.set(-1.0); 
		}else {
			Climb_One.set(0.0);
			Climb_Two.set(0.0);
		}
		*/
		}
	

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
		//Comp
		Comp.start();
		
		//Test Mode Stuff
		LiveWindow.add(Drive);
		LiveWindow.add(Intake_Arm);
		LiveWindow.add(Climb_One);
		LiveWindow.add(Climb_Two);
		LiveWindow.add(Shifting);
		LiveWindow.add(Intake);
		LiveWindow.add(Lift_Motor);
			
		LiveWindow.add(Gyro);
		Gyro.calibrate();
	
	}
}
