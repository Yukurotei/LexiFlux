package it.yuruni.game;

import com.badlogic.gdx.Input;

import java.util.ArrayList;
import java.util.List;

public class GameConstants {
    /**
     * List of all keys that can be assigned to notes in the game.
     * Map makers can only use keys from this list.
     */
    public static final List<Integer> PLAYABLE_KEYS = new ArrayList<Integer>() {{
        add(Input.Keys.Q); add(Input.Keys.W); add(Input.Keys.E); add(Input.Keys.R); add(Input.Keys.T);
        add(Input.Keys.Y); add(Input.Keys.U); add(Input.Keys.I); add(Input.Keys.O); add(Input.Keys.P);
        add(Input.Keys.A); add(Input.Keys.S); add(Input.Keys.D); add(Input.Keys.F); add(Input.Keys.G);
        add(Input.Keys.H); add(Input.Keys.J); add(Input.Keys.K); add(Input.Keys.L); add(Input.Keys.SEMICOLON);
        add(Input.Keys.Z); add(Input.Keys.X); add(Input.Keys.C); add(Input.Keys.V); add(Input.Keys.B);
        add(Input.Keys.N); add(Input.Keys.M); add(Input.Keys.COMMA); add(Input.Keys.PERIOD);
    }};

    private GameConstants() {
        // Prevent instantiation
    }
}
