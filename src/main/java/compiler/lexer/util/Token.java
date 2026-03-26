package compiler.lexer.util;

public record Token(TokenType type, String value, int line, int column) {
    @Override
    public String toString() {
        return String.format("[%s '%s' at %d:%d]", type, value, line, column);
    }
}