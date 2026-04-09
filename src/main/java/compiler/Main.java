package compiler;

import java.io.IOException;

import compiler.lexer.LexerAPI;
import compiler.parser.ParserAPI;
import compiler.semantics.SemanticAnalyzer;
import compiler.util.symtab.SymbolTableVisualizer;
import compiler.util.token.TokenWriter;

public class Main {
    
    private static final String GRAMMAR_FILE = "config/grammar.config";
    private static final String SOURCE_FILE = "test_script.spy";
    private static final String TABLE_OUTPUT_FILE = "parsing_table.html";
    private static final String TOKEN_OUTPUT_FILE = "lexed_output.tkn";
    private static final String SYMBOL_TABLE_OUTPUT_FILE = "symbol_table.html";

    public static void main(String[] args) {
        try {
            System.out.println("--- 1. Lexical Analysis ---");
            LexerAPI lexerAPI = new LexerAPI(SOURCE_FILE);
            TokenWriter.writeTokensToFile(lexerAPI.getAllTokens(), TOKEN_OUTPUT_FILE);

            System.out.println("\n--- 2. Parser Initialization ---");
            ParserAPI parserAPI = new ParserAPI(GRAMMAR_FILE);
            parserAPI.exportParsingTable(TABLE_OUTPUT_FILE);

            System.out.println("\n--- 3. Syntax Analysis ---");
            if (parserAPI.parse(lexerAPI)) {
                
                parserAPI.exportAST("ast_graph.html");
                
                System.out.println("\n--- 4. Semantic Analysis ---");
                SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerAPI.getSymbolTable());
                parserAPI.getASTRoot().accept(analyzer);
                
                SymbolTableVisualizer.generateHTML(lexerAPI.getSymbolTable(), "semantic_" + SYMBOL_TABLE_OUTPUT_FILE);
                System.out.println("Semantic Analysis complete. Updated Symbol Table generated.");
            }

        } catch (IOException e) {
            System.err.println("Compilation Error: " + e.getMessage());
        }
    }
}