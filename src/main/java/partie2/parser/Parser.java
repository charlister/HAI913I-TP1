package partie2.parser;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.Map;

public class Parser {
    public static CompilationUnit parseSource(char[] classSource) {
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        Map options = JavaCore.getOptions();
        parser.setBindingsRecovery(true);
        parser.setCompilerOptions(options);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setSource(classSource);
        return (CompilationUnit) parser.createAST(null);
    }
}
