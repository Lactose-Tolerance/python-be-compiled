package compiler.lexer;

import compiler.util.Token;

public class LexerWrapper {
    private final Lexer lexer;

    public LexerWrapper(String source){
        lexer = new Lexer(source);
    }

    public Token getNextToken(){
        return lexer.getNextToken();
    }
}
