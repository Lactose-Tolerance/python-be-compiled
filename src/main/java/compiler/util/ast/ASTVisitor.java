package compiler.util.ast;

import compiler.util.ast.nodes.AndExprNode;
import compiler.util.ast.nodes.AndTailNode;
import compiler.util.ast.nodes.ArithExprNode;
import compiler.util.ast.nodes.ArithTailNode;
import compiler.util.ast.nodes.AssignmentNode;
import compiler.util.ast.nodes.AtomNode;
import compiler.util.ast.nodes.CompOpNode;
import compiler.util.ast.nodes.ElementTailNode;
import compiler.util.ast.nodes.ElementsNode;
import compiler.util.ast.nodes.ElifListNode;
import compiler.util.ast.nodes.ElsePartNode;
import compiler.util.ast.nodes.ExprNode;
import compiler.util.ast.nodes.ExprTailNode;
import compiler.util.ast.nodes.FactorNode;
import compiler.util.ast.nodes.IfStmtNode;
import compiler.util.ast.nodes.IndexExprNode;
import compiler.util.ast.nodes.InputExprNode;
import compiler.util.ast.nodes.ListNode;
import compiler.util.ast.nodes.NotExprNode;
import compiler.util.ast.nodes.PowerNode;
import compiler.util.ast.nodes.PrintStmtNode;
import compiler.util.ast.nodes.ProgramNode;
import compiler.util.ast.nodes.RelExprNode;
import compiler.util.ast.nodes.RelTailNode;
import compiler.util.ast.nodes.SimpleStmtNode;
import compiler.util.ast.nodes.StmtListNode;
import compiler.util.ast.nodes.StmtNode;
import compiler.util.ast.nodes.TermNode;
import compiler.util.ast.nodes.TermTailNode;

/**
 * The generic ASTVisitor interface.
 * The type parameter <T> allows different visitors to return different types.
 * For example, a SemanticAnalyzer might implement ASTVisitor<SpyType>,
 * while an Interpreter might implement ASTVisitor<Object>.
 */
public interface ASTVisitor<T> {
    
    // Core structural nodes
    T visit(TerminalNode node);
    T visit(GenericNode node);

    // Dynamically generated grammar nodes
    T visit(AndExprNode node);
    T visit(AndTailNode node);
    T visit(ArithExprNode node);
    T visit(ArithTailNode node);
    T visit(AssignmentNode node);
    T visit(AtomNode node);
    T visit(CompOpNode node);
    T visit(ElementsNode node);
    T visit(ElementTailNode node);
    T visit(ElifListNode node);
    T visit(ElsePartNode node);
    T visit(ExprNode node);
    T visit(ExprTailNode node);
    T visit(FactorNode node);
    T visit(IfStmtNode node);
    T visit(InputExprNode node);
    T visit(ListNode node);
    T visit(NotExprNode node);
    T visit(PowerNode node);
    T visit(PrintStmtNode node);
    T visit(ProgramNode node);
    T visit(RelExprNode node);
    T visit(RelTailNode node);
    T visit(SimpleStmtNode node);
    T visit(StmtListNode node);
    T visit(StmtNode node);
    T visit(TermNode node);
    T visit(TermTailNode node);
    T visit(IndexExprNode node);
}