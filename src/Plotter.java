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


    static final int CENTER_Press = 0x2;



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

        initial_adjustment();

        LCD.clear();
        LCD.refresh();
    }


    // Allow the user to use the buttons to perform an adjustment of the pen and assembly placing it in the initial state (pen down and assembly all the way to the right)
    private static void initial_adjustment()
    {
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

                case CENTER_Press:
                    flag = false;
            }
        }
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
