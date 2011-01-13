package com.hlidskialf.android.game.util;


public class MathHelper

{


    public static float angle_of_vector(float x, float y)
    {
        double angle = Math.atan(x / y) * 180 / Math.PI;
        angle = y > 0 ? 90 + angle : 270 + angle;
        return 270f - (float)angle;
    }
}
