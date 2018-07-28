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
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithStatements;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserSymbolDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarators.VariableSymbolDeclarator;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.SymbolDeclarator;

public class ForStatementContext extends AbstractJavaParserContext<ForStmt> {

    public ForStatementContext(ForStmt wrappedNode, TypeSolver typeSolver) {
        super(wrappedNode, typeSolver);
    }

    @Override
    public SymbolReference<? extends ResolvedValueDeclaration> solveSymbol(String name, TypeSolver typeSolver) {
        for (Expression expression : wrappedNode.getInitialization()) {
            if (expression instanceof VariableDeclarationExpr) {
                VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) expression;
                for (VariableDeclarator variableDeclarator : variableDeclarationExpr.getVariables()) {
                    if (variableDeclarator.getName().getId().equals(name)) {
                        return SymbolReference.solved(JavaParserSymbolDeclaration.localVar(variableDeclarator, typeSolver));
                    }
                }
            } else if (!(expression instanceof AssignExpr || expression instanceof MethodCallExpr)) {
                throw new UnsupportedOperationException(expression.getClass().getCanonicalName());
            }
        }

        if (requireParentNode(wrappedNode) instanceof NodeWithStatements) {
            return StatementContext.solveInBlock(name, typeSolver, wrappedNode);
        } else {
            return getParent().solveSymbol(name, typeSolver);
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

        for (Expression expression : wrappedNode.getInitialization()) {
            if (expression instanceof VariableDeclarationExpr) {
                VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) expression;

                SymbolDeclarator symbolDeclarator = new VariableSymbolDeclarator(variableDeclarationExpr,
                        typeSolver);
                SymbolReference<? extends ResolvedValueDeclaration> symbolReference = solveWithLambda(
                        symbolDeclarator,
                        expression,
                        checkFunction);

                if (symbolReference.isSolved()) {
                    // return SymbolReference.solved(JavaParserSymbolDeclaration.localVar(v, typeSolver));
                    return symbolReference;
                }
            } else if (!(expression instanceof AssignExpr || expression instanceof MethodCallExpr)) {
                throw new UnsupportedOperationException(expression.getClass().getCanonicalName());
            }
        }

        if (requireParentNode(wrappedNode) instanceof NodeWithStatements) {
            return StatementContext.solveInBlockLambda(typeSolver, wrappedNode, checkFunction);
        } else {
            return getParent().solveLambda(typeSolver, checkFunction);
        }

    }
}
