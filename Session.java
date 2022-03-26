package com.example.cosc195cst107finalproject;

/**
 *
 * This class defines the data object containing stats per one session of 10 rounds.
 *
 * @author Jonathan Cruz CST107
 * @version 1
 */
public class Session
{
    public long id;
    public String name;
    public long correct;

    public Session(String name, long correct)
    {
        this.name = name;
        this.correct = correct;
    }

    public Session(long id, String name, long correct)
    {
        this(name, correct);
        this.id = id;
    }
}
