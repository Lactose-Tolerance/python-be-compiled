package compiler.parser;

import java.util.Set;
import java.util.Stack;

import compiler.lexer.LexerAPI;
import compiler.parser.grammar.Production;
import compiler.parser.grammar.Terminal;
import compiler.parser.table.Action;
import compiler.parser.table.ParsingTable;
import compiler.util.Token;
import compiler.util.TokenType;
import compiler.util.ast.ASTBuilder;
import compiler.util.ast.visualizer.ASTVisualizer;

public class CLRParser {
    private final ParsingTable table;
    private final ASTBuilder astBuilder = new ASTBuilder();

    // Define synchronizing tokens that act as safe "landing zones" for recovery
    private static final Set<TokenType> SYNC_TOKENS = Set.of(
            TokenType.NEWLINE,
            TokenType.DEDENT,
            TokenType.EOF
    );

    public CLRParser(ParsingTable table) {
        this.table = table;
    }

    /**
     * Executes the Shift-Reduce parsing algorithm against a token stream.
     * @param lexer The Lexer API providing tokens
     * @return true if successful, false if errors occurred.
     */
    public boolean parse(LexerAPI lexer) {
        Stack<Integer> stateStack = new Stack<>();
        stateStack.push(0);

        Token currentToken = lexer.getNextToken();
        boolean hadError = false;

        while (true) {
            int currentState = stateStack.peek();
            Terminal currentTerminal = new Terminal(currentToken.type());

            Action action = table.getAction(currentState, currentTerminal);

            // ERROR DETECTION
            if (action == null || action.getType() == null || action.getType() == Action.Type.ERROR) {
                hadError = true;
                System.err.println("\nSyntax Error: Unexpected token\n" + currentToken);

                // TRIGGER PANIC MODE
                boolean recovered = recover(lexer, stateStack);
                
                if (!recovered) {
                    System.err.println("Fatal: Stack completely unwound. Could not recover.");
                    return false; // Abort completely if recovery fails
                }

                // If recovered, fetch the sync token we landed on and continue parsing!
                currentToken = lexer.peekToken();
                continue;
                
            } 
            
            // NORMAL PARSING EXECUTION
            switch (action.getType()) {
                case SHIFT -> {
                    stateStack.push(action.getTargetState());
                    astBuilder.shift(currentToken);
                    currentToken = lexer.getNextToken();
                }
                case REDUCE -> {
                    Production prod = action.getRule();
                    astBuilder.reduce(prod);
                    int rhsSize = prod.getRightHandSide().size();
                    
                    // Pop RHS symbols from the stack
                    for (int i = 0; i < rhsSize; i++) {
                        stateStack.pop();
                    }   
                    
                    int nextState = stateStack.peek();
                    Integer gotoState = table.getGoto(nextState, prod.getLeftHandSide());
                    
                    if (gotoState == null) {
                        throw new RuntimeException("Syntax Error: Missing goto transition for non-terminal "
                                + prod.getLeftHandSide() + " from state " + nextState);
                    }   
                    stateStack.push(gotoState);
                }
                case ACCEPT -> {
                    if (hadError) {
                        System.err.println("\nCompilation finished with syntax errors.");
                        return false;
                    } else {
                        System.out.println("Parse successful!");
                        ASTVisualizer.generateHTML(astBuilder.getRoot(), "ast_graph.html");
                        return true;
                    }
                }
                default -> throw new RuntimeException("Unknown parsing action state.");
            }
        }
    }

    /**
     * Executes Panic Mode Recovery.
     * 1. Discard tokens until a synchronization token is found.
     * 2. Pop states off the stack until a state is found that can shift the sync token.
     */
    private boolean recover(LexerAPI lexer, Stack<Integer> stateStack) {
        // Step 1: Fast-forward through the input stream until we hit a Sync Token
        Token syncToken = lexer.peekToken();
        while (!SYNC_TOKENS.contains(syncToken.type()) && syncToken.type() != TokenType.EOF) {
            lexer.getNextToken(); // consume and discard
            syncToken = lexer.peekToken();
        }

        if (syncToken.type() == TokenType.EOF && stateStack.isEmpty()) {
            return false;
        }

        // Step 2: Unwind the state stack until we find a state that can SHIFT our Sync Token
        Terminal syncTerminal = new Terminal(syncToken.type());
        
        while (!stateStack.isEmpty()) {
            int currentState = stateStack.peek();
            Action action = table.getAction(currentState, syncTerminal);

            // Can this state safely SHIFT the sync token?
            if (action != null && action.getType() == Action.Type.SHIFT) {
                return true;
            }
            
            // Otherwise, discard the state
            stateStack.pop();
        }

        return false; // Emptied the stack without finding a valid recovery state
    }
}