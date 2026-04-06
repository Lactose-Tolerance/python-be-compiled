package compiler.parser.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import compiler.parser.grammar.Grammar;
import compiler.parser.grammar.NonTerminal;
import compiler.parser.grammar.Production;
import compiler.parser.grammar.Symbol;
import compiler.parser.grammar.Terminal;
import compiler.util.token.TokenType;

public class GrammarLoader {

    /**
     * Loads the grammar entirely from a single file.
     * - Terminals are automatically derived from the TokenType enum.
     * - Non-Terminals are dynamically inferred (any symbol not a Terminal is a Non-Terminal).
     * - The Start Symbol is the LHS of the very first valid production rule.
     */
    public static Grammar load(String grammarFile) throws IOException {
        Map<String, Terminal> terminals = loadTerminalsFromEnum();
        Map<String, NonTerminal> nonTerminals = new HashMap<>();
        Grammar grammar = null;

        Path path = Path.of(grammarFile);
        if (!Files.exists(path)) {
            throw new IOException("Grammar file not found: " + grammarFile);
        }

        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split("->");
            if (parts.length < 1 || parts.length > 2) {
                throw new IllegalArgumentException("Invalid production format (must contain '->'): " + line);
            }

            // Extract the Left-Hand Side (LHS)
            String lhsName = parts[0].trim();
            NonTerminal lhs = nonTerminals.computeIfAbsent(lhsName, NonTerminal::new);

            // The first LHS we encounter becomes the Start Symbol for our Grammar
            if (grammar == null) {
                grammar = new Grammar(lhs);
            }

            // Handle empty RHS (epsilon production where nothing follows '->')
            String rhsString = parts.length == 2 ? parts[1] : "";

            // Split multiple right-hand sides by '|'
            String[] rhsOptions = rhsString.split("\\|");

            for (String rhsOption : rhsOptions) {
                String[] rhsNames = rhsOption.trim().split("\\s+");
                List<Symbol> rhs = new ArrayList<>();

                // Check for Epsilon (empty string, "ε", or "epsilon")
                if (rhsNames.length == 1 && (rhsNames[0].isEmpty() || rhsNames[0].equals("ε") || rhsNames[0].equalsIgnoreCase("epsilon"))) {
                    // Epsilon production: leave the RHS list empty
                } else {
                    for (String symName : rhsNames) {
                        if (symName.isEmpty()) continue;

                        if (terminals.containsKey(symName)) {
                            // It's a known Terminal
                            rhs.add(terminals.get(symName));
                        } else {
                            // If it's not a Terminal, it must be a Non-Terminal
                            rhs.add(nonTerminals.computeIfAbsent(symName, NonTerminal::new));
                        }
                    }
                }

                // Add each variation as its own discrete production
                grammar.addProduction(new Production(lhs, rhs));
            }
        }

        if (grammar == null) {
            throw new RuntimeException("Grammar file is empty or contains no valid rules.");
        }

        return grammar;
    }

    /**
     * Automatically maps every TokenType defined in the enum to a Terminal object.
     */
    private static Map<String, Terminal> loadTerminalsFromEnum() {
        Map<String, Terminal> map = new HashMap<>();
        for (TokenType type : TokenType.values()) {
            map.put(type.name(), new Terminal(type));
        }
        return map;
    }
}