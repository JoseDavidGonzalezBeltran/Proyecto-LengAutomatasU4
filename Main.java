public class Main {
    public static void main(String[] args) {
        
        String codigoFuente = 
        "int a1;\n" +
"float limite;\n" +
"{\n" +
"  input limite\n" + 
"  ; output contador\n" + // CORRECCIÓN 1: 'contador' debe ser precedido por 'output'
"  ; while contador == limite do \n" +
"    {\n" + 
"      output contador\n" +
"      ; input contador\n" + 
"    }\n" + 
"  ; if 10 == 5 then \n" +
"      output 1 \n" +
"    else \n" +
"      output 0\n" +
"}\n";
        


        Scanner scanner = new Scanner(codigoFuente);
        Parser parser = new Parser(scanner);
        
        System.out.println("--- Iniciando Análisis Léxico y Sintáctico ---");
        
        // Simplemente llama al parser, la lógica de impresión se maneja internamente.
        parser.parse(); 
    }
}

