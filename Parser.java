import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final Scanner scanner;
    private Token currentToken;
    private final List<String> errors = new ArrayList<>(); // <-- LISTA PARA RECOPILAR TODOS LOS ERRORES

    public Parser(Scanner scanner) {
        this.scanner = scanner;
    }
    
    // --- Manejo de Errores y Recuperación (Modo Pánico) ---
    
    private void error(String message) {
        String errorMessage = String.format("❌ Error de Sintaxis en línea %d (%s): %s", 
                                            currentToken.linea, currentToken.lexema, message);
        errors.add(errorMessage); 
        System.err.println(errorMessage);
    }
    
    private void synchronize(Token.Tipo expectedType) {
        // Tokens que sirven como puntos seguros para reanudar.
        Token.Tipo[] synchronizationSet = {
            Token.Tipo.SEMICOLON, Token.Tipo.R_BRACE, Token.Tipo.EOF,
            Token.Tipo.IF, Token.Tipo.WHILE, Token.Tipo.INPUT, Token.Tipo.OUTPUT,
            Token.Tipo.INT, Token.Tipo.FLOAT // Para sincronizar declaraciones
        };
        
        while (currentToken.tipo != Token.Tipo.EOF) {
            // Si el token actual es uno de sincronización, salimos.
            for (Token.Tipo tipo : synchronizationSet) {
                if (currentToken.tipo == tipo) {
                    return; 
                }
            }
            // Saltamos el token inválido
            currentToken = scanner.nextToken(); 
        }
    }

    private void match(Token.Tipo expectedType) {
        if (currentToken.tipo == Token.Tipo.ERROR) {
             errors.add("❌ Error Léxico detectado. Se cancela el análisis.");
             return;
        }
        
        if (currentToken.tipo.equals(expectedType)) {
            currentToken = scanner.nextToken();
        } else {
            error("Se esperaba '" + expectedType + "' pero se encontró '" + currentToken.tipo + "'");
            
            // Lógica de Recuperación:
            synchronize(expectedType);
            
            // Consumir el token si el salto nos llevó al lugar correcto.
            if (currentToken.tipo.equals(expectedType)) {
                 currentToken = scanner.nextToken();
            }
        }
    }
    
    // --- Método de inicio y Reglas Gramaticales ---
    
    public ASTNode parse() {
        currentToken = scanner.nextToken(); 
        ASTNode root = P();

        if (!errors.isEmpty()) {
            System.out.println("\n⚠️ ANÁLISIS FINALIZADO CON ERRORES. EL ÁRBOL NO SERÁ GENERADO.");
            System.out.println("TOTAL DE ERRORES SINTÁCTICOS Y LÉXICOS ENCONTRADOS: " + errors.size());
            return null; // <-- NO DEVUELVE EL AST
        } else if (currentToken.tipo.equals(Token.Tipo.EOF)) {
            System.out.println("\n✅ Análisis sintáctico completado con éxito.");
        } else {
            error("Error: Código extra después del fin del programa.");
            return null;
        }
        return root;
    }
    
    // P -> D S <eof> [cite: 8]
    private ASTNode P() {
        ASTNode pNode = new ASTNode("Program");
        pNode.children.add(D());
        pNode.children.add(S());
        match(Token.Tipo.EOF);
        return pNode;
    }

    // D -> (int | float) id ; D | ℇ [cite: 9, 10]
    private ASTNode D() {
        if (currentToken.tipo == Token.Tipo.INT || currentToken.tipo == Token.Tipo.FLOAT) {
            ASTNode dNode = new ASTNode("Declaration");
            dNode.children.add(new ASTNode("Type", currentToken.lexema));
            match(currentToken.tipo); 
            dNode.children.add(new ASTNode("Id", currentToken.lexema));
            match(Token.Tipo.ID); 
            match(Token.Tipo.SEMICOLON); 
            dNode.children.add(D()); 
            return dNode;
        } else {
            return new ASTNode("EmptyDeclaration"); // Regla ℇ
        }
    }

    // S -> if E then S else S | while E do S | { S L | input E | output E [cite: 11-15]
    private ASTNode S() {
        ASTNode sNode; 
        
        switch (currentToken.tipo) {
            case IF:
                sNode = new ASTNode("IfStatement"); 
                match(Token.Tipo.IF);
                sNode.children.add(E()); 
                match(Token.Tipo.THEN);
                sNode.children.add(S()); 
                match(Token.Tipo.ELSE); // ELSE obligatorio por gramática
                sNode.children.add(S()); 
                break;
            case WHILE:
                sNode = new ASTNode("WhileStatement"); 
                match(Token.Tipo.WHILE);
                sNode.children.add(E()); 
                match(Token.Tipo.DO);
                sNode.children.add(S()); 
                break;
            case L_BRACE:
                sNode = new ASTNode("BlockStatement"); 
                match(Token.Tipo.L_BRACE);
                sNode.children.add(S()); 
                sNode.children.add(L()); 
                break;
            case INPUT:
                sNode = new ASTNode("InputStatement"); 
                match(Token.Tipo.INPUT);
                sNode.children.add(E());
                // NO hay SEMICOLON aquí, debe ser consumido por L() 
                break;
            case OUTPUT:
                sNode = new ASTNode("OutputStatement"); 
                match(Token.Tipo.OUTPUT);
                sNode.children.add(E()); 
                // NO hay SEMICOLON aquí, debe ser consumido por L()
                break;
            default:
                error("Sentencia inválida. Se esperaba IF, WHILE, '{', INPUT, u OUTPUT.");
                synchronize(null); 
                return new ASTNode("ErrorStatement");
        }
        return sNode;
    }

    // L -> } | ; S L [cite: 16, 17]
    private ASTNode L() {
        if (currentToken.tipo == Token.Tipo.R_BRACE) {
            match(Token.Tipo.R_BRACE);
            return new ASTNode("EndBlock");
        } else if (currentToken.tipo == Token.Tipo.SEMICOLON) {
            ASTNode lNode = new ASTNode("StatementList");
            match(Token.Tipo.SEMICOLON);
            lNode.children.add(S());
            lNode.children.add(L());
            return lNode;
        } else {
             error("Se esperaba SEMICOLON (;) o '}' dentro del bloque de sentencias.");
             synchronize(Token.Tipo.R_BRACE); 
             if (currentToken.tipo == Token.Tipo.R_BRACE) {
                 return L();
             }
             return new ASTNode("ErrorList");
        }
    }
    
    // E -> num == num | id == id | num | id [cite: 18-21]
    private ASTNode E() {
        ASTNode eNode = new ASTNode("Expression");
        if (currentToken.tipo == Token.Tipo.NUM) {
            eNode.children.add(new ASTNode("Num", currentToken.lexema));
            match(Token.Tipo.NUM);
            eNode.children.add(E_prime(Token.Tipo.NUM));
        } else if (currentToken.tipo == Token.Tipo.ID) {
            eNode.children.add(new ASTNode("Id", currentToken.lexema));
            match(Token.Tipo.ID);
            eNode.children.add(E_prime(Token.Tipo.ID));
        } else {
            error("Expresión inválida. Se esperaba un número (NUM) o un identificador (ID).");
        }
        return eNode;
    }
    
    // E' -> == Tipo | ε
    private ASTNode E_prime(Token.Tipo expectedType) {
        if (currentToken.tipo == Token.Tipo.EQ_EQ) {
            ASTNode primeNode = new ASTNode("Comparison");
            match(Token.Tipo.EQ_EQ);
            
            String nodeType = (expectedType == Token.Tipo.NUM) ? "Num" : "Id";
            primeNode.children.add(new ASTNode(nodeType, currentToken.lexema));
            
            match(expectedType);
            return primeNode;
        } else {
            return new ASTNode("Empty");
        }
    }
}