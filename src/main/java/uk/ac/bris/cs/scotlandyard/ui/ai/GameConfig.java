package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;


public class GameConfig {

    private ScotlandYardView view;
    private List<ScotlandYardPlayer> players;
    private int MrXScore;

    GameConfig(ScotlandYardView view, List<ScotlandYardPlayer> players)
    {
        this.view = view;
        this.players = players;
        MrXScore = new MrXScoring(view, players.get(0).location(), players).totalScore();
    }

    public int getMrXScore() {
        return MrXScore;
    }
}
