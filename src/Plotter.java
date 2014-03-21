/*
 *  Copyright 2014 Abid Hasan Mujtaba
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

/**
 * LeJOS project that implements a rover-based Plotter.
 *
 * It consists of controlling three motors:
 *
 *  (i) Raise and lower the pen.
 *  (ii) Move the pen assembly laterally.
 *  (iii) Drive the Rover forwards and backwards.
 */

public class Plotter
{
    static RegulatedMotor pen = Motor.A;
    static RegulatedMotor assembly = Motor.B;
    static RegulatedMotor rover = Motor.C;

    static final int SPEED = 50;            // The speed for all three motors

    // Define the various Button IDs (extracted from the LeJOS Button class) used with result of Button.waitForAnyEvent()
    static final int RELEASE_EVENT_SHIFT = 8;          // Bit shift required to convert button down to button up

    static final int UP_Press = 0x1;
    static final int UP_Release = UP_Press << RELEASE_EVENT_SHIFT;

    static final int DOWN_Press = 0x4;
    static final int DOWN_Release = DOWN_Press << RELEASE_EVENT_SHIFT;

    static final int LEFT_Press = 0x10;
    static final int LEFT_Release = LEFT_Press << RELEASE_EVENT_SHIFT;

    static final int RIGHT_Press = 0x8;
    static final int RIGHT_Release = RIGHT_Press << RELEASE_EVENT_SHIFT;

    static final int CENTER_Press = 0x2;



    static final int ASSEMBLY_LENGTH = 120;     // Rotation angle for the assembly that makes it move across it's entire length
    static final int PEN_LENGTH = 40;           // Angle that lifts pen sufficiently
    static final int ROVER_LENGTH = 75;         // Angle that moves rover forward equal to the full range of motion of the assembly

    static int mAssemblyPos = 0;            // Keeps track of the current position of the assembly



    public static void main(String[] args)
    {
        log("Program starts");

        initiate();

        log("Program ends");
    }


    // Method for initiating the application.
    private static void initiate()
    {
        LCD.clear();
        LCD.drawString("PLOTTER", 2, 2);

        pen.setSpeed(SPEED);
        assembly.setSpeed(SPEED);
        rover.setSpeed(SPEED);

        lejos.hardware.Sound.beep();            // Announces that the EV3 is ready for user input
        initial_adjustment();

        LCD.clear();
        LCD.drawString("PLOTTER", 2, 2);

        plot();

        LCD.clear();
        LCD.refresh();
    }


    // Allow the user to use the buttons to perform an adjustment of the pen and assembly placing it in the initial state (pen down and assembly all the way to the right)
    private static void initial_adjustment()
    {
        LCD.drawString("Adjusting ...", 4, 4);

        boolean flag = true;

        while (flag)
        {
            int event = Button.waitForAnyEvent();

            switch (event)
            {
                case UP_Press:
                    forward(pen);
                    break;

                case DOWN_Press:
                    reverse(pen);
                    break;

                case UP_Release:
                case DOWN_Release:
                    stop(pen);
                    break;

                case RIGHT_Press:
                    forward(assembly);
                    break;

                case LEFT_Press:
                    reverse(assembly);
                    break;

                case RIGHT_Release:
                case LEFT_Release:
                    stop(assembly);
                    break;

                case CENTER_Press:
                    flag = false;
            }
        }
    }

    // Method that tells the EV3 what to plot.
    private static void plot()
    {
        left(80, false);
        forward(80, false);

        right(80, true);
        reverse(80, false);

        raise_pen();
    }


    private static void raise_pen()
    {
        pen.rotate(PEN_LENGTH);
    }


    private static void lower_pen()
    {
        pen.rotate(-PEN_LENGTH);
    }


    private static void right(int percent, boolean immediate_return)
    {
        move_assembly(-percent, immediate_return);
    }


    private static void left(int percent, boolean immediate_return)
    {
        move_assembly(percent, immediate_return);
    }


    private static void move_assembly(int percent, boolean immediate_return)
    {
        int angle = (int) (percent / 100.0 * ASSEMBLY_LENGTH);
        int new_angle = angle + mAssemblyPos;

        if (new_angle < 0 || new_angle > ASSEMBLY_LENGTH)           // The new motion will cause the assembly to come of the end of the tracks
        {
            log("Error: Moving by this angle will cause assembly to move off tracks. Motion Denied.");
            return;
        }

        assembly.rotate(angle, immediate_return);

        mAssemblyPos += angle;      // Update the current position of the assembly
    }


    private static void forward(int percent, boolean immediate_return)
    {
        move_rover(-percent, immediate_return);
    }


    private static void reverse(int percent, boolean immediate_return)
    {
        move_rover(percent, immediate_return);
    }


    private static void move_rover(int percent, boolean immediate_return)
    {
        int angle = (int) (percent / 100.0 * ROVER_LENGTH);

        rover.rotate(angle, immediate_return);
    }



    // Methods for controlling all three of the motors in an abstract fashion:
    private static void forward(RegulatedMotor motor)
    {
        motor.forward();
    }


    private static void reverse(RegulatedMotor motor)
    {
        motor.backward();
    }


    private static void stop(RegulatedMotor motor)
    {
        motor.stop();
    }


    private static void log(String msg)
    {
        System.out.println("log>\t" + msg);
    }


    private static void delay(long interval)         // Wait for specified amount of milliseconds
    {
        try
        {
            Thread.sleep(interval);
        }
        catch (InterruptedException e) {}
    }
}