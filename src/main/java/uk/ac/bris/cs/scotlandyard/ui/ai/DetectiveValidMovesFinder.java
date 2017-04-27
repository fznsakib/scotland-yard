package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static uk.ac.bris.cs.scotlandyard.model.Ticket.fromTransport;

public class DetectiveValidMovesFinder extends ValidMovesFinder {

    private Set<Move> detectiveMoves;
    private Collection<Edge<Integer, Transport>> allEdges;

    DetectiveValidMovesFinder(ScotlandYardView view, GameTreePlayer currentPlayer) {
        super(view, currentPlayer);
        detectiveMoves = new HashSet<>();
        allEdges = view.getGraph().getEdgesFrom(view.getGraph().getNode(currentPlayer.location()));
    }

    @Override
    public Set<Move> findValidMoves() {

        for (Edge<Integer, Transport> edge : allEdges) {
            // Check if current player has enough tickets to make the move, and check if a detective is
            // already at the destination. If conditions are met, then add ticket to moves
            if (!playerLocations.contains(edge.destination()) && currentPlayer.hasTickets(fromTransport(edge.data()), 1)) {
                detectiveMoves.add(new TicketMove(currentPlayer.colour(), fromTransport(edge.data()), edge.destination().value()));
            }
        }
        // If there are no valid edges to traverse on, add a pass move to the list of valid moves
        if (detectiveMoves.isEmpty()) {
            detectiveMoves.add(new PassMove(currentPlayer.colour()));
        }
        return Collections.unmodifiableSet(detectiveMoves);
    }
}
