package compiler.util.ast;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import compiler.parser.grammar.Production;
import compiler.util.token.Token;

public class ASTBuilder {
    private final Stack<ASTNode> nodeStack = new Stack<>();
    private static final String NODES_PACKAGE = "compiler.util.ast.nodes.";

    public void shift(Token token) {
        nodeStack.push(new TerminalNode(token));
    }

    public void reduce(Production production) {
        int rhsSize = production.getRightHandSide().size();
        List<ASTNode> poppedNodes = new ArrayList<>();
        
        // Pop nodes from the stack in reverse order to preserve left-to-right ordering
        for (int i = 0; i < rhsSize; i++) {
            poppedNodes.add(0, nodeStack.pop());
        }

        String ruleName = production.getLeftHandSide().getName();

        // Optional: AST Cleanup - Unwrap pass-through rules to flatten the tree
        // If a rule like 'expr -> term' just passes one non-terminal up, don't wrap it.
        // if (poppedNodes.size() == 1 && !(poppedNodes.get(0) instanceof TerminalNode) && !ruleName.equals("program")) {
        //     nodeStack.push(poppedNodes.get(0));
        //     return;
        // }

        // Construct the generated class name
        String className = NODES_PACKAGE + ASTGenerator.toPascalCase(ruleName) + "Node";

        try {
            // Use Reflection to load the generated class
            Class<?> clazz = Class.forName(className);
            Constructor<?> constructor = clazz.getConstructor(List.class);
            
            // Instantiate the class and push it to the stack!
            ASTNode newNode = (ASTNode) constructor.newInstance(poppedNodes);
            nodeStack.push(newNode);
            
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            // If the class doesn't exist or fails, fall back gracefully
            System.err.println("Warning: Could not instantiate " + className + ". Using GenericNode.");
            nodeStack.push(new GenericNode(ruleName, poppedNodes));
        }
    }

    public ASTNode getRoot() {
        return nodeStack.isEmpty() ? null : nodeStack.peek();
    }
}