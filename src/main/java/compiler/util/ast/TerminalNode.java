package compiler.util.ast;

import compiler.util.Token;

public class TerminalNode extends ASTNode {
    private final Token token;

    public TerminalNode(Token token) {
        this.token = token;
    }

    public Token getToken() { return token; }

    @Override
    public String printTree(int indentLevel) {
        return getIndent(indentLevel) + "Terminal(" + token.type() + ": '" + token.lexeme() + "')\n";
    }
}