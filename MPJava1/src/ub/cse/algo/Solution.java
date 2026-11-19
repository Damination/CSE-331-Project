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

    float score(SolutionObject sol){
        HashMap<Integer,Integer>  delays = Simulator.run(graph, clients, sol);
        return Revenue.revenue(info,sol, delays, true, true, !this.bandwidths.equals(sol.bandwidths));
    }

    int [] getLoad (HashMap<Integer, ArrayList<Integer>> paths){
        int [] load = new int[graph.size()];
        for(ArrayList<Integer> path : paths.values()){
            int idx = 1;
            while (idx < path.size() - 1) {
                load[path.get(idx)] += 1;
                idx += 1;
            }
        }
        return load;
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

    ArrayList<Integer> buyEverything(int[] load){
        ArrayList<Integer> newBandwidths = new ArrayList<>(this.bandwidths);
        for(int idx = 0; idx < load.length; idx ++){
            if(idx != graph.contentProvider && load[idx] > newBandwidths.get(idx)){
                newBandwidths.set(idx, load[idx]);
            }
        }
        return newBandwidths;
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

    float trim(SolutionObject sol, float bestScore) {
        float newScore;
        ArrayList<Integer> problemRouters = new ArrayList<>();

        // Pass 1: revert bought routers back to original
        for (int idx = 0; idx < sol.bandwidths.size(); idx++) {
            // Skip nodes we never bought units for (this also skips the ISP)
            if (sol.bandwidths.get(idx) <= this.bandwidths.get(idx)) {
                continue;
            }
            int oldBW = sol.bandwidths.get(idx);
            sol.bandwidths.set(idx, this.bandwidths.get(idx));
            newScore = score(sol);
            if (newScore < bestScore) {
                // this router needs extra units so put them back
                sol.bandwidths.set(idx, oldBW);
                problemRouters.add(idx);
            } else {
                // Same or better with fewer units, so keep the lower bandwidth
                bestScore = newScore;
            }
        }

        // Pass 2: binary search each problem router for the fewest units that keep the score
        for (int router : problemRouters) {
            int low = this.bandwidths.get(router) + 1;   // lowest value worth trying (original was too low)
            int high = sol.bandwidths.get(router);       // current value, known to work
            while (low < high) {
                int mid = (low + high) / 2;
                sol.bandwidths.set(router, mid);
                float midScore = score(sol);
                if (midScore >= bestScore) {
                    // mid still works, so the answer is mid or lower
                    bestScore = midScore;
                    high = mid;
                } else {
                    // mid is too low, so the answer is above it
                    low = mid + 1;
                }
            }
            // high is always the lowest value we know works
            sol.bandwidths.set(router, high);

        }
        return bestScore;
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
        int[] load = getLoad(sol.paths);
        ArrayList<Integer> bandwidthCopy = new ArrayList<>(this.bandwidths);
        ArrayList<Integer> bandwidthBuy = buyEverything(load);

        sol.bandwidths = bandwidthCopy;
        float origScore = score(sol);

        sol.bandwidths = bandwidthBuy;
        float newScore = score(sol);

        if (origScore > newScore){
            sol.bandwidths = bandwidthCopy;
        }
        else{
            sol.bandwidths = bandwidthBuy;
            trim(sol,newScore);
        }

        /* TODO:Your solution goes here */
        return sol;
    }
}
