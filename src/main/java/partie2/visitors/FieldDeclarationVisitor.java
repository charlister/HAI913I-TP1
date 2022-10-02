package partie2.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import java.util.ArrayList;
import java.util.List;

public class FieldDeclarationVisitor extends ASTVisitor {
    private List<FieldDeclaration> fieldDeclarationList;

    public FieldDeclarationVisitor() {
        this.fieldDeclarationList = new ArrayList<>();
    }

    public boolean visit(FieldDeclaration node) {
        fieldDeclarationList.add(node);
        return super.visit(node);
    }

    @Override
    public void endVisit(PackageDeclaration node) {
        System.out.println("( ATTRIBUTE ) " + node.getName() + " [ VISITED ]");
        super.endVisit(node);
    }

    public List<FieldDeclaration> getFieldDeclarationList() {
        return fieldDeclarationList;
    }
}
