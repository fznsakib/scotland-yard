package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardPlayer;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;

public abstract class ValidMovesFinder {

    ScotlandYardPlayer currentPlayer;
    Collection<Node> playerLocations;

    ValidMovesFinder(ScotlandYardView view, ScotlandYardPlayer currentPlayer) {
        playerLocations = new ArrayList<>();
        this.currentPlayer = currentPlayer;

        for (Colour colour : view.getPlayers()) {
            // Add current player locations to an array to check whether destination is occupied later on
            if (!colour.equals(Black)) {
                playerLocations.add(view.getGraph().getNode(view.getPlayerLocation(colour)));
            }
        }
    }

    // The main method to find valid moves for the corresponding type of player
    public abstract Set<Move> findValidMoves();
}

