# SpyLang Compiler & Studio

**SpyLang** is a custom, Python-inspired programming language featuring a fully functional compiler front-end built from scratch in Java. This project includes a Lexer (with a custom NFA-based regex engine), a CLR(1) Parser, a dynamically generated Abstract Syntax Tree (AST), a Semantic Analyzer for strict type-checking, and a dedicated Swing-based IDE (SpyLang Studio) for seamless development and interactive visual debugging.

This project is highly educational and visual, outputting interactive HTML graphs for NFA states, CLR(1) parsing tables, AST structures, and Symbol Tables.

---

## 🌊 Data Flow Architecture

The compiler processes source code through a standard multi-pass pipeline, transforming raw text into an evaluated, semantically verified AST.

1. **Source Code (`.spy`)** ➔ **CharReader**: Reads the file character-by-character with lookahead capabilities.
2. **CharReader** ➔ **Lexer**: The Lexer feeds characters into multiple NFA-based extractors. It also tracks indentation (like Python) to emit `INDENT` and `DEDENT` tokens.
3. **Lexer** ➔ **Token Stream**: A sequence of categorized tokens (e.g., `IDENTIFIER`, `INTEGER`, `PLUS`, `INDENT`).
4. **Grammar File (`grammar.config`)** ➔ **CLRTableGenerator**: Parses the grammar rules, computes FIRST/FOLLOW sets, and generates a Canonical LR(1) Parsing Table.
5. **Token Stream + CLR(1) Table** ➔ **CLRParser**: Performs Shift/Reduce operations. Upon a `Reduce` action, it signals the `ASTBuilder`.
6. **ASTBuilder** ➔ **Abstract Syntax Tree (AST)**: Pops nodes from a stack and dynamically constructs the AST using Reflection based on the reduced grammar rule.
7. **AST** ➔ **Semantic Analyzer**: Traverses the AST using the Visitor pattern to infer types, catch type mismatches, and build the Symbol Table.
8. **Pipeline Outputs**: Generates interactive HTML visualizations (`ast_graph.html`, `parsing_table.html`, etc.) and console logs.

---

## ⚙️ Module Breakdown & Working Details

### 1. Lexical Analysis (`compiler.lexer.*`)
The Lexer is responsible for tokenizing the input stream. It does not rely on Java's built-in regex; instead, it uses a theoretical **Non-deterministic Finite Automaton (NFA)** engine.
* **NFA Engine**: Characters are fed into `State` and `Transition` objects. Extractor classes (`IdentifierExtractor`, `NumberExtractor`, `StringExtractor`, etc.) build their own NFAs to match specific token patterns.
* **Indentation Tracker**: Since SpyLang uses Python-like whitespace, the `IndentationTracker` measures leading spaces and emits `INDENT` or `DEDENT` tokens to represent block scope.
* **Visualizer**: Generates `vis.js` network graphs of the internal NFA states for debugging (`NFAVisualizer`).

### 2. Syntax Analysis (`compiler.parser.*`)
A robust **CLR(1) Parser** that ensures the token stream conforms to the grammar defined in `config/grammar.config`.
* **Grammar Loader**: Dynamically reads the config file, separating Terminals (derived from the `TokenType` enum) from Non-Terminals.
* **Grammar Analyzer & Table Generator**: Computes Nullability and FIRST sets, generates LR(1) items, computes closures, and builds the GOTO/ACTION canonical parsing table.
* **Panic Mode Error Recovery**: If a syntax error is encountered, the parser triggers panic mode. It discards tokens until a synchronization token (like `NEWLINE` or `DEDENT`) is found, unwinds the state stack, and safely resumes parsing.

### 3. Abstract Syntax Tree (`compiler.util.ast.*`)
The AST represents the hierarchical syntactic structure of the code.
* **Dynamic Generation (`ASTGenerator`)**: Before compilation, this script reads `grammar.config` and auto-generates specific Java classes for every grammar rule (e.g., `IfStmtNode`, `ArithExprNode`).
* **AST Builder**: As the CLR Parser reduces a rule, the builder uses Java Reflection to instantiate the auto-generated Node class, passing its children as arguments.
* **Visualization**: `ASTVisualizer` exports the tree structure into a beautiful, interactive `vis.js` HTML graph.

### 4. Semantic Analysis (`compiler.semantics.*`)
Enforces the meaning and rules of the language.
* **Visitor Pattern**: The `SemanticAnalyzer` implements `ASTVisitor<SpyType>`, traversing the tree to evaluate nodes.
* **Strict Type Checking**: Evaluates operations between `SpyType` primitives (`INTEGER`, `FLOAT`, `STRING`, `BOOLEAN`, `LIST`). It strictly enforces type mixing rules (e.g., preventing the concatenation of a `STRING` and an `INTEGER`).
* **Symbol Table**: Tracks variables, their scope, and their inferred types, throwing errors if a variable is used before assignment. Exports to `symbol_table.html`.

### 5. User Interface (`compiler.ui.*`)
**SpyLang Studio** is a custom Swing IDE built for the language.
* **Editor & Console**: A syntax-aware text area for writing `.spy` code and a hacker-green console for viewing compiler output.
* **Dynamic Visualizations Panel**: Automatically detects the HTML and Token files generated during compilation and creates buttons to open them instantly in a new IDE tab or external browser.

---

## 🚀 How to Run the Project

### Prerequisites
* **Java 21** or higher.
* **Maven** (3.8+ recommended).

### Setup
1. Clone or download the project directory.
2. Open a terminal and navigate to the project root (where `pom.xml` is located).
3. Compile the project and resolve dependencies:
   ```bash
   mvn clean compile
   ```

### Running the CLI Compiler
To run the compiler directly on the default `test_script.spy` file via the command line:
```bash
mvn exec:java -Dexec.mainClass="compiler.Main"
```
*This will execute the lexical, syntax, and semantic analysis phases and generate all visualization HTML files in the project root.*

### Running SpyLang Studio (IDE)
To launch the graphical IDE and interact with the compiler visually:
```bash
mvn exec:java -Dexec.mainClass="compiler.ui.CompilerIDE"
```
**Using the IDE:**
1. Type your SpyLang code into the editor or click **"Upload .spy File"** to load a script.
2. Click **"Run Script"** (the green button).
3. Watch the compilation phases process in the Console Output.
4. Click the newly generated buttons on the left panel (e.g., `ast_graph.html`, `parsing_table.html`) to view the interactive compiler visualizations.