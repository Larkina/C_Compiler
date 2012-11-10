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
        while ((t = fi.readLine()) != null)
            if (buff.length() == 0)
                buff = t;
            else
                buff += '\n' + t;
        fi.close();

        // Инициализировать меп
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
        
        key_words.add("break");
        key_words.add("case");
        key_words.add("char");
        key_words.add("continue");
        key_words.add("do");
        key_words.add("double");
        key_words.add("else");
        key_words.add("float");
        key_words.add("for");
        key_words.add("goto");
        key_words.add("if");
        key_words.add("int");
        key_words.add("long");
        key_words.add("return");
        key_words.add("short");
        key_words.add("signed");
        key_words.add("sizeof");
        key_words.add("struct");
        key_words.add("switch");
        key_words.add("unsigned");
        key_words.add("void");
        key_words.add("while");

    }

    String file_name;
    private int line = 0;
    private int pos = 0;
    private int curr_pos = 0;
    private int p = 0;
    private int l = 0;
    String buff = "";
    char curr = 0;
    Token currentToken;
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
            if (buff.charAt(curr_pos + 1) == '/'){
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
                if (buff.charAt(curr_pos + 1) == '*'){
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
                    if (buff.charAt(curr_pos + 1) == '=') {
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
        return currentToken;
    }

    Token getIdent() {
        String s = buildStringWithCh();
        line = l;
        pos = curr_pos;
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
        line = l;
        pos = curr_pos;
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
        line = l;
        pos = curr_pos;
        p += tmp.length() + 1;
        return makeToken(val, tmp.toString(), TokenType.INT);    
    }
    
    Token getNumber() throws LexerException{
        TokenType type = TokenType.INT;
        boolean was_point = false;
        boolean was_exp = false;
        boolean was_sign = false;
        StringBuilder tmp = new StringBuilder();
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
                if (was_exp || curr_pos + 1 == buff.length()){
                    throwException("Incorrect float number");
                }
                char next_ch = buff.charAt(curr_pos + 1);
                if (next_ch != '-' && next_ch != '+' && !Character.isDigit(next_ch) || was_sign){
                    throwException("Incorrect float number");
                }
                was_sign = (curr == '+' || curr == '-');
                was_exp = true;
            }

        } while (Character.isDigit(curr) || (curr == '.' || curr == 'e' || curr == '+' || curr == '-'));
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
        line = l;
        pos = curr_pos;
        p += tmp.length() + 1;
        return null;
    }
    
    public boolean next() throws LexerException{
        l = line;
        curr_pos = pos;
        if (curr_pos >= buff.length()){
            currentToken = makeToken("EOF", "EOF", TokenType.EOF);
            return false;
        }
        eatSpace();  
        while (curr == '/'){
            String t = eatComments();
            if (!"".equals(t)){
                if ("eof".equals(t)) {
                    return false;
                }
                currentToken = makeToken(t, t, str_to_type.get(t));
                pos = curr_pos + t.length();
                return true;
            }
            eatSpace();
        }

        if (Character.isLetter(curr)) {
            currentToken = getIdent();
            return true;
        }

        // Разделители Операции
        switch(curr){
            case ';': case ',': case '.':
            case '[': case ']': case '{': case '}':
            case '(': case ')': case ':':
            {
                String s = curr + "";
                TokenType t = str_to_type.get(s);
                currentToken = makeToken(curr, s, t);
                line = l;
                pos = curr_pos + 1;
                return true;
            }
            case '+': case '-': case '*': case '%':
            case '~': case '!': case '&': case '|':
            case '=': case '>': case '<': case '^': {
                String to_str = curr + "";
                if (curr_pos + 1 < buff.length()){
                    char next_ch = buff.charAt(curr_pos + 1);
                    String concat = "" + curr + next_ch;
                    // *= +* -=
                    if (next_ch == '='){
                        currentToken = makeToken(concat, concat, str_to_type.get(concat));
                        pos = curr_pos + 2;
                        p += 2;
                        line = l;
                        return true;
                    }
                    if (next_ch == curr){
                        if (curr == '+' || curr == '-' || curr == '=' || curr == '|' || curr == '&') {
                            currentToken = makeToken(concat, concat, str_to_type.get(concat));
                            pos = curr_pos + 2;
                            p += 2;
                            return true;
                        }
                        else
                            if (curr == '>' || curr == '<'){
                                if (curr_pos + 2 < buff.length() && buff.charAt(curr_pos + 2) == '='){
                                    currentToken = new Token(p + 1, l + 1, concat + "=", concat + "=", str_to_type.get(concat + "="));
                                    pos = curr_pos + 3;
                                    p += 3;
                                    return true;
                                }
                                currentToken = new Token(p + 1, l + 1, concat, concat, str_to_type.get(concat));
                                pos = curr_pos + 2;
                                p += 2;
                                return true;
                            }
                    }
                }
                currentToken = new Token(p + 1, l + 1, to_str, to_str, str_to_type.get(to_str));
                line = l;
                pos = curr_pos + 1;
                ++p;
                return true;
            }
        }
        
        // Строка

        if (curr == '\"'){
            StringBuilder tmp = new StringBuilder();
            tmp.append('\"');
            while (curr_pos + 1  < buff.length() && buff.charAt(curr_pos + 1) != '\"'){
                curr = buff.charAt(++curr_pos);
                if (curr == '\n'){
                    throwException("Unclosed string const");
                }
                if (curr == '\\'){
                    if (curr_pos + 1 == buff.length())
                        break;
                    char tail = buff.charAt(++curr_pos);
                    String res = "";
                    if (Character.isDigit(tail)){
                        if (curr_pos + 2 == buff.length()){
                            ++curr_pos;
                            break;
                        }
                        char f = buff.charAt(curr_pos + 1);
                        char s = buff.charAt(curr_pos + 2);
                        if (Character.isDigit(f)){
                            res += f;
                            curr_pos ++;
                        }

                         if (Character.isDigit(s)){
                            res += s;
                            curr_pos ++;
                        }

                        tmp.append(res);
                    }
                    if (tail == 'x'){
                        if (curr_pos + 2 == buff.length()){
                            ++curr_pos;
                            break;
                        }
                        char f = buff.charAt(curr_pos + 1);
                        char s = buff.charAt(curr_pos + 2);
                        if (Character.isLetterOrDigit(f))
                            if (Character.toLowerCase(f) > 'f'){
                                // Плохая 16ти ричная константа
                                throwException("Incorrect hex const");
                            }
                            else {
                                res += f;
                                curr_pos++;
                            }
                       if (Character.isLetterOrDigit(s))
                            if (Character.toLowerCase(s) > 'f'){
                                // Плохая 16ти ричная константа
                                throwException("Incorrect hex const");
                            }
                            else {
                                res += s;
                                curr_pos++;
                            }
                        tmp.append(res);

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
                    //    case 'a': res = "\a";
                    //    case 'v': res = "\v";
                    //    case '?': res = "\?";

                    }
                    if (res.length() == 0) {
                        // Неправильная эскейп последовательность
                        throwException("Incorrect escape sequence");
                    }
                    tmp.append(res);
                }
                else
                    tmp.append(curr);
            }
            if (curr_pos + 1  == buff.length()){
                // бросить искллючение. мы строковую константу не знакрыли
                throwException("Unclosed string const");
            }
            tmp.append("\"");
            line = l;
            pos = curr_pos + 2;
            p += tmp.length() + 1;
            currentToken = new Token<>(p + 1, l + 1, tmp.toString(), tmp.toString(), TokenType.STRING);
            return true;
        }

        //Числа
       if (Character.isDigit(curr)){
           // 8/16-ти ричные
           if (curr == '0'){
               if (curr_pos + 1 < buff.length() &&
                   Character.toLowerCase(buff.charAt(curr_pos + 1)) == 'x') {
                        currentToken = getHexNumber();
                        return true;
                   }
                else {
                    if (curr_pos + 1 < buff.length() &&
                        Character.isDigit(buff.charAt(curr_pos + 1))){
                            currentToken = getOctNumber();
                            return true;
                    }
                 }
            }
            currentToken = getNumber();
            return true;
       }
        pos = buff.length();
        line = l;
        currentToken = new Token<>(p + 1, l + 1, "EOF", "EOF", TokenType.EOF);
        return false;
    }

}
