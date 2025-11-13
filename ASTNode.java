import java.util.ArrayList;
import java.util.List;

public class ASTNode {
    // Se mantiene 'type' para la categoría del nodo (Program, Statement, etc.)
    public String type; 
    // Se mantiene 'value' para el lexema/valor.
    public final String value; 
    public final List<ASTNode> children;

    // Constructor para nodos de Producción (Non-Terminal)
    public ASTNode(String type) {
        this.type = type;
        this.value = null;
        this.children = new ArrayList<>();
    }

    // Constructor para nodos de Token (Terminal)
    public ASTNode(String type, String value) {
        this.type = type;
        this.value = value;
        this.children = new ArrayList<>();
    }

    /**
     * Muestra la estructura del AST en pre-orden, usando Sangría (depth) para indicar la Jerarquía.
     */
    public void display(int depth) {
        String indent = "│   ".repeat(depth);
        
        String prefix;
        String content;

        if (value != null) {
            // Nodo Terminal (e.g., ID, NUM, IF)
            prefix = "└── [TOKEN: " + type + "] ";
            // *** MODIFICACIÓN SOLICITADA AQUÍ ***
            content = "TokenV: " + value; 
            // **********************************
        } else {
            // Nodo No Terminal (e.g., Program, IfStatement)
            prefix = "├── (NODE: " + type + ") ";
            content = "PRODUCTION";
        }
        
        // Imprimir el nodo actual
        System.out.println(indent + prefix + content);

        // Llamada recursiva para los hijos
        for (int i = 0; i < children.size(); i++) {
            ASTNode child = children.get(i);
            child.display(depth + 1);
        }
    }
}