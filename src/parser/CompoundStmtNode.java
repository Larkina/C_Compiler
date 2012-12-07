
package parser;

import lexer.Token;

public class CompoundStmtNode extends Node{
    
    Node decl = null, stmt = null;

    public CompoundStmtNode(int lvl, Token token, Node decl, Node stmt) {
        super(lvl, token);
        this.decl = decl;
        this.stmt = stmt;
    }
    
}
