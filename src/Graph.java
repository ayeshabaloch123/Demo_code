import java.util.*;

public class Graph implements CITS2200Project {
    private Map<String, Integer> urlToId; // Mapping from URL to vertex ID
    private List<String> idToUrl; // Mapping from vertex ID to URL
    private List<List<Integer>> adjacencyList; // Adjacency list representation of the graph

    public Graph() {
        urlToId = new HashMap<>();
        idToUrl = new ArrayList<>();
        adjacencyList = new ArrayList<>();
    }

    public void addEdge(String urlFrom, String urlTo) {
        // Check if the vertices already exist in the graph
        int fromId = urlToId.getOrDefault(urlFrom, -1);
        int toId = urlToId.getOrDefault(urlTo, -1);

        // If the vertices do not exist, add them to the graph
        if (fromId == -1) {
            fromId = idToUrl.size(); // Assign a new ID
            urlToId.put(urlFrom, fromId);
            idToUrl.add(urlFrom);
            adjacencyList.add(new ArrayList<>());
        }
        if (toId == -1) {
            toId = idToUrl.size(); // Assign a new ID
            urlToId.put(urlTo, toId);
            idToUrl.add(urlTo);
            adjacencyList.add(new ArrayList<>());
        }

        // Add the edge between the vertices
        adjacencyList.get(fromId).add(toId);
    }

    public int getShortestPath(String urlFrom, String urlTo) {
        // Check if the vertices exist in the graph
        int fromId = urlToId.getOrDefault(urlFrom, -1);
        int toId = urlToId.getOrDefault(urlTo, -1);

        // If either vertex doesn't exist, return -1 (no path)
        if (fromId == -1 || toId == -1) {
            return -1;
        }

        // Perform Breadth First Search (BFS) to find the shortest path
        int[] distances = new int[idToUrl.size()]; // Array to store distances from the source
        boolean[] visited = new boolean[idToUrl.size()]; // Array to track visited vertices

        // Initialize distances array to -1 (indicating no path)
        for (int i = 0; i < distances.length; i++) {
            distances[i] = -1;
        }

        Queue<Integer> queue = new LinkedList<>();
        queue.offer(fromId); // Enqueue the source vertex
        distances[fromId] = 0; // Set the distance of the source vertex to 0
        visited[fromId] = true; // Mark the source vertex as visited

        while (!queue.isEmpty()) {
            int currentVertex = queue.poll(); // Dequeue a vertex from the queue

            // Iterate over the neighboring vertices
            for (int neighbor : adjacencyList.get(currentVertex)) {
                if (!visited[neighbor]) {
                    queue.offer(neighbor); // Enqueue the neighbor
                    visited[neighbor] = true; // Mark the neighbor as visited
                    distances[neighbor] = distances[currentVertex] + 1; // Update the distance
                }
            }
        }

        return distances[toId]; // Return the distance to the destination vertex
    }


    public String[] getCenters() {
        List<Integer> centers = new ArrayList<>(); // List to store center vertex IDs
        int minEccentricity = Integer.MAX_VALUE; // Variable to track minimum eccentricity

        for (int i = 0; i < idToUrl.size(); i++) {
            int eccentricity = getEccentricity(i); // Compute the eccentricity of the current vertex

            if (eccentricity < minEccentricity) {
                minEccentricity = eccentricity; // Update the minimum eccentricity
                centers.clear(); // Clear the previous center vertices
                centers.add(i); // Add the new center vertex
            } else if (eccentricity == minEccentricity) {
                centers.add(i); // Add the current vertex as another center vertex
            }
        }

        // Convert the center vertex IDs to corresponding URLs
        String[] centerUrls = new String[centers.size()];
        for (int i = 0; i < centers.size(); i++) {
            centerUrls[i] = idToUrl.get(centers.get(i));
        }

        return centerUrls;
    }


    private int getEccentricity(int sourceId) {
        int[] distances = new int[idToUrl.size()]; // Array to store distances from the source
        boolean[] visited = new boolean[idToUrl.size()]; // Array to track visited vertices

        // Initialize distances array to -1 (indicating no path)
        for (int i = 0; i < distances.length; i++) {
            distances[i] = -1;
        }

        Queue<Integer> queue = new LinkedList<>();
        queue.offer(sourceId); // Enqueue the source vertex
        distances[sourceId] = 0; // Set the distance of the source vertex to 0
        visited[sourceId] = true; // Mark the source vertex as visited

        while (!queue.isEmpty()) {
            int currentVertex = queue.poll(); // Dequeue a vertex from the queue

            // Iterate over the neighboring vertices
            for (int neighbor : adjacencyList.get(currentVertex)) {
                if (!visited[neighbor]) {
                    queue.offer(neighbor); // Enqueue the neighbor
                    visited[neighbor] = true; // Mark the neighbor as visited
                    distances[neighbor] = distances[currentVertex] + 1; // Update the distance
                }
            }
        }

        int maxDistance = 0;
        for (int distance : distances) {
            if (distance > maxDistance) {
                maxDistance = distance; // Update the maximum distance
            }
        }

        return maxDistance;
    }


