package ub.cse.algo;

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
        Arrays.fill(priors, -1);
        // Run BFS, finding the nodes parent in the shortest path
        Queue<Integer> searchQueue = new LinkedList<>();
        searchQueue.add(graph.contentProvider);
        while (!searchQueue.isEmpty()) {
            int node = searchQueue.poll();
            ArrayList<Integer> bandwidthsSorted = graph.get(node);
            // Sort neighbors by bandwidth in descending to reduce the chance of congestion and packet delay.
            bandwidthsSorted.sort((bw1, bw2) -> Integer.compare(this.bandwidths.get(bw2), this.bandwidths.get(bw1)));
            for (int neighbor : bandwidthsSorted) {
                // search through neighbors and find the one with the largest bandwidth.
                if (priors[neighbor] == -1 && neighbor != graph.contentProvider) {
                    priors[neighbor] = node;
                    searchQueue.add(neighbor);
                }
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
