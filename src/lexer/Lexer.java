package lexer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class Lexer {

    public Lexer(String file_name) throws FileNotFoundException, IOException {
        this.file_name = file_name;
        BufferedReader fi = new BufferedReader(new FileReader(file_name));
        int i = 0;
        String t;
        while ((t = fi.readLine()) != null) {
            if (buff.length() == 0) {
                buff = t;
            }
            else {
                buff += '\n' + t;
            }
        }
        fi.close();

        str_to_type.put(";", TokenType.SEMICOLON);
        str_to_type.put(".", TokenType.POINT);
        str_to_type.put(",", TokenType.COMMA);
        str_to_type.put("(", TokenType.L_PARENTHESIS);
        str_to_type.put(")", TokenType.R_PARENTHESIS);
        str_to_type.put("[", TokenType.L_BRAKET);
        str_to_type.put("]", TokenType.R_BRAKET);
        str_to_type.put("{", TokenType.L_BRACE);
        str_to_type.put("}", TokenType.R_BRACE);
        str_to_type.put("/", TokenType.DIV);
        str_to_type.put("/=", TokenType.DIV_ASSIGN);
        str_to_type.put("+", TokenType.PLUS);
        str_to_type.put("-", TokenType.MINUS);
        str_to_type.put("*", TokenType.MUL);
        str_to_type.put("%", TokenType.MOD);
        str_to_type.put("+=", TokenType.PLUS_ASSIGN);
        str_to_type.put("-=", TokenType.MINUS_ASSIGN);
        str_to_type.put("*=", TokenType.MUL_ASSIGN);
        str_to_type.put("%=", TokenType.MOD_ASSIGN);
        str_to_type.put("!", TokenType.NOT);
        str_to_type.put("~", TokenType.NOT_B);
        str_to_type.put("|", TokenType.OR_B);
        str_to_type.put("&", TokenType.AND_B);
        str_to_type.put("^", TokenType.XOR);
        str_to_type.put("!=", TokenType.NE);
        str_to_type.put("&=", TokenType.AND_ASSIGN);
        str_to_type.put("|=", TokenType.OR_ASSIGN);
        str_to_type.put("==", TokenType.EQ);
        str_to_type.put("||", TokenType.OR);
        str_to_type.put("&&", TokenType.AND);
        str_to_type.put("=", TokenType.ASSIGN);
        str_to_type.put("++", TokenType.INC);
        str_to_type.put("--", TokenType.DEC);
        str_to_type.put(">>", TokenType.SHR);
        str_to_type.put("<<", TokenType.SHL);
        str_to_type.put(">>=", TokenType.SHR_ASSIGN);
        str_to_type.put("<<=", TokenType.SHL_ASSIGN);
        str_to_type.put(">=", TokenType.GE);
        str_to_type.put("<=", TokenType.LE);
        str_to_type.put(">", TokenType.GT);
        str_to_type.put("<", TokenType.LT);
        str_to_type.put(":", TokenType.COLON);
        str_to_type.put("->", TokenType.ARROW);
        
        key_words.add("break");
        key_words.add("char");
        key_words.add("continue");
        key_words.add("do");
        key_words.add("double");
        key_words.add("else");
        key_words.add("float");
        key_words.add("for");
        key_words.add("if");
        key_words.add("int");
        key_words.add("long");
        key_words.add("return");
        key_words.add("struct");
        key_words.add("void");
        key_words.add("while");

    }

    String file_name;
    private int line = 0;
    private int curr_pos = 0;
    private int p = 0;
    private int l = 0;
    String buff = "";
    char curr = 0;
    Token token;
    HashMap<String, TokenType> str_to_type = new HashMap<>();
    HashSet<String> key_words = new HashSet<>();
    
    char getNextChar() {
        curr_pos++;
        if (curr_pos >= buff.length()) {
            return 0;
        }
        else {
            return buff.charAt(curr_pos);
        }
    }
    
    char getNextChar(int idx){
        if (curr_pos + idx >= buff.length()) {
            return 0;
        }
        else {
            return buff.charAt(curr_pos + idx);
        }
    }
    
    String buildStringWithCh(){
        StringBuilder tmp = new StringBuilder();
        do {
            tmp.append(curr);
            curr = getNextChar();
        } while (Character.isLetterOrDigit(curr));
        return tmp.toString();
    }
    
    void throwException(String msg) throws LexerException {
        throw new LexerException(l + 1, p + 1, msg);
    }
    
    Token makeToken(Object val, String text, TokenType type){
        return new Token<>(p + 1, l + 1, val, text, type);
    }
    
    public static <T> boolean isIn(T t, T... ts) {
        for(T i: ts) { 
            if (t.equals(i)) {
                return true;
            }
        }
        return false;
    }

    void setParam(int pp, int cp) {
        p = pp;
        curr_pos = cp;
    }
    
    private void eatSpace(){
        while ((curr = getNextChar()) != 0){
            if (!Character.isSpaceChar(curr)) {
                break;
            }
            if (curr == '\n'){
                ++l;
                p = 0;
            }
            ++p;
        }
    }

    String eatComments() throws LexerException{
        while (curr == '/'){
            if (getNextChar(1) == '/'){
                ++l;
                p = 0;
                do {
                    curr = getNextChar();
                } while (curr != '\n');
                if (curr_pos >= buff.length()){
                    return "eof";
                }
            }
            else
                if (getNextChar(1) == '*'){
                    curr_pos++;
                    p+= 2;
                    while ((curr = getNextChar()) != '*' && buff.charAt(curr_pos + 1) != '/') {
                        if (curr == '\n'){
                            l++;
                            p = 0;
                        }
                        p++;
                    }
                    curr_pos++; 
                    p += 2;
                    if (curr_pos >= buff.length()){
                        throwException("Unclosed multiline comment");
                    }
                    curr = getNextChar();
                }
                else
                {
                    if (getNextChar(1) == '=') {
                        return "/=";
                    }
                    else {
                        return "/";
                    }
                }
        }
        return "";
    }
    
    public Token getToken(){
        return token;
    }

    Token getIdent() {
        String s = buildStringWithCh();
        TokenType type = TokenType.VAR;
        if (key_words.contains(s)) {
            type = TokenType.KEY_WORD;
        }
        return makeToken(s, s, type);       
    }
    
    Token getHexNumber() throws LexerException {
        String tmp = buildStringWithCh();
        Integer val = 0;
        try {
            val = Integer.parseInt(tmp.substring(2),  16);
        }
        catch (Exception e){
            throwException("Incorrect hex nubmer");
        }
        p += tmp.length() + 1;
        return makeToken(val, tmp, TokenType.INT);
    }
    
    Token getOctNumber() throws LexerException{
        StringBuilder tmp = new StringBuilder();
        do {
            tmp.append(curr);
            curr = getNextChar();
        } while (Character.isDigit(curr));
        Integer val = 0;
        try {
            val = Integer.parseInt(tmp.toString(), 8);
        }
        catch (Exception e){
            throwException("Incorrect oct number");
        }
        p += tmp.length() + 1;
        return makeToken(val, tmp.toString(), TokenType.INT);    
    }
    
    Token getNumber(String ... args) throws LexerException{
        TokenType type = TokenType.INT;
        boolean was_point = false;
        boolean was_exp = false;
        boolean was_sign = false;
        StringBuilder tmp = new StringBuilder(args[0]);
        do {
            tmp.append(curr);
            curr = Character.toLowerCase(getNextChar());
            if (curr == '.') {
                if (was_point){
                    throwException("Incorrect float number");
                }
                else {
                    was_point = true;
                }
            }
            if (curr == 'e'){
                char next_ch = getNextChar();
                if (was_exp && next_ch != '-' && next_ch != '+' && !Character.isDigit(next_ch) || was_sign){
                    throwException("Incorrect float number");
                }
                was_sign = isIn(curr, '+' , '-');
                was_exp = true;
            }

        } while (Character.isDigit(curr) || isIn(curr, '.', 'e', '+', '-'));
        String s = tmp.toString();
        Double dval;
        Integer val;
        try { 
            if (was_point || was_exp){
                dval = Double.parseDouble(s);
                return makeToken(dval, s, type);
            }
            else {
                val = Integer.parseInt(s,  10);
                return makeToken(val, s, type);
            }
        } catch (Exception e){
            throwException("Incorrect number");
        }
        p += tmp.length() + 1;
        return null;
    }
    
    Token getString() throws LexerException{
        StringBuilder tmp = new StringBuilder();
        tmp.append('\"');
        StringBuilder val = new StringBuilder();
        while ((curr = getNextChar()) != '\"' && curr != 0){
            if (curr == '\n'){
                throwException("Unclosed string const");
            }
            if (curr == '\\'){
                char tail = getNextChar();
                String res = "";
                if (Character.isDigit(tail)){
                    char t; int i;
                    for(i = 1, t = getNextChar(i); i < 3 && Character.isDigit(t); ++i) {
                        res += t;
                    }
                    curr_pos += res.length();
                    tmp.append(tail).append(res);
                    val.append(Integer.parseInt(tail + res, 8));
                }
                if (tail == 'x'){
                    char t; int i;
                    for(i = 1, t = getNextChar(i); i < 4 && Character.isLetterOrDigit(t); ++i) {
                        res += t;
                    }
                    curr_pos += res.length();
                    tmp.append(res);
                    val.append(Integer.parseInt(res, 16));
                }
                switch(tail){
                    case '\\': {res = "\\"; break;}
                    case '\"': {res = "\""; break; }
                    case '\'': {res = "\'"; break; }
                    case 'n':  {res = "\n"; break; }
                    case 'r': {res = "\r"; break; }
                    case 'b': {res = "\b"; break; }
                    case 't': {res = "\t"; break; }
                    case 'f': {res = "\f"; break; }
                }
                if (res.length() == 0) {
                    res += tail;
                    val.append(tail);
                    //throwException("Incorrect escape sequence");
                }
                else {
                    val.append('\\' + tail);
                }
                tmp.append(res);
                
            }
            else {
                val.append(curr);
                tmp.append(curr);
            }
        }
        if (curr_pos + 1  == buff.length()){
            throwException("Unclosed string const");
        }
        tmp.append("\"");
        curr_pos += 2;
        p += tmp.length() + 1;
        return makeToken(val.toString(), tmp.toString(), TokenType.STRING);    
    }
    
    Token getOperation() {
        Token tmpToken;
        String to_str = curr + "";
        char next_ch = getNextChar(1);
        if (next_ch != 0){
            String concat = "" + curr + next_ch;
            if ((curr == '-' && next_ch == '>') || next_ch == '=') {
                setParam(p + 2, curr_pos + 2);
            }
            if (next_ch == curr){
                if (isIn(curr, '>', '<') && getNextChar(2) == '='){
                    concat = concat + "=";
                    setParam(p + 3, curr_pos + 3);
                }
                else {
                    setParam(p + 2, curr_pos + 2);
                }
            }
            tmpToken = makeToken(concat, concat, str_to_type.get(concat));
        } 
        else 
        {
            tmpToken = makeToken(to_str, to_str, str_to_type.get(to_str));
            setParam(p + 1, curr_pos + 1);
        }    
        return tmpToken;
    }
    
    public boolean next() throws LexerException{
        if (curr_pos >= buff.length()){
            token = makeToken("EOF", "EOF", TokenType.EOF);
            return false;
        }
        eatSpace();  
        while (curr == '/'){
            String t = eatComments();
            if (!"".equals(t)){
                if ("eof".equals(t)) {
                    return false;
                }
                token = makeToken(t, t, str_to_type.get(t));
                curr_pos += t.length();
                return true;
            }
            eatSpace();
        }

        if (Character.isLetter(curr)) {
            token = getIdent();
            return true;
        }
 
        if (isIn(curr, ';', ',', '.', '[', ']', '{', '}', '(', ')', '^')){
            if (curr == '.' && Character.isDigit(getNextChar(1))) {
                token = getNumber("0");
            }
            else {
                token = makeToken(curr, curr + "", str_to_type.get(curr + ""));
            }
            curr_pos++;
            return true;
        }
        
        if (isIn(curr, '+', '-', '*', '%', '~', '!', '&', '|', '=', '>', '<', '^')){
            token = getOperation();
            return true;
        }

        if (curr == '\"'){
            token = getString();
            return true;
        }

       if (Character.isDigit(curr)){
           if (curr == '0'){
               if (Character.toLowerCase(getNextChar(1)) == 'x') {
                        token = getHexNumber();
                        return true;
                   }
                else {
                    if (Character.isDigit(getNextChar(1))){
                        token = getOctNumber();
                        return true;
                    }
                 }
            }
            token = getNumber();
            return true;
       }
       
        curr_pos = buff.length();
        token = makeToken("EOF", "EOF", TokenType.EOF);
        return false;
    }

}