    public String[][] getStronglyConnectedComponents() {
        List<List<Integer>> components = new ArrayList<>(); // List to store SCCs
        boolean[] visited = new boolean[idToUrl.size()]; // Array to track visited vertices
        Stack<Integer> stack = new Stack<>(); // Stack for DFS post-order traversal

        // Step 1: Perform DFS on the original graph to obtain post-order
        for (int i = 0; i < idToUrl.size(); i++) {
            if (!visited[i]) {
                dfs(i, visited, stack);
            }
        }

        // Step 2: Compute the transpose graph
        List<List<Integer>> transpose = transposeGraph();

        // Step 3: Perform DFS on the transpose graph based on post-order
        visited = new boolean[idToUrl.size()]; // Reset visited array

        while (!stack.isEmpty()) {
            int vertex = stack.pop();

            if (!visited[vertex]) {
                List<Integer> component = new ArrayList<>();
                dfsTranspose(vertex, visited, transpose, component);
                components.add(component);
            }
        }

        // Convert the components to arrays of URLs
        String[][] result = new String[components.size()][];
        for (int i = 0; i < components.size(); i++) {
            List<Integer> component = components.get(i);
            String[] urls = new String[component.size()];
            for (int j = 0; j < component.size(); j++) {
                urls[j] = idToUrl.get(component.get(j));
            }
            result[i] = urls;
        }

        return result;
    }

    private void dfs(int vertex, boolean[] visited, Stack<Integer> stack) {
        visited[vertex] = true;

        for (int neighbor : adjacencyList.get(vertex)) {
            if (!visited[neighbor]) {
                dfs(neighbor, visited, stack);
            }
        }

        stack.push(vertex);
    }

    private void dfsTranspose(int vertex, boolean[] visited, List<List<Integer>> transpose, List<Integer> component) {
        visited[vertex] = true;
        component.add(vertex);

        for (int neighbor : transpose.get(vertex)) {
            if (!visited[neighbor]) {
                dfsTranspose(neighbor, visited, transpose, component);
            }
        }
    }

    private List<List<Integer>> transposeGraph() {
        List<List<Integer>> transpose = new ArrayList<>();

        for (int i = 0; i < idToUrl.size(); i++) {
            transpose.add(new ArrayList<>());
        }

        for (int i = 0; i < idToUrl.size(); i++) {
            for (int neighbor : adjacencyList.get(i)) {
                transpose.get(neighbor).add(i);
            }
        }

        return transpose;
    }


    /**
     * Finds a Hamiltonian path in the page graph. There may be many
     * possible Hamiltonian paths. Any of these paths is a correct output.
     * This method should never be called on a graph with more than 20
     * vertices. If there is no Hamiltonian path, this method will
     * return an empty array. The output array should contain the URLs of pages
     * in a Hamiltonian path. The order matters, as the elements of the
     * array represent this path in sequence. So the element [0] is the start
     * of the path, and [1] is the next page, and so on.
     *
     * @return a Hamiltonian path of the page graph.
     */
    public String[] getHamiltonianPath() {
        List<String> path = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        // Iterate over each vertex as a starting point
        for (int vertexId = 0; vertexId < idToUrl.size(); vertexId++) {
            path.clear();
            visited.clear();

            // Start the DFS from the current vertex
            boolean foundPath = dfsHamiltonianPath(vertexId, path, visited);

            // If a Hamiltonian path is found, convert IDs to URLs and return it
            if (foundPath) {
                String[] hamiltonianPath = new String[path.size()];
                for (int i = 0; i < path.size(); i++) {
                    vertexId = Integer.parseInt(path.get(i));
                    hamiltonianPath[i] = idToUrl.get(vertexId);
                }
                return hamiltonianPath;
            }
        }

        // No Hamiltonian path found
        return new String[0];
    }

    private boolean dfsHamiltonianPath(int vertexId, List<String> path, Set<Integer> visited) {
        String currentVertex = Integer.toString(vertexId);

        // Mark the current vertex as visited
        visited.add(vertexId);
        path.add(currentVertex);

        // Base case: All vertices are visited
        if (visited.size() == idToUrl.size()) {
            return true;  // Hamiltonian path found
        }

        // Recursive case: Explore unvisited neighbors
        for (int neighborId : adjacencyList.get(vertexId)) {
            if (!visited.contains(neighborId)) {
                boolean foundPath = dfsHamiltonianPath(neighborId, path, visited);
                if (foundPath) {
                    return true;  // Hamiltonian path found
                }
            }
        }

        // Backtrack: Remove the current vertex from the path
        visited.remove(vertexId);
        path.remove(path.size() - 1);

        return false;  // No Hamiltonian path found
    }



    }


