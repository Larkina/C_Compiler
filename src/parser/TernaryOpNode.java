package parser;

import lexer.Token;

public class TernaryOpNode extends Node {
    
    Node cond, mid, end;

    public TernaryOpNode(int lvl, Token token, Node cond, Node mid, Node end) {
        super(lvl, token);
        this.cond = cond;
        this.mid = mid;
        this.end = end;
    }

    @Override
    public Node incLevel() {
        super.incLevel();
        cond.incLevel();
        mid.incLevel();
        end.incLevel();
        return this;
    }
    
    @Override
    public String toString() {
        return super.toString() + cond.toString() + mid.toString() + end.toString();
    }
}
