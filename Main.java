public class Main {
    public static void main(String[] args) {
        
        // CÓDIGO CON ERRORES INTENCIONALES PARA DEMOSTRAR LA RECUPERACIÓN Y REPORTE MÚLTIPLE
        String codigoFuente_ConErrores = 
            "int contador; " +
            
            // 2. Bloque Principal (S -> { S L)
            "{ " +
            
            // 3. Sentencia S (input E) que NO consume el punto y coma
            "  input contador " + 
            
            // 4. Cierre del Bloque (L -> })
            "}";

        Scanner scanner = new Scanner(codigoFuente_ConErrores);
        Parser parser = new Parser(scanner);
        
        System.out.println("--- Iniciando Análisis Léxico y Sintáctico ---");
        ASTNode ast = parser.parse();
        
        // Desplegar el Árbol Sintáctico SÓLO si no hay errores (según lo solicitado)
        if (ast != null) {
            System.out.println("\n--- ÁRBOL SINTÁCTICO ---");
            ast.display(0);
        }
    }
}