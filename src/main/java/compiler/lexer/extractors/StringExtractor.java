package compiler.lexer.extractors;

import compiler.lexer.nfa.NFA;
import compiler.lexer.nfa.State;
import compiler.lexer.nfa.Transition;
import compiler.lexer.nfa.visualizer.NFAVisualizer;
import compiler.lexer.util.CharReader;
import compiler.util.Token;
import compiler.util.TokenType;

public class StringExtractor implements TokenExtractor {
    private final NFA nfa;

    public StringExtractor() {
        State strS0 = new State(false);
        State acceptS = new State(true);  // Successfully closed string
        
        // ==========================================
        // PATH 1: Double-Quoted Strings ("...")
        // ==========================================
        State doubleS1 = new State(false); // Inside double-quote body
        State doubleEscapeS = new State(false); // Escape character triggered
        
        // 1. Open Double Quote
        strS0.addTransition(new Transition(c -> c == '"', doubleS1, "\\\""));
        
        // 2. Normal characters (Anything except a double quote, backslash, AND newline)
        doubleS1.addTransition(new Transition(
            c -> c != '"' && c != '\\' && c != '\n' && c != '\r', 
            doubleS1, 
            "[^\\\"\\\\\\n\\r]"
        ));
        
        // 3. Escape Sequence Initiator (\)
        doubleS1.addTransition(new Transition(c -> c == '\\', doubleEscapeS, "\\\\"));
        
        // 4. Accept ANY character immediately following a backslash
        doubleEscapeS.addTransition(new Transition(c -> true, doubleS1, "any"));
        
        // 5. Close Double Quote
        doubleS1.addTransition(new Transition(c -> c == '"', acceptS, "\\\""));

        // ==========================================
        // PATH 2: Single-Quoted Strings ('...')
        // ==========================================
        State singleS1 = new State(false); // Inside single-quote body
        State singleEscapeS = new State(false); // Escape character triggered
        
        // 1. Open Single Quote
        strS0.addTransition(new Transition(c -> c == '\'', singleS1, "'"));
        
        // 2. Normal characters (Anything except a single quote, backslash, AND newline)
        singleS1.addTransition(new Transition(
            c -> c != '\'' && c != '\\' && c != '\n' && c != '\r', 
            singleS1, 
            "[^'\\\\\\n\\r]"
        ));
        
        // 3. Escape Sequence Initiator (\)
        singleS1.addTransition(new Transition(c -> c == '\\', singleEscapeS, "\\\\"));
        
        // 4. Accept ANY character immediately following a backslash
        singleEscapeS.addTransition(new Transition(c -> true, singleS1, "any"));
        
        // 5. Close Single Quote
        singleS1.addTransition(new Transition(c -> c == '\'', acceptS, "'"));
        
        // Build the combined NFA
        this.nfa = new NFA(strS0);
        NFAVisualizer.generateHTML(nfa, "string_nfa_graph.html");
    }

    @Override
    public boolean canHandle(char c) {
        return nfa.isAlive(String.valueOf(c));
    }

    @Override
    public Token extract(CharReader reader, int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        
        while (!reader.isAtEnd()) {
            sb.append(reader.peek());
            if (nfa.isAlive(sb.toString())) {
                reader.advance();
            } else {
                sb.deleteCharAt(sb.length() - 1);
                break;
            }
        }
        
        String text = sb.toString();
        if (!nfa.matches(text)) {
            // Graceful recovery for unterminated strings
            return new Token(TokenType.UNKNOWN, text, startLine, startCol);
        }
        
        return new Token(TokenType.STRING, text, startLine, startCol);
    }
}