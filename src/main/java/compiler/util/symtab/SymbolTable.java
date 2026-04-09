package compiler.util.symtab;

import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable {
    public record SymbolRecord(String lexeme, String type, int line, int column) {}

    // Using LinkedHashMap to preserve insertion order (first appearance)
    private final Map<String, SymbolRecord> table = new LinkedHashMap<>();

    public void addSymbol(String lexeme, int line, int column) {
        if (!table.containsKey(lexeme)) {
            table.put(lexeme, new SymbolRecord(lexeme, "ERROR", line, column));
        }
    }

    public void updateType(String lexeme, SpyType type) {
        if (table.containsKey(lexeme)) {
            SymbolRecord old = table.get(lexeme);
            // Overwrite the type field with the name of our inferred SpyType
            table.put(lexeme, new SymbolRecord(old.lexeme(), type.name(), old.line(), old.column()));
        }
    }

    public SpyType getType(String lexeme) {
        if (table.containsKey(lexeme)) {
            String t = table.get(lexeme).type();
            try {
                return SpyType.valueOf(t);
            } catch (IllegalArgumentException e) {
                return SpyType.ERROR;
            }
        }
        return SpyType.ERROR;
    }

    public Map<String, SymbolRecord> getTable() {
        return table;
    }
}