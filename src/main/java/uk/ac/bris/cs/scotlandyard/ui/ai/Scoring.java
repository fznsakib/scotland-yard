package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;

public abstract class Scoring {

    private int totalScore;
    private int destination;
    private DijkstraPath boardPath;
    private ScotlandYardView view;

    Scoring(ScotlandYardView view, int destination)
    {
        this.totalScore = 0;
        this.destination = destination;
        this.view = view;
        this.boardPath = new DijkstraPath(destination, view);
    }

    // A scoring system to allow the AI to choose/prioritise the best moves available. The higher the score, the better

    // Sums up all the individual scores for a move that are generated according to some parameters (i.e. distance from
    // detectives, move eligibility of detectives to a certain location, contextual multipliers, etc)
    public abstract int totalScore();

    // Provides a score for a move depending on how far away all the detectives are from a location.
    public abstract int distanceScore();

    // Produces a score based on how likely it is a detective can reach a location by checking their tickets
    public abstract int ticketScore();

}

