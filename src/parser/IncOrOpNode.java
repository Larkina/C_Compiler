package parser;

import lexer.Token;

public class IncOrOpNode extends BinOpNode {

    public IncOrOpNode(int lvl, Token token, Node left, Node right) {
        super(lvl, token, left, right);
    }
    
}
