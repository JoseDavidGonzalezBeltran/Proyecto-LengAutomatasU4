import java.util.ArrayList;
import java.util.List;

public class ASTNode {
    public String type; 
    public final String value; 
    public final List<ASTNode> children;

    public ASTNode(String type) {
        this.type = type;
        this.value = null;
        this.children = new ArrayList<>();
    }

    public ASTNode(String type, String value) {
        this.type = type;
        this.value = value;
        this.children = new ArrayList<>();
    }

    /** Muestra la estructura del AST usando el formato TokenV. */
    public void display(int depth) {
        String indent = "│   ".repeat(depth);
        String prefix;
        String content;

        if (value != null) {
            // Nodo Terminal (Token)
            prefix = "└── [TOKEN: " + type + "] ";
            content = "TokenV: " + value; 
        } else {
            // Nodo No Terminal (Producción)
            prefix = "├── (NODE: " + type + ") ";
            content = "PRODUCTION";
        }
        
        System.out.println(indent + prefix + content);

        for (int i = 0; i < children.size(); i++) {
            ASTNode child = children.get(i);
            child.display(depth + 1);
        }
    }
}