package compiler.util.ast.nodes;

import compiler.util.ast.ASTNode;
import java.util.List;

public class ProgramNode extends ASTNode {
    public final List<ASTNode> children;

    public ProgramNode(List<ASTNode> children) {
        this.children = children;
    }

    @Override
    public String printTree(int indentLevel) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indentLevel)).append("ProgramNode").append("\n");
        for (ASTNode child : children) {
            sb.append(child.printTree(indentLevel + 1));
        }
        return sb.toString();
    }
}
