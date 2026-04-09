package compiler.semantics;

import java.util.List;

import compiler.util.ast.ASTNode;
import compiler.util.ast.ASTVisitor;
import compiler.util.ast.GenericNode;
import compiler.util.ast.TerminalNode;
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
import compiler.util.symtab.SpyType;
import compiler.util.symtab.SymbolTable;
import compiler.util.token.Token;

public class SemanticAnalyzer implements ASTVisitor<SpyType> {
    private final SymbolTable symbolTable;

    public SemanticAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    // ==========================================================
    // HELPER METHODS (Type Coercion & Strict Mixing Rules)
    // ==========================================================

    private SpyType visitChildren(List<ASTNode> children) {
        SpyType lastType = SpyType.NONE;
        for (ASTNode child : children) {
            lastType = child.accept(this);
        }
        return lastType;
    }

    /**
     * Master router for binary operations (Arithmetic and Concatenation).
     * It strictly isolates Strings from Numeric types.
     */
    private SpyType resolveBinaryOperation(SpyType left, SpyType right) {
        // If an error already occurred downstream, bubble it up
        if (left == SpyType.ERROR || right == SpyType.ERROR) return SpyType.ERROR;

        // 1. STRICT STRING ISOLATION
        if (left == SpyType.STRING || right == SpyType.STRING) {
            return combineString(left, right);
        }

        // 2. STRICT NUMERIC ISOLATION
        return combineNumeric(left, right);
    }

    private SpyType combineString(SpyType left, SpyType right) {
        if (left == SpyType.STRING && right == SpyType.STRING) {
            return SpyType.STRING; // Allowed: "hello" + "world"
        }
        
        // Type Mixing Blocked
        System.err.println("TypeError: Cannot mix STRING with " + 
            (left == SpyType.STRING ? right : left) + " in operations.");
        return SpyType.ERROR;
    }

    private SpyType combineNumeric(SpyType left, SpyType right) {
        if (left == SpyType.FLOAT || right == SpyType.FLOAT) return SpyType.FLOAT;
        if (left == SpyType.INTEGER && right == SpyType.INTEGER) return SpyType.INTEGER;
        
        System.err.println("TypeError: Invalid numeric operation between " + left + " and " + right);
        return SpyType.ERROR;
    }

    private SpyType combineLogical(SpyType left, SpyType tail) {
        if (tail == SpyType.NONE) return left; // No logical operator was applied
        return SpyType.BOOLEAN;
    }


    // ==========================================================
    // TERMINALS & DYNAMIC SYMBOL LOOKUPS
    // ==========================================================

    @Override
    public SpyType visit(TerminalNode node) {
        Token t = node.getToken();
        return switch (t.type()) {
            case INTEGER -> SpyType.INTEGER;
            case FLOAT -> SpyType.FLOAT;
            case STRING -> SpyType.STRING;
            case BOOLEAN -> SpyType.BOOLEAN;
            case IDENTIFIER -> {
                SpyType type = symbolTable.getType(t.lexeme());
                if (type == SpyType.UNKNOWN) {
                    System.err.println("Variable '" + t.lexeme() + "' used before assignment at line " + t.line());
                }
                yield type;
            }
            default -> SpyType.NONE;
        };
    }


    // ==========================================================
    // ASSIGNMENTS (Dynamic Updating)
    // ==========================================================

    @Override
    public SpyType visit(AssignmentNode node) {
        // assignment -> IDENTIFIER ASSIGN expr
        TerminalNode idNode = (TerminalNode) node.children.get(0);
        String variableName = idNode.getToken().lexeme();

        SpyType rhsType = node.children.get(2).accept(this);

        symbolTable.updateType(variableName, rhsType);
        
        return SpyType.NONE;
    }


    // ==========================================================
    // ARITHMETIC EXPRESSIONS
    // ==========================================================

    @Override
    public SpyType visit(ArithExprNode node) {
        SpyType term = node.children.get(0).accept(this);
        SpyType tail = node.children.get(1).accept(this);
        return tail == SpyType.NONE ? term : resolveBinaryOperation(term, tail);
    }

    @Override
    public SpyType visit(ArithTailNode node) {
        if (node.children.isEmpty()) return SpyType.NONE;
        SpyType term = node.children.get(1).accept(this);
        SpyType tail = node.children.get(2).accept(this);
        return tail == SpyType.NONE ? term : resolveBinaryOperation(term, tail);
    }

    @Override
    public SpyType visit(TermNode node) {
        SpyType power = node.children.get(0).accept(this);
        SpyType tail = node.children.get(1).accept(this);
        return tail == SpyType.NONE ? power : resolveBinaryOperation(power, tail);
    }

    @Override
    public SpyType visit(TermTailNode node) {
        if (node.children.isEmpty()) return SpyType.NONE;
        SpyType power = node.children.get(1).accept(this);
        SpyType tail = node.children.get(2).accept(this);
        return tail == SpyType.NONE ? power : resolveBinaryOperation(power, tail);
    }

