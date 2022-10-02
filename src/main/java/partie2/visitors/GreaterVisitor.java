package partie2.visitors;

import org.eclipse.jdt.core.dom.*;

public class GreaterVisitor extends ASTVisitor {
    private int nbLinesOfCode;

    public GreaterVisitor() {
        nbLinesOfCode = 0;
    }

    public int getNbLinesOfCode() {
        return nbLinesOfCode;
    }

    public boolean visit(AssertStatement node) {
        nbLinesOfCode++;
        return super.visit(node);
    }

    public boolean visit(BreakStatement node) {
        nbLinesOfCode++;
        return super.visit(node);
    }

    public boolean visit(ConstructorInvocation node) {
        nbLinesOfCode++;
        return super.visit(node);
    }

    public boolean visit(ContinueStatement node) {
        nbLinesOfCode++;
        return super.visit(node);
    }

    public boolean visit(DoStatement node) {
        nbLinesOfCode++;
        return super.visit(node);
    }

    public boolean visit(EmptyStatement node) {
        nbLinesOfCode++;
        return super.visit(node);
    }

    public boolean visit(EnhancedForStatement node) {
        nbLinesOfCode++;
        return super.visit(node);
    }

    public boolean visit(ExpressionStatement node){
        nbLinesOfCode++;
        return super.visit(node);
    }

    public boolean visit(ForStatement node) {
        nbLinesOfCode++;
        return super.visit(node);
    }

    public boolean visit(FieldDeclaration node) {
        nbLinesOfCode++;
        return true;
    }

    public boolean visit(IfStatement node) {
        nbLinesOfCode++;
        return true;
    }

    public boolean visit(ImportDeclaration node) {
        nbLinesOfCode++;
        return super.visit(node);
    }

    public boolean visit(LabeledStatement node) {
        nbLinesOfCode++;
        return true;
    }

    public boolean visit(MethodDeclaration node) {
        nbLinesOfCode++;
        return super.visit(node);
    }

    public boolean visit(SuperConstructorInvocation node) {
        nbLinesOfCode++;
        return super.visit(node);
    }

    public boolean visit(PackageDeclaration node) {
        nbLinesOfCode++;
        return super.visit(node);
    }

    public boolean visit(ReturnStatement node) {
        nbLinesOfCode++;
        return true;
    }

    public boolean visit(SwitchCase node) {
        nbLinesOfCode++;
        return true;
    }

    public boolean visit(SwitchStatement node) {
        nbLinesOfCode++;
        return true;
    }

    public boolean visit(ThrowStatement node) {
        nbLinesOfCode++;
        return true;
    }

    public boolean visit(TryStatement node) {
        nbLinesOfCode++;
        return true;
    }

    public boolean visit(TypeDeclaration node) {
        nbLinesOfCode++;
        return super.visit(node);
    }

    public boolean visit(SynchronizedStatement node) {
        nbLinesOfCode++;
        return true;
    }

    public boolean visit(TypeDeclarationStatement node) {
        nbLinesOfCode++;
        return true;
    }

    public boolean visit(VariableDeclarationStatement node) {
        nbLinesOfCode++;
        return true;
    }

    public boolean visit(WhileStatement node) {
        nbLinesOfCode++;
        return true;
    }
}
