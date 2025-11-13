public class Token {
    public enum Tipo {
        INT, FLOAT, ID, NUM, IF, THEN, ELSE, WHILE, DO, INPUT, OUTPUT, 
        EQ_EQ, L_BRACE, FIN_BLOQUE, FINAL_DE_SENTENCIA, EOF, ERROR
    }
    
    public final Tipo tipo;
    public final String lexema;
    public final int linea;

    public Token(Tipo tipo, String lexema, int linea) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
    }
    
    @Override
    public String toString() {
        return String.format("<%s, %s> (Línea: %d)", tipo, lexema, linea);
    }
}