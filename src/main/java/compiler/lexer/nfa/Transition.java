package compiler.lexer.nfa;

import java.util.function.Predicate;

public class Transition {
    public static final Predicate<Character> EPSILON = c -> c == '\0';

    public final Predicate<Character> condition;
    private final State targetState;
    private final String label;

    public Transition(Predicate<Character> condition, State targetState, String label) {
        this.condition = condition;
        this.targetState = targetState;
        this.label = label;
    }

    public Transition(State targetState) {
        this(EPSILON, targetState, "ε");
    }

    public boolean matches(char c) {
        return condition.test(c);
    }

    public State getTargetState() {
        return targetState;
    }

    public String getLabel() {
        return label;
    }
}