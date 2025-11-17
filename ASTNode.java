import java.util.ArrayList;
import java.util.List;

public class ASTNode {
    public String type; 
    public final String value; 
    public final List<ASTNode> children;

    // Constructor para Nodos No-Terminales
    public ASTNode(String type) {
        this.type = type;
        this.value = null;
        this.children = new ArrayList<>();
    }

    // Constructor para Nodos Terminales
    public ASTNode(String type, String value) {
        this.type = type;
        this.value = value;
        this.children = new ArrayList<>();
    }

    /** Este método ahora está vacío. El Árbol Sintáctico (AST) ya no se genera. */
    public void display(int depth) {
        // La implementación está vacía para eliminar la salida visual del árbol.
    }
}