package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.*;
import java.util.*;
import uk.ac.bris.cs.scotlandyard.model.*;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Bus;

public class DijkstraPath {

    //private Map<Integer, LinkedList<Map<Integer, Integer>>> shortestPathNodes = new HashMap<>();
    private HashMap<Integer, Integer> distanceNodes = new HashMap<>();
    private Set<Integer> unsettledNodes = new HashSet<>();
    private Set<Integer> settledNodes = new HashSet<>();
    private HashMap<Integer, LinkedList<Edge<Integer, Transport>>> pathNodes = new HashMap<>();
    private ScotlandYardView view;

    public DijkstraPath(int start, ScotlandYardView view) {

        this.view = view;

        // Grab all nodes from current graph, and convert to nodes holding distance from source
        List<Node<Integer>> allNodes = view.getGraph().getNodes();


        for (Node node : allNodes)
        {
            distanceNodes.put((Integer) node.value(), Integer.MAX_VALUE);
            pathNodes.put((Integer) node.value(), new LinkedList<>());
        }
        //System.out.println(pathNodes);
        distanceNodes.replace(start, 0);

        // Initialise unsettled nodes with source node
        this.unsettledNodes.add(start);
    }

    private Map<Integer, Integer> calculateShortestPathFromSource()
    {
        while (unsettledNodes.size() != 0)
        {
            int currentNode = getLowestDistanceNode(unsettledNodes);
            unsettledNodes.remove(currentNode);

            Collection<Edge<Integer, Transport>> adjacentEdges;
            adjacentEdges = view.getGraph().getEdgesFrom(view.getGraph().getNode(currentNode));
            //System.out.println(adjacentEdges);

            for (Edge<Integer, Transport> edge : adjacentEdges)
            {
                int adjacentNode = edge.destination().value();
                int edgeWeight = weightFromTransport(edge.data());
                if (!settledNodes.contains(adjacentNode))
                {
                    calculateMinDistance(adjacentNode, edgeWeight, currentNode, edge);
                    unsettledNodes.add(adjacentNode);
                }
            }
            settledNodes.add(currentNode);
        }
        return distanceNodes;
    }

    private int getLowestDistanceNode(Set<Integer> unsettledNodes)
    {
        int lowestDistance = Integer.MAX_VALUE;
        int lowestDistanceNode = 0;
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
        int sourceDistance = distanceNodes.get(currentNode);
        if ((sourceDistance + edgeWeight) < distanceNodes.get(adjacentNode))
        {
            distanceNodes.replace(adjacentNode, (sourceDistance + edgeWeight));
            LinkedList<Edge<Integer, Transport>> shortestPath = new LinkedList<>(pathNodes.get(currentNode));
            shortestPath.add(edge);
            pathNodes.replace(adjacentNode, shortestPath);
        }
    }

    public int getDistanceFromDetective(int detectiveLocation)
    {
        return calculateShortestPathFromSource().get(detectiveLocation);
    }

    public LinkedList<Edge <Integer, Transport>> getPathFromDetective(int detectiveLocation)
    {
        calculateShortestPathFromSource();
        return pathNodes.get(detectiveLocation);
    }

    /*private LinkedList<Map<Integer, Integer>> getShortestPath(Map<Integer, Integer> sourceNode)
    {
        int sourceNodeValue = (Integer) sourceNode.keySet().toArray()[0];
        LinkedList<Map<Integer, Integer>> shortestPath = new LinkedList<>();
        for (Map<Integer, LinkedList<Map<Integer, Integer>>> nodeWithList : shortestPathNodes)
        {
            if (nodeWithList.containsKey(sourceNodeValue))
            {
                shortestPath = (LinkedList<Map<Integer, Integer>>) nodeWithList.values().toArray()[0];
            }
        }
        return shortestPath;
    }

    private void removeShortestPathFromList(Set<Map<Integer, LinkedList<Map<Integer, Integer>>>> shortestPathNodes, Map<Integer, Integer> evaluationNode)
    {
        int evaluationNodeValue = (Integer) evaluationNode.keySet().toArray()[0];
        for (Map<Integer, LinkedList<Map<Integer, Integer>>> nodeWithList : shortestPathNodes)
        {
            if (nodeWithList.containsKey(evaluationNodeValue))
            {
                shortestPathNodes.remove(nodeWithList);
            }
        }
    }

    private Set<Integer> keysFrom(Set<Map<Integer, Integer>> settledNodes)
    {
        Set<Integer> keys = new HashSet<>();
        for (Map<Integer, Integer> node : settledNodes)
        {
            keys.add((Integer) node.keySet().toArray()[0]);
        }
        return keys;
    }

    private Map<Integer, Integer> findNode(int nodeLocation)
    {
        for (Map<Integer, Integer> node : distanceNodes)
        {
            if (node.containsKey(nodeLocation))
            {
                return node;
            }
        }
        return null;
    }*/

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
