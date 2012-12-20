
package parser;

import lexer.Token;

public class CompoundStmtNode extends Node{
    
    Node decl = null, stmt = null;

    public CompoundStmtNode(int lvl, Token token, Node decl, Node stmt) {
        super(lvl, token);
        this.decl = decl;
        this.stmt = stmt;
    }

    @Override
    public String toString() {
        String res = "";
        if (stmt != null) {
            res += stmt.childrenToString();
        }
        return res;
    }
    
}
