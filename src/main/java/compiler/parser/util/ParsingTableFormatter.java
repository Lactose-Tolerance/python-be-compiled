package compiler.parser.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import compiler.parser.grammar.NonTerminal;
import compiler.parser.grammar.Production;
import compiler.parser.grammar.Terminal;
import compiler.parser.table.Action;
import compiler.parser.table.ParsingTable;

public class ParsingTableFormatter {

    public static void writeTableToFile(ParsingTable table, String filename) throws IOException {
        Set<Terminal> terminals = new TreeSet<>(Comparator.comparing(Terminal::getName));
        Set<NonTerminal> nonTerminals = new TreeSet<>(Comparator.comparing(NonTerminal::getName));
        int maxState = 0;

        // Extract all symbols and find max state
        for (Map.Entry<Integer, Map<Terminal, Action>> entry : table.getActionTable().entrySet()) {
            maxState = Math.max(maxState, entry.getKey());
            terminals.addAll(entry.getValue().keySet());
        }
        for (Map.Entry<Integer, Map<NonTerminal, Integer>> entry : table.getGotoTable().entrySet()) {
            maxState = Math.max(maxState, entry.getKey());
            nonTerminals.addAll(entry.getValue().keySet());
        }

        // Calculate dynamic column widths (minimum width is length of the symbol's name)
        Map<Terminal, Integer> termWidths = new HashMap<>();
        for (Terminal t : terminals) {
            termWidths.put(t, Math.max(t.getName().length(), getActionColWidth(table, maxState, t)));
        }

        Map<NonTerminal, Integer> ntWidths = new HashMap<>();
        for (NonTerminal nt : nonTerminals) {
            ntWidths.put(nt, Math.max(nt.getName().length(), 4));
        }

        int stateWidth = Math.max(5, String.valueOf(maxState).length());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // 1. Print Header (ACTION and GOTO regions)
            writer.write(padRight("STATE", stateWidth) + " | ");
            writer.write("ACTION" + " ".repeat(Math.max(0, getTotalWidth(termWidths) - 6)) + " | ");
            writer.write("GOTO\n");

            // 2. Print Symbols row
            writer.write(" ".repeat(stateWidth) + " | ");
            for (Terminal t : terminals) {
                writer.write(padRight(t.getName(), termWidths.get(t)) + " | ");
            }
            for (NonTerminal nt : nonTerminals) {
                writer.write(padRight(nt.getName(), ntWidths.get(nt)) + " | ");
            }
            writer.write("\n");

            // 3. Print Separator Line
            int totalLineLength = stateWidth + 3 + getTotalWidth(termWidths) + (terminals.size() * 3) + getTotalWidth(ntWidths) + (nonTerminals.size() * 3);
            writer.write("-".repeat(totalLineLength) + "\n");

            // 4. Print Rows
            for (int i = 0; i <= maxState; i++) {
                // Only print states that actually exist in the table
                if (!table.getActionTable().containsKey(i) && !table.getGotoTable().containsKey(i)) continue;

                writer.write(padRight(String.valueOf(i), stateWidth) + " | ");

                // Print Actions
                for (Terminal t : terminals) {
                    Action action = table.getAction(i, t);
                    writer.write(padRight(formatAction(action), termWidths.get(t)) + " | ");
                }

                // Print Gotos
                for (NonTerminal nt : nonTerminals) {
                    Integer gotoState = table.getGoto(i, nt);
                    String gotoStr = (gotoState != null) ? String.valueOf(gotoState) : "";
                    writer.write(padRight(gotoStr, ntWidths.get(nt)) + " | ");
                }
                writer.write("\n");
            }
        }
    }

    private static int getActionColWidth(ParsingTable table, int maxState, Terminal t) {
        int max = 0;
        for (int i = 0; i <= maxState; i++) {
            Action act = table.getAction(i, t);
            max = Math.max(max, formatAction(act).length());
        }
        return max;
    }

    private static String formatAction(Action action) {
        if (null != action.getType()) 
            switch (action.getType()) {
                case SHIFT -> {
                    return "s" + action.getTargetState();
                }
                case REDUCE -> {
                    Production p = action.getRule();
                    return "r(" + p.getLeftHandSide().getName() + "->" + (p.isEpsilon() ? "ε" : "...") + ")";
                }
                case ACCEPT -> {
                    return "acc";
                }
                default -> {
                }
            }
        return "";
    }

    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    private static int getTotalWidth(Map<?, Integer> widths) {
        return widths.values().stream().mapToInt(Integer::intValue).sum();
    }
}