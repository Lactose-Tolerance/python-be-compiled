package compiler.util.ast;

import compiler.util.ast.nodes.*;

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
}