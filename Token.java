import java.util.Objects;

public class Token {
    public enum Tipo {
        INT, FLOAT, ID, NUM, IF, THEN, ELSE, WHILE, DO, INPUT, OUTPUT, 
        EQ_EQ, LLAVE_IZQ, LLAVE_DER, PUNTO_COMA, EOF, ERROR
    }
    
    public final Tipo tipo;
    public final String lexema;
    public final int linea;

    public Token(Tipo tipo, String lexema, int linea) {
        this.tipo = Objects.requireNonNull(tipo);
        this.lexema = Objects.requireNonNull(lexema);
        this.linea = linea;
    }
    
    @Override
    public String toString() {
        return String.format("<%s, %s> (Línea: %d)", tipo, lexema, linea);
    }
}