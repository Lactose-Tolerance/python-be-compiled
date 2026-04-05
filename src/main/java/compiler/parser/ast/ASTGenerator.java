package compiler.parser.ast;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ASTGenerator {
    private static final String GRAMMAR_FILE = "config/grammar.config";
    private static final String OUTPUT_DIR = "src/main/java/compiler/parser/ast/nodes/";

    public static void main(String[] args) {
        try {
            List<String> lines = Files.readAllLines(Path.of(GRAMMAR_FILE));
            Set<String> rules = new HashSet<>();

            // Extract all LHS rule names
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (line.contains("->")) {
                    rules.add(line.split("->")[0].trim());
                }
            }

            Files.createDirectories(Path.of(OUTPUT_DIR));

            for (String rule : rules) {
                generateNodeClass(rule);
            }

            System.out.println("Successfully generated " + rules.size() + " AST Node classes in " + OUTPUT_DIR);

        } catch (IOException e) {
            System.err.println("Failed to read grammar or write classes: " + e.getMessage());
        }
    }

    private static void generateNodeClass(String ruleName) throws IOException {
        String className = toPascalCase(ruleName) + "Node";
        Path outputPath = Path.of(OUTPUT_DIR + className + ".java");

        String classContent = """
            package compiler.parser.ast.nodes;
            
            import compiler.parser.ast.ASTNode;
            import java.util.List;
            
            public class %s extends ASTNode {
                public final List<ASTNode> children;
                
                public %s(List<ASTNode> children) {
                    this.children = children;
                }
                
                @Override
                public String printTree(int indentLevel) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(getIndent(indentLevel)).append("%s").append("\\n");
                    for (ASTNode child : children) {
                        sb.append(child.printTree(indentLevel + 1));
                    }
                    return sb.toString();
                }
            }
            """.formatted(className, className, className);

        Files.writeString(outputPath, classContent);
    }

    /**
     * Converts snake_case (e.g., "if_stmt") to PascalCase (e.g., "IfStmt").
     */
    public static String toPascalCase(String snake_case) {
        String[] parts = snake_case.split("_");
        StringBuilder camelCaseString = new StringBuilder();
        for (String part : parts) {
            if (part.length() > 0) {
                camelCaseString.append(part.substring(0, 1).toUpperCase());
                camelCaseString.append(part.substring(1).toLowerCase());
            }
        }
        return camelCaseString.toString();
    }
}