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
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserSymbolDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarators.VariableSymbolDeclarator;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.SymbolDeclarator;

public class ForechStatementContext extends AbstractJavaParserContext<ForeachStmt> {

    public ForechStatementContext(ForeachStmt wrappedNode, TypeSolver typeSolver) {
        super(wrappedNode, typeSolver);
    }

    @Override
    public SymbolReference<? extends ResolvedValueDeclaration> solveSymbol(String name, TypeSolver typeSolver) {
        if (wrappedNode.getVariable().getVariables().size() != 1) {
            throw new IllegalStateException();
        }
        VariableDeclarator variableDeclarator = wrappedNode.getVariable().getVariables().get(0);
        if (variableDeclarator.getName().getId().equals(name)) {
            return SymbolReference.solved(JavaParserSymbolDeclaration.localVar(variableDeclarator, typeSolver));
        } else {
            if (requireParentNode(wrappedNode) instanceof BlockStmt) {
                return StatementContext.solveInBlock(name, typeSolver, wrappedNode);
            } else {
                return getParent().solveSymbol(name, typeSolver);
            }
        }
    }

    @Override
    public SymbolReference<ResolvedMethodDeclaration> solveMethod(String name, List<ResolvedType> argumentsTypes,
                                                                  boolean staticOnly, TypeSolver typeSolver) {
        return getParent().solveMethod(name, argumentsTypes, false, typeSolver);
    }

    @Override
    public SymbolReference<? extends ResolvedValueDeclaration> solveLambda(TypeSolver typeSolver,
                                                                           BiFunction<Declaration, Node, Boolean> checkFunction) {

        if (wrappedNode.getVariable().getVariables().size() != 1) {
            throw new IllegalStateException();
        }

        VariableDeclarationExpr variableDeclarationExpr = wrappedNode.getVariable();

        SymbolDeclarator symbolDeclarator = new VariableSymbolDeclarator(variableDeclarationExpr,
                typeSolver);
        SymbolReference<? extends ResolvedValueDeclaration> symbolReference = solveWithLambda(
                symbolDeclarator,
                variableDeclarationExpr,
                checkFunction);

        if (symbolReference.isSolved()) {
            // return SymbolReference.solved(JavaParserSymbolDeclaration.localVar(v, typeSolver));
            return symbolReference;
        } else {
            if (requireParentNode(wrappedNode) instanceof BlockStmt) {
                return StatementContext.solveInBlockLambda(typeSolver, wrappedNode, checkFunction);
            } else {
                return getParent().solveLambda(typeSolver, checkFunction);
            }
        }

    }

}
