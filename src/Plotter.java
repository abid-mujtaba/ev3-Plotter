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

    static final int SPEED = 100;            // The speed for all three motors

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


    static final int ASSEMBLY_LENGTH = 180;     // Rotation angle for the assembly that makes it move across it's entire length
    static final int PEN_LENGTH = 40;           // Angle that lifts pen sufficiently
    static final int ROVER_LENGTH = 100;         // Angle that moves rover forward equal to the full range of motion of the assembly

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

        // After a successful adjustment the assembly is at position ASSEMBLY_LENGTH (all the way to the right) and is prepared for limit tracking.
        mAssemblyPos = 0; //ASSEMBLY_LENGTH;
    }

    // Method that tells the EV3 what to plot.
    private static void plot()
    {
        polar(10, 180);      // Move left
        polar(10, 90);       // Move forward
        polar(14.14, 315);   // Move diagonally back to the origin

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


    /** Controls the inherently x-y plotter in a polar fashion by adjusting lengths and speeds
     *
     * The assembly and rover wheels have different radii so they move by different absolute amounts for the same angular rotation. We take the maximum possible movement by the assembly to be equal to 10 UNITs of motion.
     *
     * The relation between angles and absolute distances in terms of UNITs is given by:
     *
     *      Assembly: 10 UNITs = 120
     *      Rover:    10 UNITs =  75
     *
     * This method takes magnitude in UNITs and an angle in degrees and uses it to calculate the SPEEDs and angles that will move the pen in the specified polar direction.
     */

    private static int UNITS = 10;         // The number of standard units per max assembly motion. (The number of UNITs that corresponds to the assembly moving across its entire length)

    private static void polar(double mag, int degree_angle)
    {
        // We use trigonometry to calculate the x and y movement the pen must make in UNITs
        double angle = degree_angle / 180.0 * Math.PI;

        double x = mag * Math.cos(angle);
        double y = mag * Math.sin(angle);

        // With x and y UNITs in hand we use them to calculate the speeds and angles of the motors. Angles first:
        int x_angle = (int) (x / UNITS * ASSEMBLY_LENGTH);
        int y_angle = (int) (y / UNITS * ROVER_LENGTH);

        // The SPEEDs need to be adjusted so that the maximum value is SPEED (set globally) and so that both x and y motions start and (more importantly) end at the same time so it is about matching the "time"
        // of motions now that the displacement has been set. Since speed = distance / time => time = distance / speed. and so to match times the SPEEDs must be in the same ratio as the distances (angles).

        // We just need to ensure that the larger of the two speeds is equal to the global value SPEED:

        int xSpeed, ySpeed;

        if (Math.abs(x_angle) > Math.abs(y_angle))
        {
            xSpeed = SPEED;

            ySpeed = (int) Math.abs((double) y_angle / x_angle * SPEED);
        }
        else
        {
            ySpeed = SPEED;

            xSpeed = (int) Math.abs((double) x_angle / y_angle * SPEED);
        }

        // With the calculations complete we simply set the necessary speeds and rotate by the calculated angles.

        assembly.setSpeed(xSpeed);
        rover.setSpeed(ySpeed);

        if (y_angle == 0)           // if the y_angle is 0 then move_assembly must NOT return immediately since that would cause the execution to effectively skip this instruction
        {
            move_assembly(-x_angle, false);
        }
        else
        {
            move_assembly(-x_angle, true);      // We need the negative sign because our coordinate system (x-y) and its positive directions correspond to the negative rotational direction of both motors
            rover.rotate(-y_angle, false);
        }
    }


    private static void move_assembly(int angle, boolean immediate_return)
    {
        int new_pos = angle + mAssemblyPos;

        if (new_pos < 0 || new_pos > ASSEMBLY_LENGTH)           // The new motion will cause the assembly to come of the end of the tracks
        {
            log("Error: Moving by this angle will cause assembly to move off tracks. Motion Denied.");
            return;
        }

        assembly.rotate(angle, immediate_return);

        mAssemblyPos += angle;      // Update the current position of the assembly
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