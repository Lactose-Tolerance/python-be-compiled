package compiler.test;

import compiler.lexer.nfa.State;
import compiler.lexer.nfa.Transition;
import compiler.lexer.nfa.NFA;
import compiler.lexer.nfa.visualizer.NFAVisualizer;

public class TestVisualizer {
    public static void main(String[] args) {
        // Build a sample NFA that matches "ab*"
        State s0 = new State(false);
        State s1 = new State(true);
        
        // s0 to s1 on 'a'
        s0.addTransition(new Transition(c -> c == 'a', s1, "a"));
        
        // s1 loops on 'b'
        s1.addTransition(new Transition(c -> c == 'b', s1, "b"));
        
        NFA nfa = new NFA(s0);
        
        // Generate the graph!
        NFAVisualizer.generateHTML(nfa, "test_nfa_graph.html");
    }
}