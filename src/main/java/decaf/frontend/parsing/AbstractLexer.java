package decaf.frontend.parsing;

import decaf.driver.ErrorIssuer;
import decaf.driver.error.DecafError;
import decaf.driver.error.IntTooLargeError;
import decaf.frontend.tree.Pos;

import java.io.IOException;

/**
 * The abstract lexer specifies all methods that a concrete lexer (i.e. the one generated by jflex) should implement.
 * Also, a couple of helper methods are provided.
 * <p>
 * See {@code src/main/jflex/Decaf.jflex}.
 */
abstract class AbstractLexer {

    /**
     * Get position of the current token.
     */
    abstract Pos getPos();

    /**
     * Get the next token (if any). NOTE that every token is encoded as an integer, called the token's _code_.
     *
     * @throws IOException
     */
    abstract int yylex() throws IOException;

    private AbstractParser parser;
    private ErrorIssuer issuer;

    /**
     * When lexing, we need to interact with the parser to set semantic value.
     */
    void setup(AbstractParser parser, ErrorIssuer issuer) {
        this.parser = parser;
        this.issuer = issuer;
    }

    /**
     * Helper method used by the concrete lexer: record a keyword by its code.
     *
     * @param code the token's code
     * @return just {@code code}
     */
    protected int keyword(int code) {
        parser.semValue = new SemValue(code, getPos());
        return code;
    }

    /**
     * Helper method used by the concrete lexer: record an operator (with a single character).
     *
     * @param code the token's code
     * @return just `code`
     */
    protected int operator(int code) {
        parser.semValue = new SemValue(code, getPos());
        return code;
    }

    /**
     * Helper method used by the concrete lexer: record a constant integer.
     *
     * @param value the text representation of the integer
     * @return the token INT_LIT
     */
    protected int intConst(String value) {
        parser.semValue = new SemValue(Tokens.INT_LIT, getPos());
        try {
            parser.semValue.intVal = Integer.decode(value);
        } catch (NumberFormatException e) {
            issueError(new IntTooLargeError(getPos(), value));
        }
        return Tokens.INT_LIT;
    }

    /**
     * Helper method used by the concrete lexer: record a constant bool.
     *
     * @param value the text representation of the bool, i.e. "true" or "false"
     * @return the token BOOL_LIT
     */
    protected int boolConst(boolean value) {
        parser.semValue = new SemValue(Tokens.BOOL_LIT, getPos());
        parser.semValue.boolVal = value;
        return Tokens.BOOL_LIT;
    }

    /**
     * Helper method used by the concrete lexer: record a constant string.
     *
     * @param value the _quoted_ string, i.e. the exact user input
     * @return the token STRING_LIT
     */
    protected int stringConst(String value, Pos pos) {
        parser.semValue = new SemValue(Tokens.STRING_LIT, pos);
        parser.semValue.strVal = value;
        return Tokens.STRING_LIT;
    }

    /**
     * Helper method used by the concrete lexer: record an identifier.
     *
     * @param name the text representation (or name) of the identifier
     * @return the token IDENTIFIER
     */
    protected int identifier(String name) {
        parser.semValue = new SemValue(Tokens.IDENTIFIER, getPos());
        parser.semValue.strVal = name;
        return Tokens.IDENTIFIER;
    }

    /**
     * Helper method used by the concrete lexer: report error.
     *
     * @param error the error
     */
    protected void issueError(DecafError error) {
        issuer.issue(error);
    }
}
