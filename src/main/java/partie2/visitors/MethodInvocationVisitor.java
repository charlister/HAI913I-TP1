package partie2.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MethodInvocationVisitor extends ASTVisitor {
    Set<MethodInvocation> methodInvocations;

    public MethodInvocationVisitor() {
        methodInvocations = new HashSet<>();
    }

    @Override
    public boolean visit(MethodInvocation node) {
        /*Optional<MethodInvocation> methodInvocation = methodInvocations
                .stream()
                .filter(invocation ->
                                invocation.getExpression() != null
                                &&
                                invocation.getExpression().toString().equals(node.getExpression().toString())
                                &&
                                invocation.getName().toString().equals(node.getName().toString())
                                &&
                                invocation.typeArguments().equals(node.typeArguments())
                )
                .findFirst();
        if (methodInvocation.isEmpty())*/
        methodInvocations.add(node);
        return super.visit(node);
    }

    public Set<MethodInvocation> getMethodInvocations() {
        return methodInvocations;
    }
}
