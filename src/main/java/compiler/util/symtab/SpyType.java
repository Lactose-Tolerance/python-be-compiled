package compiler.util.symtab;

public enum SpyType {
    INTEGER,
    FLOAT,
    STRING,
    BOOLEAN,
    LIST,
    NONE,      // For uninitialized variables or functions returning nothing
    UNKNOWN,   // Default state before inference
    ERROR      // For type mismatch tracking
}