
package parser;

import lexer.Token;

public class ForStmtNode extends Node{
    
    Node var, cond, act, body;

    public ForStmtNode(int lvl, Token token, Node var, Node cond, Node act, Node body) {
        super(lvl, token);
        this.var = var;
        this.cond = cond;
        this.act = act;
        this.body = body;
    }

    @Override
    public String toString() {
        return getSpace() + "ForStmtNode\n"  + 
                (var == null ? "" : var )+ 
                (cond == null ? "" : cond ) + 
                (act == null ? "" : act) +
                (body == null ? "" : body);
    }
    
    
}
