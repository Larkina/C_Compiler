
package parser;

import lexer.Token;

public class DeclNode  extends Node{

    String spec;
    
    public DeclNode(int lvl, Token token) {
        super(lvl, token);
         this.spec = "";//spec;
    }

    @Override
    public String toString() {
        return getSpace() + "Declaration\n";// + childrenToString();
    }
    
}
