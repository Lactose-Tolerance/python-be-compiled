package compiler.parser.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import compiler.parser.analysis.GrammarAnalyzer;
import compiler.parser.grammar.Grammar;
import compiler.parser.grammar.LR1Item;
import compiler.parser.grammar.NonTerminal;
import compiler.parser.grammar.Production;
import compiler.parser.grammar.Symbol;
import compiler.parser.grammar.Terminal;
import compiler.util.token.TokenType;

public class CLRTableGenerator {
    private final Grammar grammar;
    private final GrammarAnalyzer analyzer;
    private final ParsingTable table;
    
    private final Map<Set<LR1Item>, Integer> stateMap;
    private final List<Set<LR1Item>> states;

    public CLRTableGenerator(Grammar grammar) {
        this.grammar = grammar;
        this.analyzer = new GrammarAnalyzer(grammar);
        this.table = new ParsingTable();
        this.stateMap = new HashMap<>();
        this.states = new ArrayList<>();
    }

    public ParsingTable generate() {
        Terminal eof = new Terminal(TokenType.EOF);
        Production startProd = new Production(grammar.getAugmentedStart(), List.of(grammar.getStartSymbol()));
        LR1Item startItem = new LR1Item(startProd, 0, eof);

        Set<LR1Item> startState = closure(Set.of(startItem));
        states.add(startState);
        stateMap.put(startState, 0);

        Queue<Set<LR1Item>> queue = new LinkedList<>();
        queue.add(startState);

        while (!queue.isEmpty()) {
            Set<LR1Item> currentState = queue.poll();
            int stateIndex = stateMap.get(currentState);

            // 1. Compute Goto transitions
            for (Symbol X : analyzer.getAllSymbols()) {
                Set<LR1Item> nextState = gotoSet(currentState, X);
                if (!nextState.isEmpty()) {
                    int nextStateIndex = registerState(nextState, queue);

                    switch (X) {
                        case Terminal t -> table.setAction(stateIndex, t, Action.shift(nextStateIndex));
                        case NonTerminal nt -> table.setGoto(stateIndex, nt, nextStateIndex);
                        default -> {
                        }
                    }
                }
            }

            // 2. Compute Reductions & Accepts
            for (LR1Item item : currentState) {
                if (item.isComplete()) {
                    if (item.getProduction().getLeftHandSide().equals(grammar.getAugmentedStart())) {
                        table.setAction(stateIndex, item.getLookahead(), Action.accept());
                    } else {
                        handleReduceAction(stateIndex, item);
                    }
                }
            }
        }
        return table;
    }

    private int registerState(Set<LR1Item> nextState, Queue<Set<LR1Item>> queue) {
        if (!stateMap.containsKey(nextState)) {
            int nextStateIndex = states.size();
            states.add(nextState);
            stateMap.put(nextState, nextStateIndex);
            queue.add(nextState);
            return nextStateIndex;
        }
        return stateMap.get(nextState);
    }

    private void handleReduceAction(int stateIndex, LR1Item item) {
        Action existing = table.getAction(stateIndex, item.getLookahead());
        if (existing.getType() == Action.Type.SHIFT) {
            System.err.println("Warning: Shift/Reduce conflict in state " + stateIndex + " on " + item.getLookahead());
        } else if (existing.getType() == Action.Type.REDUCE) {
            System.err.println("Warning: Reduce/Reduce conflict in state " + stateIndex + " on " + item.getLookahead());
        }
        table.setAction(stateIndex, item.getLookahead(), Action.reduce(item.getProduction()));
    }

    private Set<LR1Item> closure(Set<LR1Item> items) {
        Set<LR1Item> closureSet = new HashSet<>(items);
        Queue<LR1Item> queue = new LinkedList<>(items);

        while (!queue.isEmpty()) {
            LR1Item item = queue.poll();
            Symbol nextSym = item.getSymbolAfterDot();

            if (nextSym instanceof NonTerminal nt) {
                List<Symbol> rhs = item.getProduction().getRightHandSide();
                List<Symbol> beta = new ArrayList<>();
                if (item.getDotPosition() + 1 < rhs.size()) {
                    beta.addAll(rhs.subList(item.getDotPosition() + 1, rhs.size()));
                }

                Set<Terminal> lookaheads = analyzer.firstOfSequence(beta, item.getLookahead());

                for (Production prod : grammar.getProductionsFor(nt)) {
                    for (Terminal b : lookaheads) {
                        LR1Item newItem = new LR1Item(prod, 0, b);
                        if (closureSet.add(newItem)) {
                            queue.add(newItem);
                        }
                    }
                }
            }
        }
        return closureSet;
    }

    private Set<LR1Item> gotoSet(Set<LR1Item> items, Symbol X) {
        Set<LR1Item> j = new HashSet<>();
        for (LR1Item item : items) {
            Symbol nextSym = item.getSymbolAfterDot();
            if (nextSym != null && nextSym.equals(X)) {
                j.add(new LR1Item(item.getProduction(), item.getDotPosition() + 1, item.getLookahead()));
            }
        }
        return closure(j);
    }
}