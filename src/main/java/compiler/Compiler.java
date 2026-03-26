package compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import compiler.lexer.LexerWrapper;
import compiler.util.Token;
import compiler.util.TokenType;

public class Compiler {

    public static void main(String[] args) throws FileNotFoundException {
        File file = new File("input.py");
        StringBuilder source = new StringBuilder();
        try(Scanner sc = new Scanner(file)){
            while(sc.hasNextLine()){
                source.append(sc.nextLine()).append("\n");
            }
        }
        catch(FileNotFoundException e){
            System.out.println("File not found: " + e);
            return;
        }
        String sourceCode = source.toString();
        
        System.out.println("Scanning source code: \n" + sourceCode);
        System.out.println("--------------------------------------------------");
        
        LexerWrapper lexer = new LexerWrapper(sourceCode);
        Token token = lexer.getNextToken();
        
        while (token.type() != TokenType.EOF) {
            System.out.println(token);
            token = lexer.getNextToken();
        }
        
        System.out.println(token);
    }
}
