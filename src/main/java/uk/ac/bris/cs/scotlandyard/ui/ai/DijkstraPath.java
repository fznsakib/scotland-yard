package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import java.util.*;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.scotlandyard.model.*;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Bus;

public class DijkstraPath {

    private Set<Map<Integer, LinkedList<Map<Integer, Integer>>>> shortestPathNodes = new HashSet<>();
    private Set<Map<Integer, Integer>> distanceNodes = new HashSet<>();

    private Set<Map<Integer, Integer>> unsettledNodes = new HashSet<>();
    private Set<Map<Integer, Integer>> settledNodes = new HashSet<>();

    public DijkstraPath(int start, Graph graph) {

        // Grab all nodes from current graph, and convert to nodes holding distance from source
        List<Node> allNodes = graph.getNodes();
        Map<Integer, Integer> data = new HashMap<>();

        for (Node node : allNodes)
        {
            if (node.value().equals(start))
            {
                data.put((Integer) node.value(), 0);
                distanceNodes.add(data);
            }
            data.put((Integer) node.value(), Integer.MAX_VALUE);
            distanceNodes.add(data);
        }

        // Initialise unsettled nodes with source node
        this.unsettledNodes.add(findNode(start));
    }

    public Set<Map<Integer, LinkedList<Map<Integer, Integer>>>> calculateShortestPathFromSource(Graph graph)
    {
        while (unsettledNodes.size() != 0)
        {
            Map<Integer, Integer> currentNode = getLowestDistanceNode(unsettledNodes);
            unsettledNodes.remove(currentNode);

            Collection<Edge<Integer, Transport>> adjacentEdges = graph.getEdgesFrom(graph.getNode(currentNode.keySet().toArray()[0]));

            for (Edge<Integer, Transport> edge : adjacentEdges)
            {
                Map<Integer, Integer> adjacentNode = findNode(edge.destination().value());
                Integer edgeWeight = weightFromTransport(edge.data());
                if (!keysFrom(settledNodes).contains(edge.destination().value()))
                {
                    calculateMinDistance(adjacentNode, edgeWeight, currentNode);
                    unsettledNodes.add(adjacentNode);
                }
            }
            settledNodes.add(currentNode);
        }
        return shortestPathNodes;
    }

    private Map<Integer, Integer> getLowestDistanceNode(Set<Map<Integer, Integer>> unsettledNodes)
    {
        int lowestDistance = Integer.MAX_VALUE;
        Map<Integer, Integer> lowestDistanceNode = null;
        for (Map<Integer, Integer> node : unsettledNodes)
        {
            int nodeDistance = (Integer) node.values().toArray()[0];
            if (nodeDistance < lowestDistance)
            {
                lowestDistance = nodeDistance;
                lowestDistanceNode = node;
            }
        }
        return lowestDistanceNode;
    }

    private void calculateMinDistance(Map<Integer, Integer> evaluationNode, int edgeWeight, Map<Integer, Integer> currentNode)
    {
        int sourceDistance = (Integer) currentNode.values().toArray()[0];
        if (sourceDistance + edgeWeight < (Integer) evaluationNode.values().toArray()[0])
        {
            evaluationNode.remove((Integer) evaluationNode.keySet().toArray()[0]);
            evaluationNode.put((Integer) evaluationNode.keySet().toArray()[0], (sourceDistance + edgeWeight));
            LinkedList<Map<Integer, Integer>> shortestPath = new LinkedList<>(getShortestPath(currentNode));
            shortestPath.add(currentNode);
            removeShortestPathFromList(shortestPathNodes, evaluationNode);
            Map<Integer, LinkedList<Map< Integer, Integer >>> data = new HashMap<>();
            data.put((Integer) evaluationNode.values().toArray()[0], shortestPath);
            shortestPathNodes.add(data);
        }
    }

    private LinkedList<Map<Integer, Integer>> getShortestPath(Map<Integer, Integer> sourceNode)
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
