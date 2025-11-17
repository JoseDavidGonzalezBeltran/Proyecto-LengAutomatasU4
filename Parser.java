import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final Scanner scanner;
    private Token currentToken;
    private final List<String> errors = new ArrayList<>(); 
    // NUEVO: Buffer para almacenar la traza de derivación (el "árbol").
    private final List<String> derivationTrace = new ArrayList<>();

    public Parser(Scanner scanner) {
        this.scanner = scanner;
    }
    
    // --- Lógica de Derivación, Consumo y Errores ---

    private void error(String message) {
        // Registra el error pero no suprime la lógica del parser para avanzar.
        String errorMessage = String.format("Error de Sintaxis en línea %d (%s): %s", 
                                            currentToken.linea, currentToken.lexema, message);
        errors.add(errorMessage); 
        System.err.println(errorMessage);
    }

    private void printProduction(String rule) {
        // Almacena la regla en el buffer en lugar de imprimirla inmediatamente.
        derivationTrace.add("--> APLICANDO REGLA: " + rule);
    }
    
    private void match(Token.Tipo expectedType) {
        if (currentToken.tipo == expectedType) {
            // Almacena el consumo en el buffer.
            derivationTrace.add("    |-> Consumido Token Terminal: " + currentToken.lexema + " (" + expectedType + ")");
            currentToken = scanner.nextToken();
        } else {
            error("Se esperaba '" + expectedType + "' pero se encontró '" + currentToken.tipo + "'");
            // Nota: Aquí se detiene la ejecución sin recuperación simple si hay un error
        }
    }
    
    // --- Método de inicio y Reglas Gramaticales ---
    
    public ASTNode parse() {
        currentToken = scanner.nextToken(); 
        ASTNode root = P();

        // CLAVE: Control estricto para la generación de la salida.
        if (!errors.isEmpty()) {
            // Si hay errores, solo se muestra el reporte final, NUNCA la traza.
            System.out.println("\n--- ANÁLISIS FINALIZADO CON ERRORES. EL ÁRBOL SINTÁCTICO NO SE MUESTRA. ---");
            System.out.println("TOTAL DE ERRORES SINTÁCTICOS Y LÉXICOS ENCONTRADOS: " + errors.size());
            return null; 
        } else if (currentToken.tipo.equals(Token.Tipo.EOF)) {
            // Si es correcto, primero se imprime el nuevo "árbol sintáctico" (la traza).
            System.out.println("\n--- ÁRBOL SINTÁCTICO (Análisis de Derivación) ---");
            for (String line : derivationTrace) {
                System.out.println(line);
            }
            System.out.println("\n--- Análisis completado con éxito. ---");
        } 
        return root;
    }
    
    // P -> D S <eof>
    private ASTNode P() {
        printProduction("P -> D S <eof>");
        ASTNode pNode = new ASTNode("P");
        pNode.children.add(D());
        pNode.children.add(S());
        match(Token.Tipo.EOF);
        return pNode;
    }

    // D -> (int | float) id ; D | ℇ
    private ASTNode D() {
        if (currentToken.tipo == Token.Tipo.INT || currentToken.tipo == Token.Tipo.FLOAT) {
            printProduction("D -> (int | float) id ; D");
            ASTNode dNode = new ASTNode("D");
            dNode.children.add(new ASTNode("Type", currentToken.lexema));
            match(currentToken.tipo); 
            dNode.children.add(new ASTNode("Id", currentToken.lexema));
            match(Token.Tipo.ID); 
            match(Token.Tipo.SEMICOLON); 
            dNode.children.add(D()); 
            return dNode;
        } else {
            printProduction("D -> ℇ");
            return new ASTNode("EmptyD");
        }
    }

    // S -> if E then S else S | while E do S | { S L | input E | output E
    private ASTNode S() {
        ASTNode sNode = new ASTNode("S");
        switch (currentToken.tipo) {
            case IF:
                printProduction("S -> if E then S else S");
                match(Token.Tipo.IF);
                sNode.children.add(E()); 
                match(Token.Tipo.THEN);
                sNode.children.add(S()); 
                match(Token.Tipo.ELSE);
                sNode.children.add(S()); 
                break;
            case WHILE:
                printProduction("S -> while E do S");
                match(Token.Tipo.WHILE);
                sNode.children.add(E()); 
                match(Token.Tipo.DO);
                sNode.children.add(S()); 
                break;
            case L_BRACE:
                printProduction("S -> { S L");
                match(Token.Tipo.L_BRACE);
                sNode.children.add(S()); 
                sNode.children.add(L()); 
                break;
            case INPUT:
                printProduction("S -> input E");
                match(Token.Tipo.INPUT);
                sNode.children.add(E()); 
                break;
            case OUTPUT:
                printProduction("S -> output E");
                match(Token.Tipo.OUTPUT);
                sNode.children.add(E()); 
                break;
            default:
                error("Sentencia S inválida. Se esperaba IF, WHILE, '{', INPUT, u OUTPUT.");
                return new ASTNode("ErrorS");
        }
        return sNode;
    }

    // L -> } | ; S L
    private ASTNode L() {
        if (currentToken.tipo == Token.Tipo.R_BRACE) {
            printProduction("L -> }");
            match(Token.Tipo.R_BRACE);
            return new ASTNode("EndL");
        } else if (currentToken.tipo == Token.Tipo.SEMICOLON) {
            printProduction("L -> ; S L");
            ASTNode lNode = new ASTNode("L");
            match(Token.Tipo.SEMICOLON);
            lNode.children.add(S());
            lNode.children.add(L());
            return lNode;
        } else {
             error("Se esperaba ; o } en la lista L. Encontrado: " + currentToken.tipo);
             return new ASTNode("ErrorL");
        }
    }
    
    // E -> num == num | id == id | num | id
    private ASTNode E() {
        ASTNode eNode = new ASTNode("E");
        
        if (currentToken.tipo == Token.Tipo.NUM) {
            match(Token.Tipo.NUM);
            eNode.children.add(E_prime("NUM"));
        } else if (currentToken.tipo == Token.Tipo.ID) {
            match(Token.Tipo.ID);
            eNode.children.add(E_prime("ID"));
        } else {
            error("Expresión E inválida. Se esperaba NUM o ID.");
        }
        return eNode;
    }
    
    // E' (Auxiliar para E)
    private ASTNode E_prime(String baseType) {
        if (currentToken.tipo == Token.Tipo.EQ_EQ) {
            printProduction("E' -> == " + (baseType.equals("NUM") ? "num" : "id"));
            ASTNode primeNode = new ASTNode("Comparison");
            match(Token.Tipo.EQ_EQ);
            
            Token.Tipo expectedType = baseType.equals("NUM") ? Token.Tipo.NUM : Token.Tipo.ID;
            match(expectedType);
            return primeNode;
        } else {
            printProduction("E' -> ℇ");
            return new ASTNode("EmptyE'");
        }
    }
}