    @Override
    public SpyType visit(PowerNode node) {
        if (node.children.size() == 1) return node.children.get(0).accept(this);
        SpyType left = node.children.get(0).accept(this);
        SpyType right = node.children.get(2).accept(this);
        return resolveBinaryOperation(left, right);
    }

    @Override
    public SpyType visit(FactorNode node) {
        if (node.children.size() == 1) return node.children.get(0).accept(this); // atom
        
        // Unary +/- factor
        SpyType type = node.children.get(1).accept(this);
        if (type == SpyType.STRING) {
            System.err.println("TypeError: Bad operand type for unary +/-: 'STRING'");
            return SpyType.ERROR;
        }
        return type; 
    }


    // ==========================================================
    // ATOMS & BUILT-IN FUNCTIONS
    // ==========================================================

    @Override
    public SpyType visit(AtomNode node) {
        switch (node.children.size()) {
            case 1 -> {
                return node.children.get(0).accept(this);
            }
            case 3 -> {
                return node.children.get(1).accept(this); // LPAREN expr RPAREN
            }
            case 4 -> {
                // Function calls: IDENTIFIER LPAREN expr RPAREN
                TerminalNode idNode = (TerminalNode) node.children.get(0);
                String funcName = idNode.getToken().lexeme();
                
                // Evaluate the argument to catch any inner semantic errors
                node.children.get(2).accept(this);
                
                // Basic built-in casting functions
                if (funcName.equals("int")) return SpyType.INTEGER;
                if (funcName.equals("float")) return SpyType.FLOAT;
                if (funcName.equals("str")) return SpyType.STRING;
                if (funcName.equals("bool")) return SpyType.BOOLEAN;
                
                return SpyType.UNKNOWN;
            }
            default -> {
            }
        }
        return SpyType.NONE;
    }

    @Override
    public SpyType visit(InputExprNode node) {
        return SpyType.STRING; // Python's input() always returns a string
    }

    @Override
    public SpyType visit(ListNode node) {
        visitChildren(node.children); // Evaluate children for errors
        return SpyType.LIST;
    }


    // ==========================================================
    // LOGICAL & RELATIONAL EXPRESSIONS
    // ==========================================================

    @Override
    public SpyType visit(RelExprNode node) {
        SpyType left = node.children.get(0).accept(this);
        SpyType tail = node.children.get(1).accept(this);
        return combineLogical(left, tail);
    }

    @Override
    public SpyType visit(RelTailNode node) {
        if (node.children.isEmpty()) return SpyType.NONE;
        node.children.get(1).accept(this); // evaluate arith_expr
        node.children.get(2).accept(this); // evaluate rel_tail
        return SpyType.BOOLEAN; // Comparisons always result in boolean
    }

    @Override
    public SpyType visit(NotExprNode node) {
        if (node.children.size() == 1) return node.children.get(0).accept(this);
        node.children.get(1).accept(this);
        return SpyType.BOOLEAN;
    }

    @Override
    public SpyType visit(AndExprNode node) {
        SpyType left = node.children.get(0).accept(this);
        SpyType tail = node.children.get(1).accept(this);
        return combineLogical(left, tail);
    }

    @Override
    public SpyType visit(AndTailNode node) {
        if (node.children.isEmpty()) return SpyType.NONE;
        node.children.get(1).accept(this);
        node.children.get(2).accept(this);
        return SpyType.BOOLEAN;
    }

    @Override
    public SpyType visit(ExprNode node) {
        SpyType left = node.children.get(0).accept(this);
        SpyType tail = node.children.get(1).accept(this);
        return combineLogical(left, tail);
    }

    @Override
    public SpyType visit(ExprTailNode node) {
        if (node.children.isEmpty()) return SpyType.NONE;
        node.children.get(1).accept(this);
        node.children.get(2).accept(this);
        return SpyType.BOOLEAN;
    }


    // ==========================================================
    // STRUCTURAL & CONTROL FLOW (Pass-throughs)
    // ==========================================================

    @Override public SpyType visit(ProgramNode node) { return visitChildren(node.children); }
    @Override public SpyType visit(StmtListNode node) { return visitChildren(node.children); }
    @Override public SpyType visit(StmtNode node) { return visitChildren(node.children); }
    @Override public SpyType visit(SimpleStmtNode node) { return visitChildren(node.children); }
    @Override public SpyType visit(IfStmtNode node) { return visitChildren(node.children); }
    @Override public SpyType visit(ElifListNode node) { return visitChildren(node.children); }
    @Override public SpyType visit(ElsePartNode node) { return visitChildren(node.children); }
    @Override public SpyType visit(ElementsNode node) { return visitChildren(node.children); }
    @Override public SpyType visit(ElementTailNode node) { return visitChildren(node.children); }
    
    @Override 
    public SpyType visit(PrintStmtNode node) { 
        visitChildren(node.children); 
        return SpyType.NONE; 
    }

    @Override 
    public SpyType visit(CompOpNode node) { return SpyType.NONE; }

    @Override 
    public SpyType visit(GenericNode node) { return visitChildren(node.getChildren()); }
}