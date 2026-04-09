package compiler.util.ast.nodes;

import compiler.util.ast.ASTNode;
import compiler.util.ast.ASTVisitor;
import java.util.List;

public class CompOpNode extends ASTNode {
    public final List<ASTNode> children;

    public CompOpNode(List<ASTNode> children) {
        this.children = children;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String printTree(int indentLevel) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indentLevel)).append("CompOpNode").append("\n");
        for (ASTNode child : children) {
            sb.append(child.printTree(indentLevel + 1));
        }
        return sb.toString();
    }
}
