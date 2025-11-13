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

    // --- Auxiliares ---
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

    // --- Método principal del Scanner ---
    public Token nextToken() {
        skipWhitespace();

        if (pos >= input.length()) {
            return new Token(Token.Tipo.EOF, "EOF", linea);
        }

        int start = pos;
        char c = peek();
        
        // Operadores y símbolos
        switch (c) {
            case ';': advance(); return new Token(Token.Tipo.FINAL_DE_SENTENCIA, ";", linea);
            case '{': advance(); return new Token(Token.Tipo.L_BRACE, "{", linea);
            case '}': advance(); return new Token(Token.Tipo.FIN_BLOQUE, "}", linea);
            case '=': 
                advance();
                if (peek() == '=') {
                    advance();
                    return new Token(Token.Tipo.EQ_EQ, "==", linea);
                } else {
                    // Error léxico si se encuentra solo un '=', ya que no está definido en la gramática
                    System.err.println("Error Léxico en línea " + linea + ": Símbolo '=' no reconocido.");
                    return new Token(Token.Tipo.ERROR, "=", linea);
                }
        }

        // Identificadores y Palabras Clave (id à letra (letra | digito) +)
        if (isLetter(c)) {
            while (pos < input.length() && (isLetter(peek()) || isDigit(peek()))) {
                advance();
            }
            String lexema = input.substring(start, pos);
            Token.Tipo tipo = PALABRAS_CLAVE.getOrDefault(lexema, Token.Tipo.ID);
            return new Token(tipo, lexema, linea);
        }

        // Números (num à digito +)
        if (isDigit(c)) {
            while (pos < input.length() && isDigit(peek())) {
                advance();
            }
            String lexema = input.substring(start, pos);
            return new Token(Token.Tipo.NUM, lexema, linea);
        }

        // Error léxico: Carácter no reconocido
        System.err.println("Error Léxico en línea " + linea + ": Carácter no reconocido '" + c + "'");
        advance();
        // **Parar compilación o retornar ERROR para manejo en el Parser**
        return new Token(Token.Tipo.ERROR, String.valueOf(c), linea); 
    }
}