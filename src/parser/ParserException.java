
package parser;

import lexer.Token;

public class ParserException extends Exception {

    Token tok;
    String msg = "";

    ParserException(Token tok, String msg) {
        this.tok = tok;
        this.msg = msg;
    }

    ParserException(){
    }

    @Override
    public String getMessage(){
        return "Parse error on " + tok.getLine() + " line, " + tok.getPos() + " pos: " + msg;
    }

}
