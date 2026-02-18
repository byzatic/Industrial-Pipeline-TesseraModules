package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dsl;
// Generated from MyDsl.g4 by ANTLR 4.13.2

import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MyDslParser}.
 */
public interface MyDslListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MyDslParser#script}.
	 * @param ctx the parse tree
	 */
	void enterScript(MyDslParser.ScriptContext ctx);
	/**
	 * Exit a parse tree produced by {@link MyDslParser#script}.
	 * @param ctx the parse tree
	 */
	void exitScript(MyDslParser.ScriptContext ctx);
	/**
	 * Enter a parse tree produced by {@link MyDslParser#command}.
	 * @param ctx the parse tree
	 */
	void enterCommand(MyDslParser.CommandContext ctx);
	/**
	 * Exit a parse tree produced by {@link MyDslParser#command}.
	 * @param ctx the parse tree
	 */
	void exitCommand(MyDslParser.CommandContext ctx);
	/**
	 * Enter a parse tree produced by {@link MyDslParser#getCommand}.
	 * @param ctx the parse tree
	 */
	void enterGetCommand(MyDslParser.GetCommandContext ctx) throws MCg3ApiOperationIncompleteException;
	/**
	 * Exit a parse tree produced by {@link MyDslParser#getCommand}.
	 * @param ctx the parse tree
	 */
	void exitGetCommand(MyDslParser.GetCommandContext ctx);
	/**
	 * Enter a parse tree produced by {@link MyDslParser#sourceType}.
	 * @param ctx the parse tree
	 */
	void enterSourceType(MyDslParser.SourceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MyDslParser#sourceType}.
	 * @param ctx the parse tree
	 */
	void exitSourceType(MyDslParser.SourceTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MyDslParser#processCommand}.
	 * @param ctx the parse tree
	 */
	void enterProcessCommand(MyDslParser.ProcessCommandContext ctx) throws MCg3ApiOperationIncompleteException;
	/**
	 * Exit a parse tree produced by {@link MyDslParser#processCommand}.
	 * @param ctx the parse tree
	 */
	void exitProcessCommand(MyDslParser.ProcessCommandContext ctx);
	/**
	 * Enter a parse tree produced by {@link MyDslParser#putCommand}.
	 * @param ctx the parse tree
	 */
	void enterPutCommand(MyDslParser.PutCommandContext ctx) throws MCg3ApiOperationIncompleteException;
	/**
	 * Exit a parse tree produced by {@link MyDslParser#putCommand}.
	 * @param ctx the parse tree
	 */
	void exitPutCommand(MyDslParser.PutCommandContext ctx);
	/**
	 * Enter a parse tree produced by {@link MyDslParser#modifierClause}.
	 * @param ctx the parse tree
	 */
	void enterModifierClause(MyDslParser.ModifierClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link MyDslParser#modifierClause}.
	 * @param ctx the parse tree
	 */
	void exitModifierClause(MyDslParser.ModifierClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link MyDslParser#aliasClause}.
	 * @param ctx the parse tree
	 */
	void enterAliasClause(MyDslParser.AliasClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link MyDslParser#aliasClause}.
	 * @param ctx the parse tree
	 */
	void exitAliasClause(MyDslParser.AliasClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link MyDslParser#argsClause}.
	 * @param ctx the parse tree
	 */
	void enterArgsClause(MyDslParser.ArgsClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link MyDslParser#argsClause}.
	 * @param ctx the parse tree
	 */
	void exitArgsClause(MyDslParser.ArgsClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link MyDslParser#argList}.
	 * @param ctx the parse tree
	 */
	void enterArgList(MyDslParser.ArgListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MyDslParser#argList}.
	 * @param ctx the parse tree
	 */
	void exitArgList(MyDslParser.ArgListContext ctx);
}