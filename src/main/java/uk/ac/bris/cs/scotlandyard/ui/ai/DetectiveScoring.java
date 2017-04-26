package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardPlayer;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;

import java.util.List;


public class DetectiveScoring extends Scoring {

    private int totalScore;
    private DijkstraPath boardPath;
    private ScotlandYardView view;
    private List<ScotlandYardPlayer> players;


    public DetectiveScoring(ScotlandYardView view, int destination, List<ScotlandYardPlayer> players)
    {
        super(view, destination, players);
        this.totalScore = 0;
        this.view = view;
        this.boardPath = new DijkstraPath(destination, view);
    }

    @Override
    public int totalScore() {
        totalScore = distanceScore();
        return totalScore;
    }

    @Override
    public int distanceScore() {
        // Checks how far away MrX is from the node
        //System.out.println("MrX Location: " + view.getPlayerLocation(Colour.Black));
        int distanceFromMrX = boardPath.getDistanceFrom(view.getPlayerLocation(Colour.Black));

        return distanceFromMrX;
    }

    @Override
    public int ticketScore() {
        return 0;
    }

}
