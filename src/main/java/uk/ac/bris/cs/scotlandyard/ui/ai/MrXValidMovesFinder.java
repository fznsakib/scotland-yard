package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;

import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Double;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.*;

public class MrXValidMovesFinder extends ValidMovesFinder {

    private Set<Move> MrXMoves;
    private ScotlandYardView view;

    MrXValidMovesFinder(ScotlandYardView view, GameTreePlayer currentPlayer) {
        super(view, currentPlayer);
        MrXMoves = new HashSet<>();
        this.view = view;
    }

    @Override
    public Set<Move> findValidMoves() {
        // Logic for valid moves for MrX
        //System.out.println(view.getPlayerLocation(Black));
        MrXMoves = addTickets(currentPlayer.location());

        // Create copy of MrXMoves so that 'ConcurrentModificationException' is avoided. i.e. adding
        // elements while iterating through it
        Set<Move> firstMoves = new HashSet<>();
        firstMoves.addAll(MrXMoves);

        // If MrX has double tickets and there are enough rounds left, find valid double moves
        if (currentPlayer.hasTickets(Double, 1) && ((view.getRounds().size() - view.getCurrentRound()) > 1)) {
            for (Move firstMove : firstMoves) {
                if (firstMove instanceof TicketMove) {
                    currentPlayer.removeTicket(((TicketMove) firstMove).ticket()); // Remove ticket for first move

                    // Create another array for possible consecutive moves for a double move
                    Set<Move> secondMoves = addTickets(((TicketMove) firstMove).destination());

                    for (Move secondMove : secondMoves) {
                        if (secondMove instanceof TicketMove) { // Add double move
                            MrXMoves.add(new DoubleMove(Black, ((TicketMove) firstMove).ticket(), ((TicketMove) firstMove).destination(), ((TicketMove) secondMove).ticket(), ((TicketMove) secondMove).destination()));
                        }
                    }
                    // Add back ticket lost for consistency
                    currentPlayer.addTicket(((TicketMove) firstMove).ticket());
                }
            }
        }
        return Collections.unmodifiableSet(MrXMoves);
    }

    // Created a separate method as addTickets may need to be called twice for Double Moves
    private Set<Move> addTickets(Integer currentPlayerLocation) {
        Set<Move> MrXMoves = new HashSet<>();

        // Populate edges list with available edges from current location
        Collection<Edge<Integer, Transport>> allEdges = view.getGraph().getEdgesFrom(view.getGraph().getNode(currentPlayerLocation));

        // If there is no detective at the destination and MrX has enough tickets to make the move, then add the
        // TicketMove to the list
        for (Edge<Integer, Transport> edge : allEdges) {
            if (!playerLocations.contains(edge.destination()) && currentPlayer.hasTickets(fromTransport(edge.data()), 1)) {
                MrXMoves.add(new TicketMove(Black, fromTransport(edge.data()), edge.destination().value()));
                // If MrX has a secret ticket, then add the secret version of that move as well
                if (view.getPlayerTickets(Black, Secret) > 0) {
                    MrXMoves.add(new TicketMove(Black, Secret, edge.destination().value()));
                }
            }

            // Add the secret version of a ticket if MrX does not have tickets for the original ticket move
            else if (!playerLocations.contains(edge.destination()) && !currentPlayer.hasTickets(fromTransport(edge.data()), 1) && currentPlayer.hasTickets(Secret))
            {
                MrXMoves.add(new TicketMove(Black, Secret, edge.destination().value()));
            }
        }
        return MrXMoves;
    }
}
