package parser;

import Utils.Util;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

public class Parser {

    Lexer lex;

    public Parser(Lexer l){
        lex = l;
    }

    boolean buf = false;
    Token prev;

    void next() throws Exception {
        if (!buf)
            lex.next();
        else
            buf = true;
    }

    Token getToken() {
        if (buf) {
            return prev;
        }
        else {
            return lex.getToken();
        }
    }

    void backToken() {
        prev = lex.getToken();
        buf = true;
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
       // next();
        TokenType type = lex.getToken().getType();
        switch (type) {
            case L_PARENTHESIS: {
                Node l_node = parseExpr(lvl + 1);
                next();
                if (lex.getToken().getType() != TokenType.R_PARENTHESIS) {
                 //   throw new ParserException(lex.getToken(), "Unclosed parenthesis");
                }
                return new Node(lvl, lex.getToken()).addChild(l_node);
            }
            case VAR: case CHAR_CONST: case STRING: case INT: case FLOAT: {
                return new Node(lvl, lex.getToken());
            }
            default: {
                return null;
                //throw new ParserException(lex.getToken(), "Excpected primary expr");
            }
        }

    }

    Node parseArgumentExprtList(int lvl) throws Exception {
        Node l_node = parseAssignExpr(lvl);
        next();
        if (lex.getToken().getType() == TokenType.COMMA) {
            Node r_node = parseArgumentExprtList(lvl + 1);
            return new Node(lvl, lex.getToken()).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parsePostfixFactorized(int lvl) throws Exception {
        TokenType type = lex.getToken().getType();
        switch (type) {
            case L_BRAKET: {
                Node l_node = parseExpr(lvl + 1);
                next();
                if (lex.getToken().getType() == TokenType.R_BRAKET) {
                    next();
                    Node r_node = parsePostfixFactorized(lvl + 1);
                    return new Node(lvl, lex.getToken());
                }
                else {
                    throw new ParserException(lex.getToken(), "Unclosed braket");
                }
            }
            case L_PARENTHESIS: {
                next();
                Node l_node, r_node;
                if (lex.getToken().getType() != TokenType.R_PARENTHESIS) {
                    l_node = parseArgumentExprtList(lvl + 1);
                    next();
                    if (lex.getToken().getType() == TokenType.R_PARENTHESIS) {
                        r_node = parsePostfixFactorized(lvl + 1);
                        return new Node(lvl, lex.getToken()).addChild(l_node).addChild(r_node);
                    }
                    else {
                        throw new ParserException(lex.getToken(), "Unclosed parenthesis");
                    }
                }
                else {
                    return new Node(lvl, lex.getToken()).addChild(parsePostfixFactorized(lvl + 1));
                }
            }
            case POINT: case ARROW: {
                next();
                if (lex.getToken().getType() == TokenType.VAR) {
                    return new Node(lvl, lex.getToken()).addChild(parsePostfixFactorized(lvl + 1));
                }
                else {
                    throw new ParserException(lex.getToken(), "Expected identifier");
                }
            }
            case INC: case DEC: {
                return new Node(lvl, lex.getToken()).addChild(parsePostfixFactorized(lvl + 1));
            }
        }
        return null;
    }

    Node parsePostfixExpr(int lvl) throws Exception {
        Node l_node = parsePrimaryExpr(lvl);
        //next();
        if (Util.isIn(lex.getToken().getType(), TokenType.L_BRAKET, TokenType.L_PARENTHESIS,
                TokenType.POINT, TokenType.ARROW, TokenType.INC, TokenType.DEC)) {
            Node r_node = parsePostfixFactorized(lvl + 1);
            return new Node(lvl, lex.getToken()).addChild(l_node.incLevel()).addChild(r_node);
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
        if (isUnaryOp(lex.getToken().getType())) {
           return new Node(lvl, lex.getToken()).addChild(parseCastExpr(lvl + 1));
        } else
            if (Util.isIn(lex.getToken().getType(), TokenType.DEC, TokenType.INC)) {
                return new Node(lvl, lex.getToken()).addChild(parseUnaryExpr(lvl + 1));
            }
            else {
                return parsePostfixExpr(lvl);
            }
    }

    Node parseMultiplicativeExpr(int lvl) throws Exception {
        Node l_node = parseCastExpr(lvl);
        //next();
        if (Util.isIn(lex.getToken().getType(), TokenType.MUL, TokenType.DIV, TokenType.MOD)) {
            Node r_node = parseMultiplicativeExpr(lvl + 1);
            return new Node(lvl, lex.getToken()).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseAdditiveExpr(int lvl) throws Exception{
        Node l_node = parseMultiplicativeExpr(lvl);
      //  next();
        if (Util.isIn(lex.getToken().getType(), TokenType.PLUS, TokenType.MINUS)) {
            Node r_node = parseAdditiveExpr(lvl + 1);
            return new Node(lvl, lex.getToken()).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseShiftExpr(int lvl) throws Exception {
        Node l_node = parseAdditiveExpr(lvl);
     //   next();
        if (Util.isIn(lex.getToken().getType(), TokenType.SHL, TokenType.SHR)) {
            Node r_node = parseShiftExpr(lvl + 1);
            return new Node(lvl, lex.getToken()).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseRelExpr(int lvl) throws Exception {
        Node l_node = parseShiftExpr(lvl);
      //  next();
        if (Util.isIn(lex.getToken().getType(), TokenType.LE, TokenType.LT, TokenType.GE, TokenType.GT)) {
            Node r_node = parseRelExpr(lvl + 1);
            return new Node(lvl, lex.getToken()).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseEqExpr(int lvl) throws Exception {
        Node l_node = parseRelExpr(lvl);
     //   next();
        if (Util.isIn(lex.getToken().getType(), TokenType.EQ, TokenType.NE)) {
            Node r_node = parseEqExpr(lvl + 1);
            return new Node(lvl, lex.getToken()).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseAndExpr(int lvl) throws Exception {
        Node l_node = parseEqExpr(lvl);
    //    next();
        if (lex.getToken().getType() == TokenType.AND_B) {
            Node r_node = parseAndExpr(lvl + 1);
            return new Node(lvl, lex.getToken()).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseExclusiveOrExpr(int lvl) throws Exception {
        Node l_node = parseAndExpr(lvl);
      //  next();
        if (lex.getToken().getType() == TokenType.XOR) {
            Node r_node = parseExclusiveOrExpr(lvl + 1);
            return new Node(lvl, lex.getToken()).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseInclusiveOrExpr(int lvl) throws Exception{
        Node l_node = parseExclusiveOrExpr(lvl);
     //   next();
        if (lex.getToken().getType() == TokenType.OR_B) {
            Node r_node = parseInclusiveOrExpr(lvl + 1);
            return new Node(lvl, lex.getToken()).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseLogicalAndExpr(int lvl)throws Exception {
        Node l_node = parseInclusiveOrExpr(lvl);
    //    next();
        if (lex.getToken().getType() == TokenType.AND) {
            Node r_node = parseLogicalAndExpr(lvl + 1);
            return new Node(lvl, lex.getToken()).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseLogicalOrExpr(int lvl) throws Exception {
        Node l_node = parseLogicalAndExpr(lvl);
  //      next();
        if (lex.getToken().getType() == TokenType.OR) {
            Node r_node = parseLogicalOrExpr(lvl + 1);
            return new Node(lvl, lex.getToken()).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parseConditionalExpr(int lvl) throws Exception {
        Node l_node = parseLogicalOrExpr(lvl);
    //    next();
        if (lex.getToken().getType() == TokenType.QUESTION) {
            Node middle, end;
            middle = parseExpr(lvl + 2);
            Node res = new Node(lvl, lex.getToken());
            next();
            if (lex.getToken().getType() == TokenType.COLON) {
                end = parseConditionalExpr(lvl + 2);
            }
            else {
                throw new ParserException(lex.getToken(), "Bad ternary operator");
            }
            res.addChild(l_node.incLevel()).addChild(new Node(lvl + 1, lex.getToken()).addChild(middle).addChild(end));
            return res;
        }
        return l_node;
    }

    Node parseAssignExpr(int lvl) throws Exception {
        Node l_node = parseConditionalExpr(lvl);
        Node r_node = null;
        Token curr_tok = lex.getToken();
      //  if (l_node == null) {
      //      l_node = parseUnaryExpr(lvl + 1);
            next();
            if (isAssigmentOp(lex.getToken().getType())) {
                curr_tok = lex.getToken();
                r_node = parseAssignExpr(lvl + 1);
            }
            else {
          //      throw new ParserException(lex.getToken(), "Excpected assigment operator");
            }
            if (r_node != null) {
                return new Node(lvl, curr_tok).addChild(l_node.incLevel()).addChild(r_node);
            }
            else
                return l_node;
//        }
//        return l_node;
    }

    Node parseExpr(int lvl) throws Exception {
        Node l_node = parseAssignExpr(lvl);
    //    next();
        if (lex.getToken().getType() == TokenType.COMMA) {
            Token curr = lex.getToken();
            Node r_node = parseExpr(lvl + 1);
            return new Node(lvl, curr).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    public Node parse() throws Exception {
        return parseExpr(0);
    }
}
