package compiler.parser.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import compiler.parser.grammar.Grammar;
import compiler.parser.grammar.NonTerminal;
import compiler.parser.grammar.Production;
import compiler.parser.grammar.Symbol;
import compiler.parser.grammar.Terminal;

public class GrammarAnalyzer {
    private final Grammar grammar;
    private final Map<NonTerminal, Set<Terminal>> firstSets;
    private final Map<NonTerminal, Boolean> nullable;
    private final Set<Symbol> allSymbols;

    public GrammarAnalyzer(Grammar grammar) {
        this.grammar = grammar;
        this.firstSets = new HashMap<>();
        this.nullable = new HashMap<>();
        this.allSymbols = new HashSet<>();

        extractAllSymbols();
        computeFirstSets();
    }

    private void extractAllSymbols() {
        allSymbols.add(grammar.getStartSymbol());
        for (Production prod : grammar.getProductions()) {
            allSymbols.add(prod.getLeftHandSide());
            allSymbols.addAll(prod.getRightHandSide());
        }
    }

    private void computeFirstSets() {
        for (Symbol sym : allSymbols) {
            if (sym instanceof NonTerminal nt) {
                firstSets.put(nt, new HashSet<>());
                nullable.put(nt, false);
            }
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (Production prod : grammar.getProductions()) {
                NonTerminal lhs = prod.getLeftHandSide();
                List<Symbol> rhs = prod.getRightHandSide();

                // Check nullability
                boolean allNullable = true;
                for (Symbol sym : rhs) {
                    if (sym instanceof Terminal || !nullable.get((NonTerminal) sym)) {
                        allNullable = false;
                        break;
                    }
                }
                if (allNullable && !nullable.get(lhs)) {
                    nullable.put(lhs, true);
                    changed = true;
                }

                // Compute FIRST
                Set<Terminal> currentFirst = firstSets.get(lhs);
                int oldSize = currentFirst.size();

                for (Symbol sym : rhs) {
                    if (sym instanceof Terminal t) {
                        currentFirst.add(t);
                        break;
                    } else if (sym instanceof NonTerminal nt) {
                        currentFirst.addAll(firstSets.get(nt));
                        if (!nullable.get(nt)) {
                            break;
                        }
                    }
                }
                if (currentFirst.size() > oldSize) {
                    changed = true;
                }
            }
        }
    }

    public Set<Terminal> firstOfSequence(List<Symbol> sequence, Terminal lookahead) {
        Set<Terminal> result = new HashSet<>();
        boolean allNullable = true;

        for (Symbol sym : sequence) {
            if (sym instanceof Terminal t) {
                result.add(t);
                allNullable = false;
                break;
            } else if (sym instanceof NonTerminal nt) {
                result.addAll(firstSets.get(nt));
                if (!nullable.get(nt)) {
                    allNullable = false;
                    break;
                }
            }
        }

        if (allNullable) {
            result.add(lookahead);
        }
        return result;
    }

    public Set<Symbol> getAllSymbols() {
        return allSymbols;
    }
}