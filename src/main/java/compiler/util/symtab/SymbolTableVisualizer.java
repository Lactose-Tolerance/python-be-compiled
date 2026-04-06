package compiler.util.symtab;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SymbolTableVisualizer {

    public static void generateHTML(SymbolTable symbolTable, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // Write HTML Head and CSS
            writer.write("<!DOCTYPE html>\n<html>\n<head>\n");
            writer.write("<meta charset=\"UTF-8\">\n");
            writer.write("<title>Symbol Table</title>\n");
            writer.write("<style>\n");
            writer.write("  body { font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8f9fa; color: #333; margin: 40px; }\n");
            writer.write("  h1 { text-align: center; color: #2c3e50; }\n");
            writer.write("  .table-container { overflow-x: auto; max-width: 800px; margin: 0 auto; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }\n");
            writer.write("  table { border-collapse: collapse; width: 100%; background: #fff; }\n");
            writer.write("  th, td { padding: 12px 15px; border: 1px solid #e0e0e0; text-align: center; }\n");
            writer.write("  th { background-color: #2c3e50; color: #fff; position: sticky; top: 0; }\n");
            writer.write("  tr:nth-child(even) { background-color: #fbfcfc; }\n");
            writer.write("  tr:hover { background-color: #eaf2f8; }\n");
            writer.write("  .lexeme-col { font-weight: bold; color: #2980b9; }\n");
            writer.write("</style>\n</head>\n<body>\n");

            writer.write("<h1>Symbol Table</h1>\n");
            writer.write("<div class=\"table-container\">\n");
            writer.write("<table>\n");
            writer.write("<thead>\n<tr>\n");
            writer.write("  <th>Lexeme / Symbol</th>\n  <th>Token Type</th>\n  <th>Line</th>\n  <th>Column</th>\n");
            writer.write("</tr>\n</thead>\n<tbody>\n");

            // Iterate over the symbol records and write table rows
            for (SymbolTable.SymbolRecord record : symbolTable.getTable().values()) {
                writer.write("<tr>\n");
                writer.write("  <td class=\"lexeme-col\">" + escapeHtml(record.lexeme()) + "</td>\n");
                writer.write("  <td>" + record.type() + "</td>\n");
                writer.write("  <td>" + record.line() + "</td>\n");
                writer.write("  <td>" + record.column() + "</td>\n");
                writer.write("</tr>\n");
            }

            writer.write("</tbody>\n</table>\n</div>\n</body>\n</html>");
            System.out.println("Symbol table visualization created successfully at: " + fileName);

        } catch (IOException e) {
            System.err.println("Failed to write Symbol Table visualization file: " + e.getMessage());
        }
    }

    // Helper method to safely print strings to HTML
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}