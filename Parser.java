public class Parser {
    private final Scanner scanner;
    private Token currentToken;

    public Parser(Scanner scanner) {
        this.scanner = scanner;
    }
    
    //manejo de errores y tokens 
    
    private void error(String message) {
        System.err.println("Error de Sintaxis en línea " + currentToken.linea + " (" + currentToken.lexema + "): " + message);
        //detiene la copilacion despues de que se detecta el error
        System.exit(1); 
    }

    private void match(Token.Tipo expectedType) {
        if (currentToken.tipo == Token.Tipo.ERROR) {
             System.err.println(" Deteniendo por Error Léxico previo.");
             System.exit(1);
        }
        
        if (currentToken.tipo.equals(expectedType)) {
            System.out.println("-> Consumido: " + currentToken);
            currentToken = scanner.nextToken();
        } else {
            error("Se esperaba '" + expectedType + "' pero se encontró '" + currentToken.tipo + "'");
        }
    }
    
    // --- Método de inicio ---
    
    public ASTNode parse() {
        System.out.println("\n--- Iniciando Compilación ---");
        currentToken = scanner.nextToken(); // Obtener el primer token
        ASTNode root = P();

        if (currentToken.tipo.equals(Token.Tipo.EOF)) {
            System.out.println(" Análisis sintáctico completado con éxito.");
            return root;
        } else {
            error("Se esperaban declaraciones/sentencias adicionales o <eof>.");
            return null;
        }
    }
    
    // --- Funciones del Parser Descendente Recursivo ---

    // P -> D S <eof>
    private ASTNode P() {
        ASTNode pNode = new ASTNode("Program");
        System.out.println("Analizando P -> D S <eof>");
        pNode.children.add(D());
        pNode.children.add(S());
        match(Token.Tipo.EOF);
        return pNode;
    }

    // D -> (int | float) id ; D | ε
    private ASTNode D() {
        // PRIMEROS(D): {INT, FLOAT}
        if (currentToken.tipo == Token.Tipo.INT || currentToken.tipo == Token.Tipo.FLOAT) {
            ASTNode dNode = new ASTNode("Declaration");
            
            dNode.children.add(new ASTNode("Type", currentToken.lexema));
            match(currentToken.tipo); // Consumir 'int' o 'float'

            dNode.children.add(new ASTNode("Id", currentToken.lexema));
            match(Token.Tipo.ID); // Consumir 'id'

            match(Token.Tipo.FINAL_DE_SENTENCIA); 
            dNode.children.add(D()); // Llamada recursiva
            return dNode;
        } else {
            // D -> ε (cadena nula). Se llega aquí si el lookahead está en SIGUIENTES(D): {IF, WHILE, L_BRACE, INPUT, OUTPUT, EOF}
            return new ASTNode("EmptyDeclaration");
        }
    }

    // S à if E then S else S | while E do S | { S L | input E | output E
    private ASTNode S() {
        ASTNode sNode = new ASTNode("Statement");
        
        switch (currentToken.tipo) {
            case IF:
                sNode.type = "IfStatement";
                match(Token.Tipo.IF);
                sNode.children.add(E()); // Condición
                match(Token.Tipo.THEN);
                sNode.children.add(S()); // Cuerpo THEN
                match(Token.Tipo.ELSE);
                sNode.children.add(S()); // Cuerpo ELSE
                break;
            case WHILE:
                sNode.type = "WhileStatement";
                match(Token.Tipo.WHILE);
                sNode.children.add(E()); // Condición
                match(Token.Tipo.DO);
                sNode.children.add(S()); // Cuerpo DO
                break;
            case L_BRACE:
                sNode.type = "BlockStatement";
                match(Token.Tipo.L_BRACE);
                sNode.children.add(S()); // Primer statement del bloque
                sNode.children.add(L()); // Resto del bloque
                break;
            case INPUT:
                sNode.type = "InputStatement";
                match(Token.Tipo.INPUT);
                sNode.children.add(E()); // Expresión/Variable
                match(Token.Tipo.FINAL_DE_SENTENCIA); 
                break;
            case OUTPUT:
                sNode.type = "OutputStatement";
                match(Token.Tipo.OUTPUT);
                sNode.children.add(E()); // Expresión
                match(Token.Tipo.FINAL_DE_SENTENCIA); 
                break;
            default:
                error("Sentencia inválida. Se esperaba IF, WHILE, '{', INPUT, u OUTPUT.");
        }
        return sNode;
    }

    // L à } | ; S L
    private ASTNode L() {
        if (currentToken.tipo == Token.Tipo.FIN_BLOQUE) {
            match(Token.Tipo.FIN_BLOQUE);
            return new ASTNode("EndBlock");
        } 
        // Reemplazar la condición Token.Tipo.SEMICOLON
        else if (currentToken.tipo == Token.Tipo.FINAL_DE_SENTENCIA) { 
            ASTNode lNode = new ASTNode("StatementList");
            // Reemplazar match(Token.Tipo.SEMICOLON)
            match(Token.Tipo.FINAL_DE_SENTENCIA); 
            lNode.children.add(S());
            lNode.children.add(L());
            return lNode;
        } else {
             error("Se esperaba FINAL_DE_SENTENCIA (;) o '}' dentro del bloque de sentencias.");
             return null;
        }
    }
    
    // E -> num E_num' | id E_id'
    private ASTNode E() {
        ASTNode eNode = new ASTNode("Expression");
        if (currentToken.tipo == Token.Tipo.NUM) {
            eNode.children.add(new ASTNode("Num", currentToken.lexema));
            match(Token.Tipo.NUM);
            eNode.children.add(E_num_prime());
        } else if (currentToken.tipo == Token.Tipo.ID) {
            eNode.children.add(new ASTNode("Id", currentToken.lexema));
            match(Token.Tipo.ID);
            eNode.children.add(E_id_prime());
        } else {
            error("Expresión inválida. Se esperaba un número (num) o un identificador (id).");
        }
        return eNode;
    }
    
    // E_num' -> == num | ε
    private ASTNode E_num_prime() {
        if (currentToken.tipo == Token.Tipo.EQ_EQ) {
            ASTNode primeNode = new ASTNode("Comparison");
            match(Token.Tipo.EQ_EQ);
            primeNode.children.add(new ASTNode("Num", currentToken.lexema));
            match(Token.Tipo.NUM);
            return primeNode;
        } else {
            // E_num' -> ε. Se llega aquí si el lookahead está en SIGUIENTES(E): { THEN, DO, SEMICOLON, R_BRACE }
            return new ASTNode("Empty");
        }
    }
    
    // E_id' -> == id | ε
    private ASTNode E_id_prime() {
        if (currentToken.tipo == Token.Tipo.EQ_EQ) {
            ASTNode primeNode = new ASTNode("Comparison");
            match(Token.Tipo.EQ_EQ);
            primeNode.children.add(new ASTNode("Id", currentToken.lexema));
            match(Token.Tipo.ID);
            return primeNode;
        } else {
            return new ASTNode("Empty");
        }
    }
}