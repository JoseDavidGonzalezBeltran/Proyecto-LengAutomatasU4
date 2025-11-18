import java.util.HashMap;
import java.util.Map;

public class Scanner {
    private final String input;
    private int pos = 0;
    private int linea = 1;

    private static final Map<String, Token.Tipo> PALABRAS_CLAVE;
    static {
        PALABRAS_CLAVE = new HashMap<>();
        PALABRAS_CLAVE.put("int", Token.Tipo.INT);
        PALABRAS_CLAVE.put("float", Token.Tipo.FLOAT);
        PALABRAS_CLAVE.put("if", Token.Tipo.IF);
        PALABRAS_CLAVE.put("then", Token.Tipo.THEN);
        PALABRAS_CLAVE.put("else", Token.Tipo.ELSE);
        PALABRAS_CLAVE.put("while", Token.Tipo.WHILE);
        PALABRAS_CLAVE.put("do", Token.Tipo.DO);
        PALABRAS_CLAVE.put("input", Token.Tipo.INPUT);
        PALABRAS_CLAVE.put("output", Token.Tipo.OUTPUT);
    }

    public Scanner(String input) {
        this.input = input;
    }

    private char peek() { return pos < input.length() ? input.charAt(pos) : '\0'; }
    private void advance() { pos++; }
    private void skipWhitespace() {
        while (pos < input.length() && (Character.isWhitespace(peek()))) {
            if (peek() == '\n') linea++;
            advance();
        }
    }
    private boolean isLetter(char c) { return Character.isLetter(c); }
    private boolean isDigit(char c) { return Character.isDigit(c); }

    public Token nextToken() {
        skipWhitespace();

        if (pos >= input.length()) {
            return new Token(Token.Tipo.EOF, "EOF", linea);
        }

        int start = pos;
        char c = peek();
        
        // Operadores y símbolos
        switch (c) {
            case ';': advance(); return new Token(Token.Tipo.SEMICOLON, ";", linea);
            case '{': advance(); return new Token(Token.Tipo.L_BRACE, "{", linea);
            case '}': advance(); return new Token(Token.Tipo.R_BRACE, "}", linea);
            case '=': 
                advance();
                if (peek() == '=') {
                    advance();
                    return new Token(Token.Tipo.EQ_EQ, "==", linea);
                } else {
                    // Genera ERROR si encuentra '=' simple (ya que no es válido en la gramática)
                    return new Token(Token.Tipo.ERROR, "=", linea);
                }
        }

        // Identificadores y Palabras Clave
        if (isLetter(c)) {
            while (pos < input.length() && (isLetter(peek()) || isDigit(peek()))) {
                advance();
            }
            String lexema = input.substring(start, pos);
            Token.Tipo tipo = PALABRAS_CLAVE.getOrDefault(lexema, Token.Tipo.ID);
            return new Token(tipo, lexema, linea);
        }

        // Numeros
        if (isDigit(c)) {
            while (pos < input.length() && isDigit(peek())) {
                advance();
            }
            // Soporte básico para float no implementado, solo enteros (num)
            String lexema = input.substring(start, pos);
            return new Token(Token.Tipo.NUM, lexema, linea);
        }

        // Error léxico: Carácter no reconocido
        advance();
        return new Token(Token.Tipo.ERROR, String.valueOf(c), linea); 
    }
}