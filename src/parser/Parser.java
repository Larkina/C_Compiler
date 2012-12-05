package parser;

import Utils.Util;
import java.util.LinkedList;
import java.util.Queue;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

public class Parser {

    Lexer lex;

    public Parser(Lexer l){
        lex = l;
    }

    boolean buf = false;
    Queue<Token> q = new LinkedList<>();

    void next() throws Exception {
        lex.next();
        q.add(lex.getToken());
    }

    Token getToken() {
        return q.peek();
    }

    void popToken() {
        q.poll();
    }

    TokenType getType() {
        return getToken().getType();
    }
    
    void eatToken(TokenType type, String error_msg) throws Exception{
        next();
        if (getToken().getType() == type) {
            popToken();
        }
        else {
            throw new ParserException(getToken(), error_msg);
        }
    }

    boolean isUnaryOp(TokenType t) {
        return Util.isIn(t, TokenType.AND_B, TokenType.MUL, TokenType.PLUS,
            TokenType.MINUS, TokenType.NOT_B, TokenType.NOT);
    }

    boolean isAssigmentOp(TokenType t) {
        return Util.isIn(t, TokenType.ASSIGN, TokenType.MUL_ASSIGN, TokenType.DIV_ASSIGN,
                TokenType.MOD_ASSIGN, TokenType.PLUS_ASSIGN, TokenType.MINUS_ASSIGN,
                TokenType.SHL_ASSIGN, TokenType.SHR_ASSIGN, TokenType.OR_ASSIGN,
                TokenType.AND_ASSIGN, TokenType.XOR_ASSIGN);
    }

    Node parsePrimaryExpr(int lvl) throws Exception {
         next();
         TokenType type = getToken().getType();
        switch (type) {
            case L_PARENTHESIS: {
                popToken();
                Node l_node = parseExpr(lvl);
                eatToken(TokenType.R_PARENTHESIS, "Unclosed parenthesis");
                return l_node;
            }
            case VAR: case CHAR_CONST: case STRING: case INT: case FLOAT: {
                Token t = getToken();
                popToken();
                return new Node(lvl, t);
            }
            default: {
                throw new ParserException(getToken(), "Excpected primary expr");
            }
        }

    }

