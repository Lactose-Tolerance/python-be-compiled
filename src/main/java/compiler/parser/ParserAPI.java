package compiler.parser;

import java.io.IOException;

import compiler.lexer.LexerAPI;
import compiler.parser.grammar.Grammar;
import compiler.parser.table.CLRTableGenerator;
import compiler.parser.table.ParsingTable;
import compiler.parser.util.GrammarLoader;
import compiler.parser.util.ParsingTableFormatter;
import compiler.util.ast.ASTNode;
import compiler.util.ast.visualizer.ASTVisualizer;

public class ParserAPI {
    private final Grammar grammar;
    private final ParsingTable table;
    private final CLRParser parser;

    /**
     * Initializes the entire parsing subsystem by loading the grammar
     * and generating the canonical LR(1) parsing table.
     */
    public ParserAPI(String grammarFilePath) throws IOException {
        // 1. Load Grammar
        this.grammar = GrammarLoader.load(grammarFilePath);
        
        // 2. Generate CLR(1) Table
        CLRTableGenerator generator = new CLRTableGenerator(this.grammar);
        this.table = generator.generate();
        
        // 3. Initialize Parser
        this.parser = new CLRParser(this.table);
    }

    /**
     * Executes the parsing phase using tokens provided by the LexerAPI.
     */
    public boolean parse(LexerAPI lexerAPI) {
        return parser.parse(lexerAPI);
    }

    /**
     * Exposes the root of the generated Abstract Syntax Tree.
     * Ensure parse() has been called and returned true before accessing.
     */
    public ASTNode getASTRoot() {
        return parser.getAstBuilder().getRoot(); 
    }

    // --- Accessors for Internal Components ---
    
    public ParsingTable getParsingTable() { return table; }
    public Grammar getGrammar() { return grammar; }

    // --- Diagnostic & Visualization Helpers ---

    public void exportParsingTable(String filename) {
        try {
            ParsingTableFormatter.writeTableToFile(table, filename);
            System.out.println("Parsing Table exported to: " + filename);
        } catch (IOException e) {
            System.err.println("Failed to export parsing table: " + e.getMessage());
        }
    }

    public void exportAST(String filename) {
        ASTNode root = getASTRoot();
        if (root != null) {
            ASTVisualizer.generateHTML(root, filename);
        } else {
            System.err.println("Cannot export AST: Root is null (did parsing fail?)");
        }
    }
}