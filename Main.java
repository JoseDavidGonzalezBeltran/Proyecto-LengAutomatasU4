public class Main {
    public static void main(String[] args) {
        
        String codigoFuente = 
        "int a1\n" + 
            "float b2;\n" +
            "\n" +
            "{\n" +
            "  input int\n" + 
            "  \n" +
            "  , output 10\n" + 
            "  \n" +
            "  if a == b then \n" +
            "    {\n" + 
            "      output 1\n" +
            "    }\n" + 
            "    \n" +
            "  ; while 5 == 5 do\n" +
            "    output 0\n" +
            "}\n" +
            // ERROR 5: Carácter léxico no reconocido '@'. (Violación del Scanner)
            "@";
            


        Scanner scanner = new Scanner(codigoFuente);
        Parser parser = new Parser(scanner);
        
        System.out.println("--- Iniciando Análisis Léxico y Sintáctico ---");
        
        // Simplemente llama al parser, la lógica de impresión se maneja internamente.
        parser.parse(); 
    }
}

