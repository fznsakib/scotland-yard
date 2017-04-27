package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;


public class GameConfig {

    private ScotlandYardView view;
    private List<GameTreePlayer> players;
    private int MrXScore;
    private Move move;

    GameConfig(ScotlandYardView view, List<GameTreePlayer> players, Move move)
    {
        this.view = view;
        this.players = players;
        this.move = move;
        MrXScore = new MrXScoring(view, players.get(0).location(), players).totalScore();
    }

    public int getMrXScore() {
        return MrXScore;
    }

    public List<GameTreePlayer> getPlayers(){
        return players;
    }

    public Move getMove() {
        return move;
    }
}
