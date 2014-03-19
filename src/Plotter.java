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


    public static void main(String[] args)
    {
        log("Program starts");



        log("Program ends");
    }


    private static void log(String msg)
    {
        System.out.println("log>\t" + msg);
    }
}
