package compiler.parser.ast.nodes;

import compiler.parser.ast.ASTNode;
import java.util.List;

public class TermNode extends ASTNode {
    public final List<ASTNode> children;

    public TermNode(List<ASTNode> children) {
        this.children = children;
    }

    @Override
    public String printTree(int indentLevel) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indentLevel)).append("TermNode").append("\n");
        for (ASTNode child : children) {
            sb.append(child.printTree(indentLevel + 1));
        }
        return sb.toString();
    }
}
