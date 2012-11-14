
package c_compiler;

import java.io.FileNotFoundException;
import java.io.IOException;
import lexer.Lexer;
import lexer.LexerException;

public class C_Compiler {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        
        String tests = System.getProperty("user.dir") + "\\tests\\lexer\\";
        String t_n = "23.in";
        Lexer l = new Lexer(tests + t_n);
        
       /* if (args.length == 0){
             System.out.println(" Simple C Compiler S8303a Larkina O.S. 2012.");
             System.out.println("Usage: java -jar C_compiler.java filename");
             return;
           }
        Lexer l = new Lexer(args[0]);*/
        try {
            while (l.next()){
                System.out.println(l.getToken().toString());
            }
        }
        catch (LexerException e){
            System.out.println(e.getMessage());
        }
    }
}
