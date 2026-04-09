package compiler.util.ast;

import java.util.List;

public class GenericNode extends ASTNode {
    private final String ruleName;
    private final List<ASTNode> children;

    public GenericNode(String ruleName, List<ASTNode> children) {
        this.ruleName = ruleName;
        this.children = children;
    }

    public List<ASTNode> getChildren() { return children; }

    @Override
    public String printTree(int indentLevel) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indentLevel)).append(ruleName).append("\n");
        for (ASTNode child : children) {
            sb.append(child.printTree(indentLevel + 1));
        }
        return sb.toString();
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}