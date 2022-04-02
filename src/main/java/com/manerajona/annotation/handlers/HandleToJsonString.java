package com.manerajona.annotation.handlers;

import com.google.auto.service.AutoService;
import com.manerajona.annotation.ToJsonString;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import lombok.ConfigurationKeys;
import lombok.core.AnnotationValues;
import lombok.core.configuration.CallSuperType;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil;

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.javac.Javac.CTC_PLUS;
import static lombok.javac.handlers.JavacHandlerUtil.createRelevantNonNullAnnotation;
import static lombok.javac.handlers.JavacHandlerUtil.deleteAnnotationIfNeccessary;
import static lombok.javac.handlers.JavacHandlerUtil.deleteImportFromCompilationUnit;
import static lombok.javac.handlers.JavacHandlerUtil.genJavaLangTypeRef;
import static lombok.javac.handlers.JavacHandlerUtil.isClassOrEnum;
import static lombok.javac.handlers.JavacHandlerUtil.isDirectDescendantOfObject;
import static lombok.javac.handlers.JavacHandlerUtil.methodExists;
import static lombok.javac.handlers.JavacHandlerUtil.recursiveSetGeneratedBy;

@AutoService(JavacAnnotationHandler.class)
public class HandleToJsonString extends JavacAnnotationHandler<ToJsonString> {

    @Override
    public void handle(AnnotationValues<ToJsonString> annotation, JCTree.JCAnnotation ast, JavacNode annotationNode) {
        handleFlagUsage(annotationNode, ConfigurationKeys.TO_STRING_FLAG_USAGE, "@ToString");

        deleteAnnotationIfNeccessary(annotationNode, ToJsonString.class);

        //remove import
        deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");

        Boolean callSuper = annotation.getInstance().callSuper();
        if (!annotation.isExplicit("callSuper")) callSuper = null;

        boolean isSecret = annotation.getInstance().isSecret();
        if (!annotation.isExplicit("isSecret")) isSecret = false;

        generateToJsonString(annotationNode.up(), annotationNode, callSuper, isSecret);
    }

    public void generateToJsonString(JavacNode typeNode, JavacNode source, Boolean callSuper, Boolean isSecret) {

        if (!isClassOrEnum(typeNode)) {
            source.addError("@ToJsonString is only supported on a class or enum.");
            return;
        }

        switch (methodExists("toString", typeNode, 0)) {
            case NOT_EXISTS:
                if (callSuper == null) {
                    if (!isDirectDescendantOfObject(typeNode)) {
                        CallSuperType cst = typeNode.getAst().readConfiguration(ConfigurationKeys.TO_STRING_CALL_SUPER);
                        if (cst == null) cst = CallSuperType.SKIP;
                        switch (cst) {
                            default:
                            case SKIP:
                                break;
                            case WARN:
                                source.addWarning("Generating toString implementation but without a call to superclass, even though this class does not extend java.lang.Object. If this is intentional, add '@ToJsonString(callSuper=false)' to your type.");
                                break;
                        }
                    }
                }

                typeNode.get().accept(new TreeTranslator() {
                    @Override
                    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                        JCTree.JCMethodDecl method = createToJsonStringMethodDecl(jcClassDecl.defs, typeNode, source, isSecret);
                        JavacHandlerUtil.injectMethod(typeNode, method);
                    }
                });
                break;
            case EXISTS_BY_LOMBOK:
                break;
            default:
            case EXISTS_BY_USER:
                source.addWarning("Not generating toString(): A method with that name already exists");
                break;
        }
    }

    static JCTree.JCMethodDecl createToJsonStringMethodDecl(List<JCTree> defs, JavacNode typeNode, JavacNode source, Boolean isSecret) {

        JavacTreeMaker maker = typeNode.getTreeMaker();

        JCTree.JCModifiers mods = maker.Modifiers(Flags.PUBLIC);

        JCTree.JCExpression returnType = genJavaLangTypeRef(typeNode, "String");

        JCTree.JCExpression expression = maker.Literal("{");

        boolean first = true;
        for (JCTree tree : defs) {

            if (tree.getKind().equals(Tree.Kind.VARIABLE)) {
                JCTree.JCVariableDecl var = (JCTree.JCVariableDecl) tree;

                final boolean typeString = var.vartype.toString().contains("String");

                if (first) {
                    expression = maker.Binary(CTC_PLUS, expression, maker.Literal("\"" + var.name + "\":"));
                    first = false;
                } else {
                    expression = maker.Binary(CTC_PLUS, expression, maker.Literal(",\"" + var.name + "\":"));
                }
                if (typeString) {
                    expression = maker.Binary(CTC_PLUS, expression, maker.Literal("\""));
                }
                if (isSecret) {
                    expression = maker.Binary(CTC_PLUS, expression, maker.Literal("?"));
                } else {
                    expression = maker.Binary(CTC_PLUS, expression, maker.Ident(var));
                }
                if (typeString) {
                    expression = maker.Binary(CTC_PLUS, expression, maker.Literal("\""));
                }
            }
        }
        expression = maker.Binary(CTC_PLUS, expression, maker.Literal("}"));

        JCTree.JCStatement returnStatement = maker.Return(expression);

        JCTree.JCBlock body = maker.Block(0, List.of(returnStatement));

        JCTree.JCMethodDecl methodDef = maker.MethodDef(mods, typeNode.toName("toString"), returnType,
                List.nil(), List.nil(), List.nil(), body, null);
        createRelevantNonNullAnnotation(typeNode, methodDef);
        return recursiveSetGeneratedBy(methodDef, source);
    }

}
