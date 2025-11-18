import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final Scanner scanner;
    private Token currentToken;
    private final List<String> errors = new ArrayList<>(); 
    private final List<String> derivationTrace = new ArrayList<>();

    public Parser(Scanner scanner) {
        this.scanner = scanner;
    }
    
    // --- Lógica de Derivación, Consumo y Errores ---

    private void error(String message) {
        String errorMessage = String.format("Error de Sintaxis en línea %d (%s): %s", 
                                            currentToken.linea, currentToken.lexema, message);
        errors.add(errorMessage); 
        System.err.println(errorMessage);
    }

    private void printProduction(String rule) {
        derivationTrace.add("--> APLICANDO REGLA: " + rule);
    }
    
    private void match(Token.Tipo expectedType) {
        if (currentToken.tipo == expectedType) {
            derivationTrace.add("    |-> Consumido Token Terminal: " + currentToken.lexema + " (" + expectedType + ")");
            currentToken = scanner.nextToken();
        } else {
            error("Se esperaba '" + expectedType + "' pero se encontró '" + currentToken.tipo + "'");
            // No avanzamos el token en match() para que la recuperación se maneje en el método de la regla.
        }
    }

    // --- Método de inicio y Reglas Gramaticales ---
    
    public ASTNode parse() {
        currentToken = scanner.nextToken(); 
        ASTNode root = P();

        if (!errors.isEmpty()) {
            System.out.println("\n--- ANALISIS FINALIZADO CON ERRORES. EL ARBOL SINTACTICO NO SE MUESTRA. ---");
            System.out.println("TOTAL DE ERRORES SINTÁCTICOS Y LÉXICOS ENCONTRADOS: " + errors.size());
            return null; 
        } else if (currentToken.tipo.equals(Token.Tipo.EOF)) {
            System.out.println("\n--- ARBOL SINTÁCTICO ---");
            for (String line : derivationTrace) {
                System.out.println(line);
            }
            System.out.println("\n--- Analisis completado con exito. ---");
        } 
        return root;
    }
    
    private ASTNode P() {
        printProduction("P -> D S <eof>");
        ASTNode pNode = new ASTNode("P");
        pNode.children.add(D());
        pNode.children.add(S());
        match(Token.Tipo.EOF);
        return pNode;
    }

    private ASTNode D() {
        // **CASO ℇ (Cadena nula):** La producción debe elegirse si el lookahead (currentToken) 
        // es cualquiera de los tokens que inician S, que son el FIRST set de S.
        if (currentToken.tipo == Token.Tipo.L_BRACE || currentToken.tipo == Token.Tipo.IF ||
            currentToken.tipo == Token.Tipo.WHILE || currentToken.tipo == Token.Tipo.INPUT ||
            currentToken.tipo == Token.Tipo.OUTPUT) 
        {
            printProduction("D -> ℇ");
            return new ASTNode("EmptyD");
        }

        if (currentToken.tipo == Token.Tipo.INT || currentToken.tipo == Token.Tipo.FLOAT) {
            printProduction("D -> (int | float) id ; D");
            ASTNode dNode = new ASTNode("D");
            
            Token.Tipo type = currentToken.tipo;
            dNode.children.add(new ASTNode("Type", currentToken.lexema));
            match(type); // Consume tipo
            
            dNode.children.add(new ASTNode("Id", currentToken.lexema));
            match(Token.Tipo.ID); // Consume ID
            
            match(Token.Tipo.SEMICOLON); // Consume ;
            
            dNode.children.add(D()); // Llamada recursiva
            return dNode;   
            
        } else {
            // ERROR CRÍTICO en D.
            error("Se esperaba 'int' o 'float' para declarar, o el inicio de una sentencia (IF, WHILE, INPUT, OUTPUT, {).");
            
            // Recuperación de errores: Saltamos hasta encontrar un token de sincronización de D o S.
            while (currentToken.tipo != Token.Tipo.EOF && 
                   currentToken.tipo != Token.Tipo.L_BRACE && 
                   currentToken.tipo != Token.Tipo.INT && 
                   currentToken.tipo != Token.Tipo.FLOAT &&
                   currentToken.tipo != Token.Tipo.IF &&
                   currentToken.tipo != Token.Tipo.WHILE &&
                   currentToken.tipo != Token.Tipo.INPUT &&
                   currentToken.tipo != Token.Tipo.OUTPUT) 
            {
                currentToken = scanner.nextToken();
            }

            return D();
        }
    }

    private ASTNode S() {
        ASTNode sNode = new ASTNode("S");
        switch (currentToken.tipo) {
            case IF:
                printProduction("S -> if E then S else S");
                match(Token.Tipo.IF);
                sNode.children.add(E()); 
                
                // Recuperación de errores: Si 'then' falta.
                if (currentToken.tipo != Token.Tipo.THEN) {
                     error("Se esperaba 'then' después de la expresión condicional (E).");
                     // Simulamos que 'then' fue consumido para continuar con la estructura.
                } else {
                    match(Token.Tipo.THEN);
                }
                
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
                // Error: Sentencia S inválida.
                error("Sentencia S inválida. Se esperaba IF, WHILE, '{', INPUT, u OUTPUT.");
                // Retornamos un nodo de error, dejando que el llamador (P o L) maneje la recuperación.
                return new ASTNode("ErrorS");
        }
        return sNode;
    }

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
             // Recuperación de errores: Saltamos hasta el siguiente ; o } para intentar recuperar el bloque.
             while (currentToken.tipo != Token.Tipo.EOF && 
                    currentToken.tipo != Token.Tipo.SEMICOLON && 
                    currentToken.tipo != Token.Tipo.R_BRACE) 
             {
                 currentToken = scanner.nextToken();
             }
             if (currentToken.tipo == Token.Tipo.SEMICOLON || currentToken.tipo == Token.Tipo.R_BRACE) {
                 return L();
             }
             return new ASTNode("ErrorL");
        }
    }
    

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
            // Avanzamos el token, ya que el error será capturado por S o L.
            currentToken = scanner.nextToken(); 
        }
        return eNode;
    }
    
    private ASTNode E_prime(String baseType) {
        if (currentToken.tipo == Token.Tipo.EQ_EQ) {
            printProduction("E' -> == " + (baseType.equals("NUM") ? "num" : "id"));
            ASTNode primeNode = new ASTNode("Comparison");
            match(Token.Tipo.EQ_EQ);
            
            Token.Tipo expectedType = baseType.equals("NUM") ? Token.Tipo.NUM : Token.Tipo.ID;
            match(expectedType);
            return primeNode;
        } else {
            return new ASTNode("EmptyE'");
        }
    }
}