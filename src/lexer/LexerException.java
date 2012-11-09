
package lexer;

public class LexerException extends Exception{

    Integer line, pos;
    String msg;
    
    LexerException(Integer l, Integer p, String m) {
        line = l;
        pos = p;
        msg = m;
    }
    
    LexerException(){
        line = 0;
        pos = 0;
        msg = "";
    }
    
    @Override
    public String getMessage(){
        return "Incorrect token on " + line.toString() + " line, " + pos.toString() + " pos: " + msg;
    }
    
}

