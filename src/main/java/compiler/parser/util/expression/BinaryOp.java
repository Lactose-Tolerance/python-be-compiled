package compiler.parser.util.expression;

import compiler.util.Token;

public record BinaryOp(
    Expression left, 
    Token operator,
    Expression right
) implements Expression {}