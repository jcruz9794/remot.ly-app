package com.example.cosc195cst107finalproject;
/**
 *
 * This class is a Photo data object which is used to track correct responses.
 *
 * @author Jonathan Cruz CST107
 * @version 1
 */
public class Photo
{
    public int round;
    public String correctResponse;

    public Photo(int round, String correctResponse)
    {
        this.round = round;
        this.correctResponse = correctResponse;
    }

    @Override public String toString()
    {
        return correctResponse;
    }
}
