package compiler.util.ast;

public abstract class ASTNode {
    public abstract String printTree(int indentLevel);
    
    protected String getIndent(int level) {
        return "  ".repeat(level);
    }
}