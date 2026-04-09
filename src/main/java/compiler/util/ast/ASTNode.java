package compiler.util.ast;

public abstract class ASTNode {
    public abstract String printTree(int indentLevel);

    public abstract <T> T accept(ASTVisitor<T> visitor);
    
    protected String getIndent(int level) {
        return "  ".repeat(level);
    }
}