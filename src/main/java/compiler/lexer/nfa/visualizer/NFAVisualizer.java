package compiler.lexer.nfa.visualizer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import compiler.lexer.nfa.NFA;
import compiler.lexer.nfa.State;
import compiler.lexer.nfa.Transition;

public class NFAVisualizer {

    /**
     * Generates an interactive HTML file visualizing the NFA.
     * @param nfa The NFA to visualize.
     * @param outputFileName The name of the HTML file (e.g., "nfa_graph.html").
     */
    public static void generateHTML(NFA nfa, String outputFileName) {
        Set<State> visited = new HashSet<>();
        Queue<State> queue = new LinkedList<>();

        State startState = nfa.getStartState();
        queue.add(startState);
        visited.add(startState);

        StringBuilder nodesList = new StringBuilder("[\n");
        StringBuilder edgesList = new StringBuilder("[\n");

        while (!queue.isEmpty()) {
            State current = queue.poll();
            
            // Format the Node (State)
            String shape = current.isAccepting() ? "doublecircle" : "circle";
            String color = current.isAccepting() ? "#90EE90" : "#ADD8E6"; // Green if accepting, Blue normally
            if (current == startState) {
                color = "#FFCCCB"; // Red/Pink for start state
            }

            nodesList.append(String.format("  { id: %d, label: 'S%d', shape: '%s', color: '%s' },\n",
                    current.getId(), current.getId(), shape, color));

            // Format the Edges (Transitions)
            for (Transition t : current.getTransitions()) {
                State target = t.getTargetState();
                
                // ESCAPE LOGIC ADDED HERE: Safely escape the label for JavaScript injection
                String escapedLabel = t.getLabel()
                        .replace("\\", "\\\\")
                        .replace("'", "\\\\\\'")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "");
                
                edgesList.append(String.format("  { from: %d, to: %d, label: '%s', arrows: 'to', font: {align: 'horizontal'} },\n",
                        current.getId(), target.getId(), escapedLabel));

                if (!visited.contains(target)) {
                    visited.add(target);
                    queue.add(target);
                }
            }
        }

        nodesList.append("]");
        edgesList.append("]");

        writeHtmlFile(outputFileName, nodesList.toString(), edgesList.toString());
    }

    private static void writeHtmlFile(String filename, String nodes, String edges) {
        String htmlTemplate = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>NFA Graph Visualization</title>
                <script type="text/javascript" src="https://unpkg.com/vis-network/standalone/umd/vis-network.min.js"></script>
                <style type="text/css">
                    #mynetwork {
                        width: 100vw;
                        height: 100vh;
                        border: 1px solid lightgray;
                        background-color: #f8f9fa;
                    }
                    body { margin: 0; padding: 0; font-family: Arial, sans-serif; }
                    #info { position: absolute; top: 10px; left: 10px; background: white; padding: 10px; border-radius: 5px; border: 1px solid #ccc; z-index: 10; }
                </style>
            </head>
            <body>
            <div id="info">
                <b>Legend:</b><br>
                <span style="color:#FFCCCB;">&#11044;</span> Start State<br>
                <span style="color:#ADD8E6;">&#11044;</span> Normal State<br>
                <span style="color:#90EE90;">&#11044;</span> Accepting State
            </div>
            <div id="mynetwork"></div>
            <script type="text/javascript">
                var nodes = new vis.DataSet(%s);
                var edges = new vis.DataSet(%s);
                var container = document.getElementById('mynetwork');
                var data = { nodes: nodes, edges: edges };
                var options = {
                    physics: {
                        stabilization: true,
                        barnesHut: { springLength: 150 }
                    },
                    edges: { smooth: { type: 'dynamic' } }
                };
                var network = new vis.Network(container, data, options);
            </script>
            </body>
            </html>
            """;

        try (FileWriter file = new FileWriter(filename)) {
            file.write(String.format(htmlTemplate, nodes, edges));
            System.out.println("Graph visualization created successfully at: " + filename);
        } catch (IOException e) {
            System.err.println("Failed to write visualization file: " + e.getMessage());
        }
    }
}