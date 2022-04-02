package com.manerajona.annotation.handlers;

import com.google.auto.service.AutoService;
import com.manerajona.annotation.Dto;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.handlers.HandleConstructor;
import lombok.javac.handlers.HandleEqualsAndHashCode;
import lombok.javac.handlers.HandleGetter;
import lombok.javac.handlers.HandleSetter;

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.javac.handlers.JavacHandlerUtil.deleteAnnotationIfNeccessary;
import static lombok.javac.handlers.JavacHandlerUtil.deleteImportFromCompilationUnit;
import static lombok.javac.handlers.JavacHandlerUtil.isClass;

@AutoService(JavacAnnotationHandler.class)
public class HandleDto extends JavacAnnotationHandler<Dto> {
	private HandleConstructor handleConstructor = new HandleConstructor();
	private HandleGetter handleGetter = new HandleGetter();
	private HandleSetter handleSetter = new HandleSetter();
	private HandleEqualsAndHashCode handleEqualsAndHashCode = new HandleEqualsAndHashCode();
	private HandleToJsonString handleToString = new HandleToJsonString();
	
	@Override
	public void handle(AnnotationValues<Dto> annotation, JCTree.JCAnnotation ast, JavacNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.DATA_FLAG_USAGE, "@Dto");
		
		deleteAnnotationIfNeccessary(annotationNode, Dto.class);
		JavacNode typeNode = annotationNode.up();
		
		if (!isClass(typeNode)) {
			annotationNode.addError("@Dto is only supported on a class.");
			return;
		}

		deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");

		handleConstructor.generateExtraNoArgsConstructor(typeNode, annotationNode);
		handleGetter.generateGetterForType(typeNode, annotationNode, AccessLevel.PUBLIC, true, List.nil());
		handleSetter.generateSetterForType(typeNode, annotationNode, AccessLevel.PUBLIC, true, List.nil(), List.nil());
		handleEqualsAndHashCode.generateEqualsAndHashCodeForType(typeNode, annotationNode);
		handleToString.generateToJsonString(typeNode, annotationNode, null, false);
	}
}

