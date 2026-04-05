package compiler.lexer.extractors;

import compiler.lexer.util.CharReader;
import compiler.util.Token;
import compiler.util.TokenType;

public class CommentExtractor implements TokenExtractor {

    @Override
    public boolean canHandle(char c) {
        return c == '#';
    }

    @Override
    public Token extract(CharReader reader, int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        
        while (!reader.isAtEnd() && reader.peek() != '\n') {
            sb.append(reader.advance());
        }
        
        return new Token(TokenType.COMMENT, sb.toString(), startLine, startCol);
    }
}