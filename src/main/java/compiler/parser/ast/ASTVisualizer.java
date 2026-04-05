package compiler.parser.ast;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public class ASTVisualizer {

    private static int idCounter = 0;

    /**
     * Generates an interactive HTML file visualizing the Abstract Syntax Tree.
     */
    public static void generateHTML(ASTNode root, String outputFileName) {
        StringBuilder nodesList = new StringBuilder("[\n");
        StringBuilder edgesList = new StringBuilder("[\n");

        idCounter = 0;
        // Start the root node at level 0
        traverseAndBuild(root, -1, 0, nodesList, edgesList);

        nodesList.append("]");
        edgesList.append("]");

        writeHtmlFile(outputFileName, nodesList.toString(), edgesList.toString());
    }

    private static void traverseAndBuild(ASTNode node, int parentId, int depth, StringBuilder nodes, StringBuilder edges) {
        if (node == null) return;
        
        int currentId = idCounter++;
        String label;
        String color;
        String shape = "box";

        // Style differently based on whether it's a leaf (Terminal) or a branch (Rule)
        if (node instanceof TerminalNode t) {
            // Escape special characters so the JavaScript doesn't break
            String escapedLexeme = t.getToken().lexeme()
                    .replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\\\n")
                    .replace("\r", "");
                    
            label = t.getToken().type() + "\\n" + escapedLexeme;
            color = "#FFE0B2"; // Light Orange for Terminals
            shape = "ellipse";
        } else {
            // Drop "Node" from the class name for a cleaner label (e.g., "IfStmtNode" -> "IfStmt")
            label = node.getClass().getSimpleName().replace("Node", "");
            color = "#C8E6C9"; // Light Green for Non-Terminals
        }

        // Add the 'level' property mapped to our calculated depth
        nodes.append(String.format("  { id: %d, label: '%s', shape: '%s', color: '%s', level: %d },\n",
                currentId, label, shape, color, depth));

        if (parentId != -1) {
            edges.append(String.format("  { from: %d, to: %d, arrows: 'to' },\n", parentId, currentId));
        }

        // Recursively visit all children, incrementing the depth by 1
        for (ASTNode child : getChildrenOf(node)) {
            traverseAndBuild(child, currentId, depth + 1, nodes, edges);
        }
    }

    /**
     * Uses Reflection to safely grab the `children` list from dynamically generated nodes.
     */
    @SuppressWarnings("unchecked")
    private static List<ASTNode> getChildrenOf(ASTNode node) {
        if (node == null) return List.of();
        if (node instanceof TerminalNode) return List.of();
        if (node instanceof GenericNode gn) return gn.getChildren();
        
        try {
            Field field = node.getClass().getField("children");
            return (List<ASTNode>) field.get(node);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            return List.of(); // Safely return empty if no children exist
        }
    }

    private static void writeHtmlFile(String filename, String nodes, String edges) {
        String htmlTemplate = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Abstract Syntax Tree Visualization</title>
                <script type="text/javascript" src="https://unpkg.com/vis-network/standalone/umd/vis-network.min.js"></script>
                <style type="text/css">
                    #mynetwork {
                        width: 100vw;
                        height: 100vh;
                        border: 1px solid lightgray;
                        background-color: #f8f9fa;
                    }
                    body { margin: 0; padding: 0; font-family: 'Segoe UI', Arial, sans-serif; }
                    #info { position: absolute; top: 15px; left: 15px; background: white; padding: 15px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); z-index: 10; }
                </style>
            </head>
            <body>
            <div id="info">
                <h3 style="margin-top:0;">AST Legend</h3>
                <span style="color:#C8E6C9;">&#11044;</span> Grammar Rule (Non-Terminal)<br>
                <span style="color:#FFE0B2;">&#11044;</span> Token (Terminal)<br>
                <hr>
                <i>Scroll to zoom. Drag to pan.</i>
            </div>
            <div id="mynetwork"></div>
            <script type="text/javascript">
                var nodes = new vis.DataSet(%s);
                var edges = new vis.DataSet(%s);
                var container = document.getElementById('mynetwork');
                var data = { nodes: nodes, edges: edges };
                var options = {
                    layout: {
                        hierarchical: {
                            direction: 'UD',        // Up-Down hierarchy
                            sortMethod: 'directed', // Follows the edges
                            levelSeparation: 80,    // Vertical spacing
                            nodeSpacing: 150        // Horizontal spacing
                        }
                    },
                    physics: {
                        enabled: false // Disable physics so the tree stays rigid and organized
                    },
                    edges: {
                        smooth: {
                            type: 'cubicBezier',
                            forceDirection: 'vertical',
                            roundness: 0.4
                        }
                    }
                };
                var network = new vis.Network(container, data, options);
            </script>
            </body>
            </html>
            """;

        try (FileWriter file = new FileWriter(filename)) {
            file.write(String.format(htmlTemplate, nodes, edges));
            System.out.println("AST visualization created successfully at: " + filename);
        } catch (IOException e) {
            System.err.println("Failed to write AST visualization file: " + e.getMessage());
        }
    }
}