package compiler.parser;

import java.util.Stack;

import compiler.lexer.LexerAPI;
import compiler.parser.grammar.Production;
import compiler.parser.grammar.Terminal;
import compiler.parser.table.Action;
import compiler.parser.table.ParsingTable;
import compiler.util.Token;

public class CLRParser {
    private final ParsingTable table;

    public CLRParser(ParsingTable table) {
        this.table = table;
    }

    /**
     * Executes the Shift-Reduce parsing algorithm against a token stream.
     * @param lexer The Lexer API providing tokens
     * @return true if successful, throws RuntimeException on failure.
     */
    public boolean parse(LexerAPI lexer) {
        Stack<Integer> stateStack = new Stack<>();
        stateStack.push(0);

        Token currentToken = lexer.getNextToken();

        while (true) {
            int currentState = stateStack.peek();
            Terminal currentTerminal = new Terminal(currentToken.type());

            Action action = table.getAction(currentState, currentTerminal);

            if (null == action.getType()) {
                throw new RuntimeException(
                        "Syntax Error: Unexpected token " + currentToken +
                                " at Line: " + currentToken.line() + ", Col: " + currentToken.column()
                );
            } else switch (action.getType()) {
                case SHIFT -> {
                    stateStack.push(action.getTargetState());
                    currentToken = lexer.getNextToken();
                }
                case REDUCE -> {
                    Production prod = action.getRule();
                    int rhsSize = prod.getRightHandSide().size();
                    // Pop RHS symbols from the stack
                    for (int i = 0; i < rhsSize; i++) {
                        stateStack.pop();
                    }   int nextState = stateStack.peek();
                    Integer gotoState = table.getGoto(nextState, prod.getLeftHandSide());
                    if (gotoState == null) {
                        throw new RuntimeException("Syntax Error: Missing goto transition for non-terminal "
                                + prod.getLeftHandSide() + " from state " + nextState);
                    }   stateStack.push(gotoState);
                }
                case ACCEPT -> {
                    System.out.println("Parse successful!");
                    return true;
                }
                default -> throw new RuntimeException(
                            "Syntax Error: Unexpected token " + currentToken +
                                    " at Line: " + currentToken.line() + ", Col: " + currentToken.column()
                    );
            }
        }
    }
}