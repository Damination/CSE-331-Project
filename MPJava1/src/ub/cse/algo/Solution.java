package ub.cse.algo;

import java.lang.reflect.Array;
import java.util.*;

import static ub.cse.algo.Traversals.bfs;
import static ub.cse.algo.Traversals.bfsPaths;

public class Solution {

    private Info info;
    private Graph graph;
    private ArrayList<Client> clients;
    private ArrayList<Integer> bandwidths;

    /**
     * Basic Constructor
     *
     * @param info: data parsed from input file
     */
    public Solution(Info info) {
        this.info = info;
        this.graph = info.graph;
        this.clients = info.clients;
        this.bandwidths = info.bandwidths;
    }

    HashMap<Integer, ArrayList<Integer>> bfsPathsBandwidths(Graph graph, ArrayList<Client> clients) {
        /*
            Initialize the prior array with -1 for storing the node
            that is before the current one in the shortest paths
         */
        int[] priors = new int[graph.size()];
        int[] level = new int[graph.size()];
        Arrays.fill(level, -1);
        Arrays.fill(priors, -1);
        level[graph.contentProvider] = 0;
        // Run BFS, finding the nodes parent in the shortest path
        Queue<Integer> searchQueue = new LinkedList<>();
        searchQueue.add(graph.contentProvider);
        while (!searchQueue.isEmpty()) {
            int node = searchQueue.poll();
            for (int neighbor : graph.get(node)) {
                // Skip anything already found (this also skips the ISP and node's own parents)
                if (level[neighbor] != -1) {
                    continue;
                }
                // Neighbor is one level farther from the ISP than the current node
                level[neighbor] = level[node] + 1;
                // Neighbor's parents are its neighbors at the current node's level; keep the strongest
                int bestParent = node;
                for (int candidate : graph.get(neighbor)) {
                    if (level[candidate] == level[node]
                            && this.bandwidths.get(candidate) > this.bandwidths.get(bestParent)) {
                        bestParent = candidate;
                    }
                }
                priors[neighbor] = bestParent;
                searchQueue.add(neighbor);
            }
        }
        return pathsFromPriors(clients, priors);
    }
    private  HashMap<Integer, ArrayList<Integer>> pathsFromPriors(ArrayList<Client> clients, int[] priors) {
        HashMap<Integer, ArrayList<Integer>> paths = new HashMap<>(clients.size());
        // For every client, traverse the prior array, creating the path
        for (Client client : clients) {
            ArrayList<Integer> path = new ArrayList<>();
            int currentNode = client.id;
            while (currentNode != -1) {
                /*
                    Add this ID to the beginning of the
                    path so the path ends with the client
                 */
                path.add(0, currentNode);
                currentNode = priors[currentNode];
            }
            paths.put(client.id, path);
        }
        return paths;
    }

    /**
     * Method that returns the calculated
     * SolutionObject as found by your algorithm
     *.
     * @return SolutionObject containing the paths, priorities and bandwidths
     */
    public SolutionObject outputPaths() {
        SolutionObject sol = new SolutionObject();
        sol.paths = bfsPathsBandwidths(graph,clients);
        /* TODO:Your solution goes here */
        return sol;
    }
}
