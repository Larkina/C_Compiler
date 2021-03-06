
package parser;

import lexer.Token;

public class IfStmtNode extends Node {

    Node cond, ifst, elstmt;

    public IfStmtNode(int lvl, Token token, Node cond, Node ifst, Node elstmt) {
        super(lvl, token);
        this.cond = cond;
        this.ifst = ifst;
        this.elstmt = elstmt;
    }

    @Override
    public String toString() {
        String el = "";
        if (elstmt != null) {
            el = elstmt.toString();
        }
        return getSpace() + "IfStmtNode\n" + cond.toString() + ifst.toString() + el;
    }
    
}
