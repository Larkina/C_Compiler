package parser;

import java.io.IOException;
import lexer.*;

public class Parser {
    
    Lexer lex;
    
    Parser(Lexer l){
        lex = l;
    }
    
    void parse_assign_expr() {
    
    }
    
    void parse_expr() throws LexerException, IOException {
        lex.next();
        parse_assign_expr();
        while (lex.next()) {
            if (lex.getToken().getType() == TokenType.COMMA) {
                parse_assign_expr();
            }
        }
        
    }
    
    void parse() {
        
    }
}
