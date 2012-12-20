
package parser;

import lexer.Token;

public class WhileStmtNode extends Node{
    
    Node cond, body;

    public WhileStmtNode(int lvl, Token token, Node cond, Node body) {
        super(lvl, token);
        this.cond = cond;
        this.body = body;
    }

    @Override
    public String toString() {
        return getSpace() + "WhileStmtNode\n" + cond.toString() + body.toString();
    }
    
}
