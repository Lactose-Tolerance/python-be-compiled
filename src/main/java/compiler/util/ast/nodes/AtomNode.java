package compiler.util.ast.nodes;

import compiler.util.ast.ASTNode;
import java.util.List;

public class AtomNode extends ASTNode {
    public final List<ASTNode> children;

    public AtomNode(List<ASTNode> children) {
        this.children = children;
    }

    @Override
    public String printTree(int indentLevel) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indentLevel)).append("AtomNode").append("\n");
        for (ASTNode child : children) {
            sb.append(child.printTree(indentLevel + 1));
        }
        return sb.toString();
    }
}
