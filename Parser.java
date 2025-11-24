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
    
    //

    private void error(String message) {
        //registrador de erroes
        String errorMessage = String.format("Error de Sintaxis en línea %d (%s): %s", 
                                            currentToken.linea, currentToken.lexema, message);
        errors.add(errorMessage); 
        System.err.println(errorMessage);
    }
    //mostrar regla gramarical en uso 
    private void printProduction(String rule) {
        derivationTrace.add("--> APLICANDO REGLA: " + rule);
    }

    private void match(Token.Tipo expectedType) {
        if (currentToken.tipo == expectedType) {
            derivationTrace.add("    |-> Token Consumido : " + currentToken.lexema + " (" + expectedType + ")");
            currentToken = scanner.nextToken();
        } else {
            error("Se esperaba '" + expectedType + "' pero se encontró '" + currentToken.tipo + "'");
            //no avanza el token en match() para que la recuperacion se 
            // maneje en el metodo de la regla
        }
    }

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
        if (currentToken.tipo == Token.Tipo.LLAVE_IZQ || currentToken.tipo == Token.Tipo.IF ||
            currentToken.tipo == Token.Tipo.WHILE || currentToken.tipo == Token.Tipo.INPUT ||
            currentToken.tipo == Token.Tipo.OUTPUT) 
        {
            printProduction("D -> ℇ");
            return new ASTNode("EmptyD");
        }

        if (currentToken.tipo == Token.Tipo.INT || currentToken.tipo == Token.Tipo.FLOAT) {
            printProduction("D -> (int | float) id ; D");
            ASTNode dNode = new ASTNode("D");
            //caso de declaracion normal
            Token.Tipo type = currentToken.tipo;
            dNode.children.add(new ASTNode("Type", currentToken.lexema));
            match(type); //consume tipo
            
            dNode.children.add(new ASTNode("Id", currentToken.lexema));
            match(Token.Tipo.ID); // Consume ID
            
            match(Token.Tipo.PUNTO_COMA); //consume ;
            
            dNode.children.add(D()); //llamada recursiva
            return dNode; 
            
        } else {
            // ERROR CRiTICO en D.
            error("Se esperaba 'int' o 'float' para declarar, o el inicio de una sentencia (IF, WHILE, INPUT, OUTPUT, {).");
            
            //recuperacion de errores: Saltamos hasta encontrar un token de sincronización de D o S.
            while (currentToken.tipo != Token.Tipo.EOF && 
                   currentToken.tipo != Token.Tipo.LLAVE_IZQ && 
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
                     //simulamos que 'then' fue consumido para continuar con la estructura
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
            case LLAVE_IZQ:
                printProduction("S -> { S L");
                match(Token.Tipo.LLAVE_IZQ);
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
                //error:sentencia S inválida.
                error("Sentencia S inválida. Se esperaba IF, WHILE, '{', INPUT, u OUTPUT.");
                //retorna un nodo de error, dejando que el llamador (P o L) maneje la recuperación.
                return new ASTNode("ErrorS");
        }
        return sNode;
    }

    private ASTNode L() {
        if (currentToken.tipo == Token.Tipo.LLAVE_DER) {
            printProduction("L -> }");
            match(Token.Tipo.LLAVE_DER);
            return new ASTNode("EndL");
        } else if (currentToken.tipo == Token.Tipo.PUNTO_COMA) {
            printProduction("L -> ; S L");
            ASTNode lNode = new ASTNode("L");
            match(Token.Tipo.PUNTO_COMA);
            lNode.children.add(S());
            lNode.children.add(L());
            return lNode;
        } else {
        //logica de recuperacion de errores hasta encontrar ; o }
             error("Se esperaba ; o } en la lista L. Encontrado: " + currentToken.tipo);
             //recuperacion de errores: salta hasta el siguiente ; o } para intentar recuperar el bloque
             while (currentToken.tipo != Token.Tipo.EOF && 
                    currentToken.tipo != Token.Tipo.PUNTO_COMA && 
                    currentToken.tipo != Token.Tipo.LLAVE_DER) 
             {
                 currentToken = scanner.nextToken();
             }
             if (currentToken.tipo == Token.Tipo.PUNTO_COMA || currentToken.tipo == Token.Tipo.LLAVE_DER) {
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
            error("Expresión E invalida. Se esperaba NUM o ID.");
            //avanzamos el token, ya que el error sera capturado por S o L.
            currentToken = scanner.nextToken(); 
        }
        return eNode;
    }
    
    private ASTNode E_prime(String baseType) {
        if (currentToken.tipo == Token.Tipo.EQ_EQ) {
            printProduction("E -> == " + (baseType.equals("NUM") ? "num" : "id"));
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