package partie2.parser;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.Map;

public class MyParser {

    private static final String jrePath = "C:\\Program Files\\Java\\jdk-17.0.2\\lib\\jrt-fs.jar";

    public static CompilationUnit parseSource(char[] classSource) {
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        Map options = JavaCore.getOptions();
        parser.setBindingsRecovery(true);
        parser.setCompilerOptions(options);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setUnitName("");
        String[] sources = { "C:\\Users\\senio\\IdeaProjects\\ERL\\A_Rendre\\TP1\\src\\main\\java\\project\\src" };
        String[] classpath = {jrePath};
        parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);
        parser.setSource(classSource);
        return (CompilationUnit) parser.createAST(null);
    }
}
