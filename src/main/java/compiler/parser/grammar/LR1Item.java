package compiler.parser.grammar;

import java.util.Objects;

public class LR1Item {
    private final Production production;
    private final int dotPosition;
    private final Terminal lookahead;

    public LR1Item(Production production, int dotPosition, Terminal lookahead) {
        this.production = production;
        this.dotPosition = dotPosition;
        this.lookahead = lookahead;
    }

    public Production getProduction() { return production; }
    public int getDotPosition() { return dotPosition; }
    public Terminal getLookahead() { return lookahead; }

    public boolean isComplete() {
        return dotPosition >= production.getRightHandSide().size();
    }

    public Symbol getSymbolAfterDot() {
        if (isComplete()) return null;
        return production.getRightHandSide().get(dotPosition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LR1Item lr1Item = (LR1Item) o;
        return dotPosition == lr1Item.dotPosition &&
               production.equals(lr1Item.production) &&
               lookahead.equals(lr1Item.lookahead);
    }

    @Override
    public int hashCode() {
        return Objects.hash(production, dotPosition, lookahead);
    }
}