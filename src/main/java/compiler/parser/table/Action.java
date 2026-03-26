package compiler.parser.table;

import compiler.parser.grammar.Production;

public class Action {
    public enum Type { SHIFT, REDUCE, ACCEPT, ERROR }

    private final Type type;
    private final int targetState;
    private final Production rule;

    private Action(Type type, int targetState, Production rule) {
        this.type = type;
        this.targetState = targetState;
        this.rule = rule;
    }

    public static Action shift(int state) {
        return new Action(Type.SHIFT, state, null);
    }

    public static Action reduce(Production rule) {
        return new Action(Type.REDUCE, -1, rule);
    }

    public static Action accept() {
        return new Action(Type.ACCEPT, -1, null);
    }

    public static Action error() {
        return new Action(Type.ERROR, -1, null);
    }

    public Type getType() { return type; }
    public int getTargetState() { return targetState; }
    public Production getRule() { return rule; }
}