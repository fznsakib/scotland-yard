package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.*;
import java.util.*;
import uk.ac.bris.cs.scotlandyard.model.*;


public class Scoring {

    private int totalScore;
    private int destination;
    private DijkstraPath boardPath;
    private ScotlandYardView view;

    public Scoring(ScotlandYardView view, int destination)
    {
        this.totalScore = 0;
        this.destination = destination;
        this.view = view;
        this.boardPath = new DijkstraPath(destination, view);
    }

    // A scoring system to allow the AI to choose/prioritise the best moves available. The higher the score, the better

    // Sums up all the individual scores for a move that are generated according to some parameters (i.e. distance from
    // detectives, move eligibility of detectives to a certain location, contextual multipliers, etc)
    public int totalScore()
    {
        totalScore = freedomScore() + distanceScore() + ticketScore();
        System.out.println("Freedom: " + freedomScore() + " Distance: " + distanceScore() + " Ticket: " + ticketScore() + " = " + totalScore);
        return totalScore;
    }

    // Produces a score for a move based on how much freedom of movement there is at that destination
    private int freedomScore()
    {
        // The default or 'ideal' freedom score is the same as the number of detectives
        // This method traverses all the adjacent nodes, and for every detective found at a node, the score
        // is subtracted by 1

        int freedomScore = view.getPlayers().size() - 1;
        Collection<Edge<Integer, Transport>> adjacentEdges;
        adjacentEdges = view.getGraph().getEdgesFrom(view.getGraph().getNode(destination));
                        //view.getGraph().getEdgesFrom(view.getGraph().getNode(currentNode))

        // Get detective locations
        Set<Integer> detectiveLocations = new HashSet<>();
        for (int i = 1; i < view.getPlayers().size(); i ++)
        {
            detectiveLocations.add(view.getPlayerLocation(view.getPlayers().get(i)));
        }

        for (Edge<Integer, Transport> edge : adjacentEdges)
        {
            if (detectiveLocations.contains(edge.destination().value()))
                freedomScore--;
        }

        return freedomScore;
    }

    // Provides a score for a move depending on how far away all the detectives are from a location.
    private int distanceScore()
    {
        int distanceScore = 0;

        // Cycles through each connected node to see if there is a detective there. If there is, add to the score
        for (int i = 1; i < view.getPlayers().size(); i ++)
        {
            int detectiveLocation = view.getPlayerLocation(view.getPlayers().get(i));
            int distanceFromDetective = boardPath.getDistanceFromDetective(detectiveLocation);
            distanceScore = distanceScore + distanceFromDetective;
            //System.out.println("Distance from Detective " + view.getPlayers().get(i) + ": " + distanceFromDetective);
        }

        return distanceScore;
    }

    // Produces a score based on how likely it is a detective can reach a location by checking their tickets
    private int ticketScore()
    {
        int ticketScore = 0;

        for (int i = 1; i < view.getPlayers().size(); i ++)
        {
            if (!hasEnoughTickets(view.getPlayers().get(i)))
            {
                ticketScore = ticketScore + 3;
            }
        }
        return ticketScore;
    }

    private boolean hasEnoughTickets(Colour playerColour)
    {
        LinkedList<Edge<Integer, Transport>> shortestPath = boardPath.getPathFromDetective(view.getPlayerLocation(playerColour));
        Map<Ticket, Integer> ticketsRequiredForPath = new HashMap<>();
        ticketsRequiredForPath.put(Ticket.Taxi, 0);
        ticketsRequiredForPath.put(Ticket.Bus, 0);
        ticketsRequiredForPath.put(Ticket.Underground, 0);

        // Check how many tickets of each kind is required for the path
        for (Edge<Integer, Transport> edge : shortestPath)
        {
            int taxi = 0, bus = 0, underground = 0;

            if (edge.data().equals(Transport.Taxi)) {
                taxi++;
                ticketsRequiredForPath.replace(Ticket.Taxi, taxi);
            }
            else if (edge.data().equals(Transport.Bus)){
                bus++;
                ticketsRequiredForPath.replace(Ticket.Bus, bus);
            }
            else if (edge.data().equals(Transport.Underground)){
                underground++;
                ticketsRequiredForPath.replace(Ticket.Underground, underground);
            }
        }

        boolean enoughTaxi = (view.getPlayerTickets(playerColour, Ticket.Taxi) >= ticketsRequiredForPath.get(Ticket.Taxi));
        boolean enoughBus = (view.getPlayerTickets(playerColour, Ticket.Bus) >= ticketsRequiredForPath.get(Ticket.Bus));
        boolean enoughUnderground = (view.getPlayerTickets(playerColour, Ticket.Underground) >= ticketsRequiredForPath.get(Ticket.Underground));

        if (!enoughTaxi || !enoughBus || !enoughUnderground)
            return false;
        else
            return true;
    }


}
