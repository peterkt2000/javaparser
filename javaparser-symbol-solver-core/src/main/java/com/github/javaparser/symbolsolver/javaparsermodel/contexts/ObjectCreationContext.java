/*
 * Copyright 2016 Federico Tomassetti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.javaparser.symbolsolver.javaparsermodel.contexts;

import static com.github.javaparser.symbolsolver.javaparser.Navigator.requireParentNode;

import java.util.List;
import java.util.function.BiFunction;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFactory;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;

/**
 * @author Federico Tomassetti
 */
public class ObjectCreationContext extends AbstractJavaParserContext<ObjectCreationExpr> {

    public ObjectCreationContext(ObjectCreationExpr wrappedNode, TypeSolver typeSolver) {
        super(wrappedNode, typeSolver);
    }

    @Override
    public SymbolReference<ResolvedTypeDeclaration> solveType(String name, TypeSolver typeSolver) {
        if (wrappedNode.getScope().isPresent()) {
            Expression scope = wrappedNode.getScope().get();
            ResolvedType scopeType = JavaParserFacade.get(typeSolver).getType(scope);
            if (scopeType.isReferenceType()) {
                ResolvedReferenceType referenceType = scopeType.asReferenceType();
                for (ResolvedTypeDeclaration it : referenceType.getTypeDeclaration().internalTypes()) {
                    if (it.getName().equals(name)) {
                        return SymbolReference.solved(it);
                    }
                }
            }
            throw new UnsolvedSymbolException("Unable to solve qualified object creation expression in the context of expression of type " + scopeType.describe());
        }
        return JavaParserFactory.getContext(requireParentNode(wrappedNode), typeSolver).solveType(name, typeSolver);
    }

    @Override
    public SymbolReference<? extends ResolvedValueDeclaration> solveSymbol(String name, TypeSolver typeSolver) {
        return JavaParserFactory.getContext(requireParentNode(wrappedNode), typeSolver).solveSymbol(name, typeSolver);
    }

    @Override
    public SymbolReference<ResolvedMethodDeclaration> solveMethod(String name, List<ResolvedType> argumentsTypes, boolean staticOnly, TypeSolver typeSolver) {
        return JavaParserFactory.getContext(requireParentNode(wrappedNode), typeSolver).solveMethod(name, argumentsTypes, false, typeSolver);
    }

    @Override
    public SymbolReference<? extends ResolvedValueDeclaration> solveLambda(TypeSolver typeSolver,
                                                                           BiFunction<Declaration, Node, Boolean> checkFunction) {
        return JavaParserFactory.getContext(requireParentNode(wrappedNode), typeSolver).solveLambda(typeSolver,
                checkFunction);
    }

}
