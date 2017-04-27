package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.*;
import java.util.*;
import uk.ac.bris.cs.scotlandyard.model.*;


public class MrXScoring extends Scoring {

    private int totalScore;
    private int destination;
    private DijkstraPath boardPath;
    private ScotlandYardView view;
    private List<GameTreePlayer> players;

    MrXScoring(ScotlandYardView view, int destination, List<GameTreePlayer> players)
    {
        super(view, destination, players);
        this.totalScore = 0;
        this.destination = destination;
        this.view = view;
        this.boardPath = new DijkstraPath(destination, view);
        this.players = players;
    }

    @Override
    public int totalScore()
    {
        //totalScore = distanceScore() + freedomScore() + transportScore();
        totalScore = distanceScore() + freedomScore() + transportScore();
        return totalScore;
    }

    // Produces a score for a move based on how much freedom of movement there is at that destination
    private int freedomScore()
    {
        // The default or 'ideal' freedom score is the same as the number of detectives
        // This method traverses all the adjacent nodes, and for every detective found at a node, the score
        // is subtracted by 4

        int freedomScore = (players.size() - 1) * 4;
        Collection<Edge<Integer, Transport>> adjacentEdges;
        adjacentEdges = view.getGraph().getEdgesFrom(view.getGraph().getNode(destination));

        // Get detective locations
        Set<Integer> detectiveLocations = new HashSet<>();
        for (int i = 1; i < view.getPlayers().size(); i ++)
        {
            detectiveLocations.add(players.get(i).location());
            //detectiveLocations.add(view.getPlayerLocation(view.getPlayers().get(i)));
        }

        for (Edge<Integer, Transport> edge : adjacentEdges)
        {
            if (detectiveLocations.contains(edge.destination().value()))
                freedomScore = freedomScore - 4;
        }

        return freedomScore;
    }

    @Override
    public int distanceScore()
    {
        int distanceScore = 0;

        // Cycles through each detective to see how far away they are from the chosen adjacent node
        for (int i = 1; i < players.size(); i ++)
        {
            int detectiveLocation = players.get(i).location();
            int distanceFromDetective = boardPath.getDistanceFrom(detectiveLocation);
            distanceScore = distanceScore + (2 * distanceFromDetective);
        }

        return distanceScore;
    }

    @Override
    public int ticketScore()
    {
        int ticketScore = 0;

        for (int i = 1; i < players.size(); i ++)
        {
            if (!hasEnoughTickets(players.get(i).colour()))
            {
                ticketScore = ticketScore + 3;
            }
        }
        return ticketScore;
    }

    // Produces a score by seeing how many modes of transport there is from that node
    private int transportScore()
    {
        int transportScore = 0;
        Collection<Edge<Integer, Transport>> adjacentEdges;
        adjacentEdges = view.getGraph().getEdgesFrom(view.getGraph().getNode(destination));
        ArrayList<Transport> transportAvailable = new ArrayList<>();

        for (Edge<Integer, Transport> edge : adjacentEdges)
        {
            transportAvailable.add(edge.data());
        }

        if (transportAvailable.contains(Transport.Bus))
            transportScore = transportScore + 1;
        if (transportAvailable.contains(Transport.Underground))
            transportScore = transportScore + 2;

        return transportScore;
    }

    private boolean hasEnoughTickets(Colour playerColour)
    {
        LinkedList<Edge<Integer, Transport>> shortestPath = boardPath.getPathFrom(view.getPlayerLocation(playerColour));
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
