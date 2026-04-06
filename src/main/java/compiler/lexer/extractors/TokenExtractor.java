package compiler.lexer.extractors;

import compiler.lexer.util.CharReader;
import compiler.util.token.Token;

public interface TokenExtractor {
    boolean canHandle(char c);
    Token extract(CharReader reader, int startLine, int startCol);
}