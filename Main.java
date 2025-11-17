public class Main {
    public static void main(String[] args) {
        
        // CÓDIGO CON ERRORES INTENCIONALES PARA DEMOSTRAR LA RECUPERACIÓN Y REPORTE MÚLTIPLE
        String codigoFuente = 
            // Línea 1: Declaración 1
            "int contador;\n" +
            
            // Línea 2: Declaración 2
            "float limite;\n" +
            
            // Línea 3: Inicio del Bloque
            "{\n" +
            
            // Línea 4: Sentencia S1 (input E)
            "  input contador\n" + 
            
            // Línea 5: Separador L1 (;), llama a la Sentencia 2 (output)
            "  ; output 10\n" + 
            
            // Línea 6: Separador L2 (;), llama a la Sentencia 3 (if/else)
            "  ; if contador == limite then output 1 else output 0\n" + 
            
            // Línea 7: Separador L3 (;), llama a la Sentencia 4 (while)
            "  ; while 5 == 5 do input limite\n" + 
            
            // Línea 8: Separador L4 (;), llama a la Sentencia 5 (output E)
            "  ; output contador\n" + 
            
            // Línea 9: Cierre del Bloque (L -> }). L es llamada aquí y ve '}'
            "} \n";
            


        Scanner scanner = new Scanner(codigoFuente);
        Parser parser = new Parser(scanner);
        
        System.out.println("--- Iniciando Análisis Léxico y Sintáctico ---");
        
        // Simplemente llama al parser, la lógica de impresión se maneja internamente.
        parser.parse(); 
    }
}

