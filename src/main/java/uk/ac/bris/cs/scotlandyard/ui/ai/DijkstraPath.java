package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.*;
import java.util.*;
import uk.ac.bris.cs.scotlandyard.model.*;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Bus;

public class DijkstraPath {

    private HashMap<Integer, Integer> distanceNodes = new HashMap<>();
    private Set<Integer> unsettledNodes = new HashSet<>();
    private Set<Integer> settledNodes = new HashSet<>();
    private HashMap<Integer, LinkedList<Edge<Integer, Transport>>> pathNodes = new HashMap<>();
    private ScotlandYardView view;

    public DijkstraPath(int start, ScotlandYardView view) {

        this.view = view;

        // Grab all nodes from current graph, and convert to nodes holding distance from source
        List<Node<Integer>> allNodes = view.getGraph().getNodes();

        // Populate maps with distances and paths
        for (Node node : allNodes)
        {
            distanceNodes.put((Integer) node.value(), Integer.MAX_VALUE);
            pathNodes.put((Integer) node.value(), new LinkedList<>());
        }

        // Initialise distance of source with 0
        distanceNodes.replace(start, 0);

        // Initialise unsettled nodes with source node
        this.unsettledNodes.add(start);
    }

    private Map<Integer, Integer> calculateShortestPathFromSource()
    {
        // Loop until no more nodes to evaluate
        while (unsettledNodes.size() != 0)
        {
            int currentNode = getLowestDistanceNode();
            unsettledNodes.remove(currentNode);

            // Calculate minimum distances from each connected node
            Collection<Edge<Integer, Transport>> adjacentEdges;
            adjacentEdges = view.getGraph().getEdgesFrom(view.getGraph().getNode(currentNode));

            for (Edge<Integer, Transport> edge : adjacentEdges)
            {
                // Evaluate the adjacent node if not yet settled
                int adjacentNode = edge.destination().value();
                int edgeWeight = weightFromTransport(edge.data());
                if (!settledNodes.contains(adjacentNode))
                {
                    calculateMinDistance(adjacentNode, edgeWeight, currentNode, edge);
                    unsettledNodes.add(adjacentNode);
                }
            }
            // Current node now evaluated, add to settledNodes list
            settledNodes.add(currentNode);
        }
        return distanceNodes;
    }

    private int getLowestDistanceNode()
    {
        int lowestDistance = Integer.MAX_VALUE;
        int lowestDistanceNode = 0;

        // Find the next node which is closest to any of the unsettled nodes
        for (Integer node : unsettledNodes)
        {
            int nodeDistance = distanceNodes.get(node);
            if (nodeDistance < lowestDistance)
            {
                lowestDistance = nodeDistance;
                lowestDistanceNode = node;
            }
        }
        return lowestDistanceNode;
    }

    private void calculateMinDistance(int adjacentNode, int edgeWeight, int currentNode, Edge<Integer, Transport> edge)
    {
        // Check if for this adjacent node, the distance from source + the edge weight is less than the current
        // distance to get to this adjacent node
        int sourceDistance = distanceNodes.get(currentNode);
        if ((sourceDistance + edgeWeight) < distanceNodes.get(adjacentNode))
        {
            // Change distance of node from source to include edge weight
            distanceNodes.replace(adjacentNode, (sourceDistance + edgeWeight));
            // Add to list of edges from source for this node
            LinkedList<Edge<Integer, Transport>> shortestPath = new LinkedList<>(pathNodes.get(currentNode));
            shortestPath.add(edge);
            pathNodes.replace(adjacentNode, shortestPath);
        }
    }

    // Get a weighted distance from the location passed as the argument
    public int getDistanceFromDetective(int detectiveLocation)
    {
        return calculateShortestPathFromSource().get(detectiveLocation);
    }

    // Get a list of edges that show the shortest route from the source location to the location given
    public LinkedList<Edge <Integer, Transport>> getPathFromDetective(int detectiveLocation)
    {
        calculateShortestPathFromSource();
        return pathNodes.get(detectiveLocation);
    }


    private int weightFromTransport(Transport transport)
    {
        // Assign edge weighting depending on transport required
        if (transport.equals(Transport.Taxi))
            return 1;
        else if (transport.equals(Bus))
            return 2;
        else
            return 3;
    }
}