    Node parseArgumentExprtList(int lvl) throws Exception {
        Node l_node = parseAssignExpr(lvl);
        next();
        if (getToken().getType() == TokenType.COMMA) {
            Node r_node = parseArgumentExprtList(lvl + 1);
            popToken();
            return new Node(lvl, getToken()).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parsePostfixFactorized(int lvl) throws Exception {
        TokenType type = getToken().getType();
        switch (type) {
            case L_BRAKET: {
                popToken();
                Node l_node = parseExpr(lvl + 1);
                eatToken(TokenType.R_BRAKET, "Unclosed braket");
                Token t = getToken();
                Node r_node = parsePostfixFactorized(lvl + 1);
                return new Node(lvl, t).addChild(l_node).addChild(r_node);
            }
            case L_PARENTHESIS: {
                popToken();
                next();
                Node l_node, r_node;
                if (getToken().getType() != TokenType.R_PARENTHESIS) {
                    l_node = parseArgumentExprtList(lvl + 1);
                    eatToken(TokenType.R_PARENTHESIS, "Unclosed parenthesis");
                    next();
                    Token t = getToken();
                    r_node = parsePostfixExpr(lvl + 1);
                    return new Node(lvl, t).addChild(l_node.incLevel()).addChild(r_node);
                }
                else {
                    Token t = getToken();
                    popToken();
                    return new Node(lvl, t).addChild(parsePostfixExpr(lvl + 1));
                }
            }
            case POINT: case ARROW: {
                popToken();
                eatToken(TokenType.VAR, "Expected identifier");
                next();
                return parsePostfixExpr(lvl);
            }
            case INC: case DEC: {
                popToken();
                return new Node(lvl, getToken()).addChild(parsePostfixExpr(lvl + 1));
            }
        }
        return null;
    }

    Node parsePostfixExpr(int lvl) throws Exception {
        Node l_node = parsePrimaryExpr(lvl);
        next();
        if (Util.isIn(getToken().getType(), TokenType.L_BRAKET, TokenType.L_PARENTHESIS,
                TokenType.POINT, TokenType.ARROW, TokenType.INC, TokenType.DEC)) {
            Token t = getToken();
            Node r_node = parsePostfixFactorized(lvl + 1);
            return new Node(lvl, t).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseCastExpr(int lvl) throws Exception{
        return parseUnaryExpr(lvl);
        // Спросить про каст нормальный
      /*  next();
        if (lex.getToken().getType() == TokenType.L_PARENTHESIS) {
            parseTypeName();
            next();
            if (lex.getToken().getType() == TokenType.R_PARENTHESIS) {
                parseCastExpr();
            }
            else {
                throw new ParserException(lex.getToken(), "Unclosed parenthesis");
            }
        } */
    }

    Node parseUnaryExpr(int lvl) throws Exception {
        next();
        if (isUnaryOp(getToken().getType())) {
            Token t = getToken();
            popToken();
           return new Node(lvl, t).addChild(parseCastExpr(lvl + 1));
        } else
            if (Util.isIn(getToken().getType(), TokenType.DEC, TokenType.INC)) {
                Token t = getToken();
                popToken();
                return new Node(lvl, t).addChild(parseUnaryExpr(lvl + 1));
            }
            else {
                return parsePostfixExpr(lvl);
            }
    }

    Node parseMultiplicativeExpr(int lvl) throws Exception {
        Node l_node = parseCastExpr(lvl);
        next();
        if (Util.isIn(getToken().getType(), TokenType.MUL, TokenType.DIV, TokenType.MOD)) {
            Token t = getToken();
            popToken();
            Node r_node = parseMultiplicativeExpr(lvl + 1);
            return new Node(lvl, t).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseAdditiveExpr(int lvl) throws Exception{
        Node l_node = parseMultiplicativeExpr(lvl);
        next();
        if (Util.isIn(getToken().getType(), TokenType.PLUS, TokenType.MINUS)) {
            Token t = getToken();
            popToken();
            Node r_node = parseAdditiveExpr(lvl + 1);
            return new Node(lvl, t).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseShiftExpr(int lvl) throws Exception {
        Node l_node = parseAdditiveExpr(lvl);
        next();
        if (Util.isIn(getToken().getType(), TokenType.SHL, TokenType.SHR)) {
            Token t = getToken();
            popToken();
            Node r_node = parseShiftExpr(lvl + 1);
            return new Node(lvl, t).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseRelExpr(int lvl) throws Exception {
        Node l_node = parseShiftExpr(lvl);
        next();
        if (Util.isIn(getToken().getType(), TokenType.LE, TokenType.LT, TokenType.GE, TokenType.GT)) {
            Token t = getToken();
            popToken();
            Node r_node = parseRelExpr(lvl + 1);
            return new Node(lvl, t).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseEqExpr(int lvl) throws Exception {
        Node l_node = parseRelExpr(lvl);
        next();
        if (Util.isIn(getToken().getType(), TokenType.EQ, TokenType.NE)) {
            Token t = getToken();
            popToken();
            Node r_node = parseEqExpr(lvl + 1);
            return new Node(lvl, t).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseAndExpr(int lvl) throws Exception {
        Node l_node = parseEqExpr(lvl);
        next();
        if (getToken().getType() == TokenType.AND_B) {
            Token t = getToken();
            popToken();
            Node r_node = parseAndExpr(lvl + 1);
            return new Node(lvl, t).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseExclusiveOrExpr(int lvl) throws Exception {
        Node l_node = parseAndExpr(lvl);
        next();
        if (getToken().getType() == TokenType.XOR) {
            Token t = getToken();
            popToken();
            Node r_node = parseExclusiveOrExpr(lvl + 1);
            return new Node(lvl, t).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseInclusiveOrExpr(int lvl) throws Exception{
        Node l_node = parseExclusiveOrExpr(lvl);
        next();
        if (getToken().getType() == TokenType.OR_B) {
            Token t = getToken();
            popToken();
            Node r_node = parseInclusiveOrExpr(lvl + 1);
            return new Node(lvl, t).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseLogicalAndExpr(int lvl)throws Exception {
        Node l_node = parseInclusiveOrExpr(lvl);
        next();
        if (getToken().getType() == TokenType.AND) {
            Token t = getToken();
            popToken();
            Node r_node = parseLogicalAndExpr(lvl + 1);
            return new Node(lvl, t).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseLogicalOrExpr(int lvl) throws Exception {
        Node l_node = parseLogicalAndExpr(lvl);
        next();
        if (getToken().getType() == TokenType.OR) {
            Token t = getToken();
            popToken();
            Node r_node = parseLogicalOrExpr(lvl + 1);
            return new Node(lvl, t).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseConditionalExpr(int lvl) throws Exception {
        Node l_node = parseLogicalOrExpr(lvl);
        next();
        if (getToken().getType() == TokenType.QUESTION) {
            Node middle, end;
            Token t = getToken();
            popToken();
            middle = parseExpr(lvl + 2);
            Node res = new Node(lvl, t);
            next();
            Token c = getToken();
            if (getToken().getType() == TokenType.COLON) {
                popToken();
                end = parseConditionalExpr(lvl + 2);
            }
            else {
                throw new ParserException(getToken(), "Bad ternary operator");
            }
            res.addChild(l_node.incLevel()).addChild(new Node(lvl + 1, c).addChild(middle).addChild(end));
            return res;
        }
        return l_node;
    }
    
    Node parseAssignExpr(int lvl) throws Exception {
        Node l_node = parseConditionalExpr(lvl);       
        Node r_node = null;
        Token curr_tok = getToken();
        next();
        if (isAssigmentOp(getToken().getType())) {
            curr_tok = getToken();
            popToken();
            r_node = parseConditionalExpr(lvl + 1);
        }
        if (r_node != null) {
            return new Node(lvl, curr_tok).addChild(l_node.incLevel()).addChild(r_node);
        }
        else {
            return l_node;
        }
//        }
    }

    Node parseExpr(int lvl) throws Exception {
        Node l_node = parseAssignExpr(lvl);
        next();
        if (getToken().getType() == TokenType.COMMA) {
            Token curr = getToken();
            popToken();
            Node r_node = parseExpr(lvl + 1);
            return new Node(lvl, curr).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    /*void delaration(int lvl) throws Exception {
        Node l_node = declarationSpec(lvl);
        next();
        if (getToken().getType() == TokenType.SEMICOLON) {
            popToken();
            return l_node;
        }
        Node r_node = initDeclList(lvl + 1);
        next();
        if (getToken().getType() == TokenType.SEMICOLON) {
            popToken();
        } else {
            throw new ParserException(getToken(), "Forgot ;");
        }
        //
    }*/
    
    Node jmpStatement(int lvl) throws Exception{
        next();
        if (Util.isIn(getToken().getText(), "continue", "break")) {
            Token tok = getToken();
            popToken();
            eatToken(TokenType.SEMICOLON, "Forgot ;");
            return new Node(lvl, tok);
        }
        if (getToken().getText() == "return") {
            Token tok = getToken();
            popToken();
            next();
            if (getToken().getType() == TokenType.SEMICOLON) {
                return new Node(lvl, getToken());
            }
            Node l_node = parseExpr(lvl + 1);
            Token c = getToken();
            eatToken(TokenType.SEMICOLON, "Forgot ;");
            return new Node(lvl, tok).addChild(l_node);
        }
        return null;
    }
    
    Node compoundStatement(int lvl) throws Exception {
        next();
        if (getToken().getType() == TokenType.L_BRACE) {
            popToken();
            next();
            Token curr = getToken();
            if (curr.getType() == TokenType.R_BRACE) {
                return null;
            }
            
        }
        return null;
    }
    
    Node exprStatement(int lvl) throws Exception {
        next();
        if (getToken().getType() == TokenType.SEMICOLON) {
            popToken();
            return null;
        }
        Node l_node = parseExpr(lvl);
        next();
        eatToken(TokenType.SEMICOLON, "Forogot ;");
        return l_node;
    }
    
    Node selectionSatement(int lvl) throws Exception {
        next();
        //To do: Node  class tree
        if (getToken().getText() == "if") {
            Token tok = getToken();
            popToken();
            eatToken(TokenType.L_PARENTHESIS, "Excpected (");
            Node cond = parseExpr(lvl + 1);
            eatToken(TokenType.R_PARENTHESIS, "Expected )");
            Node stmt = parseStatement(lvl + 1);
            next();
            Node el = null;
            if (getToken().getText() == "else") {
                popToken();
                el = parseStatement(lvl + 1);
            }
            return new Node(lvl, tok).addChild(cond).addChild(stmt).addChild(el);
        }
        return null;
    }
    
    public Node parse() throws Exception {
        return parseExpr(0);
    }
}
