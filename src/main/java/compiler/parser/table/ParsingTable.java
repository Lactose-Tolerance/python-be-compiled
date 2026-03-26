package compiler.parser.table;

import java.util.HashMap;
import java.util.Map;

import compiler.parser.grammar.NonTerminal;
import compiler.parser.grammar.Terminal;

public class ParsingTable {
    private final Map<Integer, Map<Terminal, Action>> actionTable = new HashMap<>();
    
    private final Map<Integer, Map<NonTerminal, Integer>> gotoTable = new HashMap<>();

    public void setAction(int state, Terminal terminal, Action action) {
        actionTable.computeIfAbsent(state, k -> new HashMap<>()).put(terminal, action);
    }

    public void setGoto(int state, NonTerminal nonTerminal, int targetState) {
        gotoTable.computeIfAbsent(state, k -> new HashMap<>()).put(nonTerminal, targetState);
    }

    public Action getAction(int state, Terminal terminal) {
        return actionTable.getOrDefault(state, new HashMap<>()).getOrDefault(terminal, Action.error());
    }

    public Integer getGoto(int state, NonTerminal nonTerminal) {
        return gotoTable.getOrDefault(state, new HashMap<>()).get(nonTerminal);
    }

    public Map<Integer, Map<Terminal, Action>> getActionTable() {
        return actionTable;
    }

    public Map<Integer, Map<NonTerminal, Integer>> getGotoTable() {
        return gotoTable;
    }
}