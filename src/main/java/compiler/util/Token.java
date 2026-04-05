package compiler.util;

public record Token(TokenType type, String lexeme, int line, int column) {
    @Override
    public String toString() {
        return String.format("Token{%s '%s' [Line: %d, Col: %d]}", type, lexeme, line, column);
    }
}