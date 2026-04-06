package compiler.util.token;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TokenWriter {

    /**
     * Writes the entire list of lexed tokens to a text file.
     */
    public static void writeTokensToFile(List<Token> tokens, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("--- LEXED TOKEN STREAM ---\n\n");
            
            for (Token token : tokens) {
                // Relies on the existing toString() format in Token.java
                writer.write(token.toString());
                writer.newLine();
            }
            
            System.out.println("Lexed tokens successfully written to: " + fileName);
        } catch (IOException e) {
            System.err.println("Failed to write tokens to file: " + e.getMessage());
        }
    }
}