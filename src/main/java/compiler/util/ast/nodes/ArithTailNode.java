package compiler.util.ast.nodes;

import compiler.util.ast.ASTNode;
import java.util.List;

public class ArithTailNode extends ASTNode {
    public final List<ASTNode> children;

    public ArithTailNode(List<ASTNode> children) {
        this.children = children;
    }

    @Override
    public String printTree(int indentLevel) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indentLevel)).append("ArithTailNode").append("\n");
        for (ASTNode child : children) {
            sb.append(child.printTree(indentLevel + 1));
        }
        return sb.toString();
    }
}
