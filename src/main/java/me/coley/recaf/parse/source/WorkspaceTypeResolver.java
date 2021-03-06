package me.coley.recaf.parse.source;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionClassDeclaration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import javassist.*;
import me.coley.recaf.workspace.Workspace;

import java.io.*;

import static com.github.javaparser.symbolsolver.javassistmodel.JavassistFactory.toTypeDeclaration;

/**
 * Type resolver that uses a Recaf workspace as a classpath.
 *
 * @author Matt
 */
public class WorkspaceTypeResolver implements TypeSolver {
	private final TypeSolver childSolver = new ReflectionTypeSolver(false);
	private final ClassPool classPool = new ClassPool(false);
	private Workspace workspace;
	private TypeSolver parent;

	/**
	 * @param workspace
	 * 		Workspace to pull classes from.
	 */
	public WorkspaceTypeResolver(Workspace workspace) {
		this.workspace = workspace;
		for (String name : workspace.getClassNames())
			classPool.insertClassPath(new ByteArrayClassPath(name.replace("/", "."), workspace.getRawClass(name)));
		classPool.appendSystemPath();
	}

	@Override
	public TypeSolver getParent() {
		return this.parent;
	}

	@Override
	public void setParent(TypeSolver parent) {
		this.parent = parent;
	}

	@Override
	public SymbolReference<ResolvedReferenceTypeDeclaration> tryToSolveType(String name) {
		try {
			// The default resolve seems to infinite loop on Object, but this doesn't.
			// IDK, JavaParser is weird.
			if (name.equals("java.lang.Object"))
				return SymbolReference.solved(new ReflectionClassDeclaration(Object.class, getRoot()));
			String internal = name.replace('.','/');
			if(workspace.hasClass(internal)) {
				InputStream is = new ByteArrayInputStream(workspace.getRawClass(internal));
				ResolvedReferenceTypeDeclaration dec = toTypeDeclaration(classPool.makeClass(is), getRoot());
				SymbolReference<ResolvedReferenceTypeDeclaration> solved = SymbolReference.solved(dec);
				if (solved.isSolved())
					return solved;
			}
		} catch(IOException ex) {
			throw new IllegalStateException("Failed to resolve type: " + name, ex);
		}
		return childSolver.tryToSolveType(name);
	}
}
