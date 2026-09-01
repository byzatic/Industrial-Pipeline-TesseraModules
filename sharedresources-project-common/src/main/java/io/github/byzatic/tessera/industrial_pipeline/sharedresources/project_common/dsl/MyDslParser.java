package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dsl;
// Generated from MyDsl.g4 by ANTLR 4.13.2

import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class MyDslParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		ID=18, STRING=19, WS=20, LINE_COMMENT=21, BLOCK_COMMENT=22;
	public static final int
		RULE_script = 0, RULE_command = 1, RULE_getCommand = 2, RULE_sourceType = 3, 
		RULE_processCommand = 4, RULE_putCommand = 5, RULE_modifierClause = 6, 
		RULE_aliasClause = 7, RULE_argsClause = 8, RULE_argList = 9;
	private static String[] makeRuleNames() {
		return new String[] {
			"script", "command", "getCommand", "sourceType", "processCommand", "putCommand", 
			"modifierClause", "aliasClause", "argsClause", "argList"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'GET FROM'", "'STORAGE'", "'BY DATA ID'", "';'", "'SELF'", "'CHILD'", 
			"'PROCESS FUNCTION'", "'RETURN'", "'PUT DATA'", "'TO STORAGE'", "'MODIFIER'", 
			"'local'", "'global'", "'AS'", "'('", "')'", "','"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, "ID", "STRING", "WS", "LINE_COMMENT", 
			"BLOCK_COMMENT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "MyDsl.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public MyDslParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ScriptContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(MyDslParser.EOF, 0); }
		public List<CommandContext> command() {
			return getRuleContexts(CommandContext.class);
		}
		public CommandContext command(int i) {
			return getRuleContext(CommandContext.class,i);
		}
		public ScriptContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_script; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) ((MyDslListener)listener).enterScript(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) ((MyDslListener)listener).exitScript(this);
		}
	}

	public final ScriptContext script() throws RecognitionException {
		ScriptContext _localctx = new ScriptContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_script);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(21); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(20);
				command();
				}
				}
				setState(23); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 642L) != 0) );
			setState(25);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CommandContext extends ParserRuleContext {
		public GetCommandContext getCommand() {
			return getRuleContext(GetCommandContext.class,0);
		}
		public ProcessCommandContext processCommand() {
			return getRuleContext(ProcessCommandContext.class,0);
		}
		public PutCommandContext putCommand() {
			return getRuleContext(PutCommandContext.class,0);
		}
		public CommandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_command; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) ((MyDslListener)listener).enterCommand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) ((MyDslListener)listener).exitCommand(this);
		}
	}

	public final CommandContext command() throws RecognitionException {
		CommandContext _localctx = new CommandContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_command);
		try {
			setState(30);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				enterOuterAlt(_localctx, 1);
				{
				setState(27);
				getCommand();
				}
				break;
			case T__6:
				enterOuterAlt(_localctx, 2);
				{
				setState(28);
				processCommand();
				}
				break;
			case T__8:
				enterOuterAlt(_localctx, 3);
				{
				setState(29);
				putCommand();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GetCommandContext extends ParserRuleContext {
		public SourceTypeContext source;
		public Token storage;
		public ModifierClauseContext modifier;
		public Token dataId;
		public AliasClauseContext alias;
		public SourceTypeContext sourceType() {
			return getRuleContext(SourceTypeContext.class,0);
		}
		public List<TerminalNode> ID() { return getTokens(MyDslParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(MyDslParser.ID, i);
		}
		public ModifierClauseContext modifierClause() {
			return getRuleContext(ModifierClauseContext.class,0);
		}
		public AliasClauseContext aliasClause() {
			return getRuleContext(AliasClauseContext.class,0);
		}
		public GetCommandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_getCommand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) {
                try {
                    ((MyDslListener)listener).enterGetCommand(this);
                } catch (MCg3ApiOperationIncompleteException e) {
                    throw new RuntimeException(e);
                }
            }
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) ((MyDslListener)listener).exitGetCommand(this);
		}
	}

	public final GetCommandContext getCommand() throws RecognitionException {
		GetCommandContext _localctx = new GetCommandContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_getCommand);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(32);
			match(T__0);
			setState(33);
			((GetCommandContext)_localctx).source = sourceType();
			setState(34);
			match(T__1);
			setState(35);
			((GetCommandContext)_localctx).storage = match(ID);
			setState(37);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__10) {
				{
				setState(36);
				((GetCommandContext)_localctx).modifier = modifierClause();
				}
			}

			setState(39);
			match(T__2);
			setState(40);
			((GetCommandContext)_localctx).dataId = match(ID);
			setState(42);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__13) {
				{
				setState(41);
				((GetCommandContext)_localctx).alias = aliasClause();
				}
			}

			setState(44);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SourceTypeContext extends ParserRuleContext {
		public Token child;
		public TerminalNode ID() { return getToken(MyDslParser.ID, 0); }
		public SourceTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sourceType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) ((MyDslListener)listener).enterSourceType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) ((MyDslListener)listener).exitSourceType(this);
		}
	}

	public final SourceTypeContext sourceType() throws RecognitionException {
		SourceTypeContext _localctx = new SourceTypeContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_sourceType);
		try {
			setState(49);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__4:
				enterOuterAlt(_localctx, 1);
				{
				setState(46);
				match(T__4);
				}
				break;
			case T__5:
				enterOuterAlt(_localctx, 2);
				{
				setState(47);
				match(T__5);
				setState(48);
				((SourceTypeContext)_localctx).child = match(ID);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ProcessCommandContext extends ParserRuleContext {
		public Token function;
		public ArgsClauseContext arguments;
		public Token resultId;
		public List<TerminalNode> ID() { return getTokens(MyDslParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(MyDslParser.ID, i);
		}
		public ArgsClauseContext argsClause() {
			return getRuleContext(ArgsClauseContext.class,0);
		}
		public ProcessCommandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_processCommand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) {
                try {
                    ((MyDslListener)listener).enterProcessCommand(this);
                } catch (MCg3ApiOperationIncompleteException e) {
                    throw new RuntimeException(e);
                }
            }
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) ((MyDslListener)listener).exitProcessCommand(this);
		}
	}

	public final ProcessCommandContext processCommand() throws RecognitionException {
		ProcessCommandContext _localctx = new ProcessCommandContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_processCommand);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(51);
			match(T__6);
			setState(52);
			((ProcessCommandContext)_localctx).function = match(ID);
			setState(54);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__14) {
				{
				setState(53);
				((ProcessCommandContext)_localctx).arguments = argsClause();
				}
			}

			setState(56);
			match(T__7);
			setState(57);
			((ProcessCommandContext)_localctx).resultId = match(ID);
			setState(58);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PutCommandContext extends ParserRuleContext {
		public Token localDataId;
		public Token storage;
		public ModifierClauseContext modifier;
		public Token dataId;
		public List<TerminalNode> ID() { return getTokens(MyDslParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(MyDslParser.ID, i);
		}
		public ModifierClauseContext modifierClause() {
			return getRuleContext(ModifierClauseContext.class,0);
		}
		public PutCommandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_putCommand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) {
                try {
                    ((MyDslListener)listener).enterPutCommand(this);
                } catch (MCg3ApiOperationIncompleteException e) {
                    throw new RuntimeException(e);
                }
            }
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) ((MyDslListener)listener).exitPutCommand(this);
		}
	}

	public final PutCommandContext putCommand() throws RecognitionException {
		PutCommandContext _localctx = new PutCommandContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_putCommand);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(60);
			match(T__8);
			setState(61);
			((PutCommandContext)_localctx).localDataId = match(ID);
			setState(62);
			match(T__9);
			setState(63);
			((PutCommandContext)_localctx).storage = match(ID);
			setState(65);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__10) {
				{
				setState(64);
				((PutCommandContext)_localctx).modifier = modifierClause();
				}
			}

			setState(67);
			match(T__2);
			setState(68);
			((PutCommandContext)_localctx).dataId = match(ID);
			setState(69);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ModifierClauseContext extends ParserRuleContext {
		public Token mod;
		public ModifierClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modifierClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) ((MyDslListener)listener).enterModifierClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) ((MyDslListener)listener).exitModifierClause(this);
		}
	}

	public final ModifierClauseContext modifierClause() throws RecognitionException {
		ModifierClauseContext _localctx = new ModifierClauseContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_modifierClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71);
			match(T__10);
			setState(72);
			((ModifierClauseContext)_localctx).mod = _input.LT(1);
			_la = _input.LA(1);
			if ( !(_la==T__11 || _la==T__12) ) {
				((ModifierClauseContext)_localctx).mod = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AliasClauseContext extends ParserRuleContext {
		public Token alias;
		public TerminalNode ID() { return getToken(MyDslParser.ID, 0); }
		public AliasClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aliasClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) ((MyDslListener)listener).enterAliasClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) ((MyDslListener)listener).exitAliasClause(this);
		}
	}

	public final AliasClauseContext aliasClause() throws RecognitionException {
		AliasClauseContext _localctx = new AliasClauseContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_aliasClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(74);
			match(T__13);
			setState(75);
			((AliasClauseContext)_localctx).alias = match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArgsClauseContext extends ParserRuleContext {
		public ArgListContext argList() {
			return getRuleContext(ArgListContext.class,0);
		}
		public ArgsClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_argsClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) ((MyDslListener)listener).enterArgsClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) ((MyDslListener)listener).exitArgsClause(this);
		}
	}

	public final ArgsClauseContext argsClause() throws RecognitionException {
		ArgsClauseContext _localctx = new ArgsClauseContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_argsClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(77);
			match(T__14);
			setState(79);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==STRING) {
				{
				setState(78);
				argList();
				}
			}

			setState(81);
			match(T__15);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArgListContext extends ParserRuleContext {
		public List<TerminalNode> STRING() { return getTokens(MyDslParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(MyDslParser.STRING, i);
		}
		public ArgListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_argList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) ((MyDslListener)listener).enterArgList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MyDslListener ) ((MyDslListener)listener).exitArgList(this);
		}
	}

	public final ArgListContext argList() throws RecognitionException {
		ArgListContext _localctx = new ArgListContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_argList);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(83);
			match(STRING);
			setState(88);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(84);
					match(T__16);
					setState(85);
					match(STRING);
					}
					}
				}
				setState(90);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			}
			setState(92);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__16) {
				{
				setState(91);
				match(T__16);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u0016_\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0001\u0000\u0004\u0000\u0016\b\u0000\u000b"+
		"\u0000\f\u0000\u0017\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0003\u0001\u001f\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0003\u0002&\b\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0003\u0002+\b\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0003\u00032\b\u0003\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0003\u00047\b\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003"+
		"\u0005B\b\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\b\u0001\b\u0003\bP\b\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0005"+
		"\tW\b\t\n\t\f\tZ\t\t\u0001\t\u0003\t]\b\t\u0001\t\u0000\u0000\n\u0000"+
		"\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0000\u0001\u0001\u0000\f\r"+
		"_\u0000\u0015\u0001\u0000\u0000\u0000\u0002\u001e\u0001\u0000\u0000\u0000"+
		"\u0004 \u0001\u0000\u0000\u0000\u00061\u0001\u0000\u0000\u0000\b3\u0001"+
		"\u0000\u0000\u0000\n<\u0001\u0000\u0000\u0000\fG\u0001\u0000\u0000\u0000"+
		"\u000eJ\u0001\u0000\u0000\u0000\u0010M\u0001\u0000\u0000\u0000\u0012S"+
		"\u0001\u0000\u0000\u0000\u0014\u0016\u0003\u0002\u0001\u0000\u0015\u0014"+
		"\u0001\u0000\u0000\u0000\u0016\u0017\u0001\u0000\u0000\u0000\u0017\u0015"+
		"\u0001\u0000\u0000\u0000\u0017\u0018\u0001\u0000\u0000\u0000\u0018\u0019"+
		"\u0001\u0000\u0000\u0000\u0019\u001a\u0005\u0000\u0000\u0001\u001a\u0001"+
		"\u0001\u0000\u0000\u0000\u001b\u001f\u0003\u0004\u0002\u0000\u001c\u001f"+
		"\u0003\b\u0004\u0000\u001d\u001f\u0003\n\u0005\u0000\u001e\u001b\u0001"+
		"\u0000\u0000\u0000\u001e\u001c\u0001\u0000\u0000\u0000\u001e\u001d\u0001"+
		"\u0000\u0000\u0000\u001f\u0003\u0001\u0000\u0000\u0000 !\u0005\u0001\u0000"+
		"\u0000!\"\u0003\u0006\u0003\u0000\"#\u0005\u0002\u0000\u0000#%\u0005\u0012"+
		"\u0000\u0000$&\u0003\f\u0006\u0000%$\u0001\u0000\u0000\u0000%&\u0001\u0000"+
		"\u0000\u0000&\'\u0001\u0000\u0000\u0000\'(\u0005\u0003\u0000\u0000(*\u0005"+
		"\u0012\u0000\u0000)+\u0003\u000e\u0007\u0000*)\u0001\u0000\u0000\u0000"+
		"*+\u0001\u0000\u0000\u0000+,\u0001\u0000\u0000\u0000,-\u0005\u0004\u0000"+
		"\u0000-\u0005\u0001\u0000\u0000\u0000.2\u0005\u0005\u0000\u0000/0\u0005"+
		"\u0006\u0000\u000002\u0005\u0012\u0000\u00001.\u0001\u0000\u0000\u0000"+
		"1/\u0001\u0000\u0000\u00002\u0007\u0001\u0000\u0000\u000034\u0005\u0007"+
		"\u0000\u000046\u0005\u0012\u0000\u000057\u0003\u0010\b\u000065\u0001\u0000"+
		"\u0000\u000067\u0001\u0000\u0000\u000078\u0001\u0000\u0000\u000089\u0005"+
		"\b\u0000\u00009:\u0005\u0012\u0000\u0000:;\u0005\u0004\u0000\u0000;\t"+
		"\u0001\u0000\u0000\u0000<=\u0005\t\u0000\u0000=>\u0005\u0012\u0000\u0000"+
		">?\u0005\n\u0000\u0000?A\u0005\u0012\u0000\u0000@B\u0003\f\u0006\u0000"+
		"A@\u0001\u0000\u0000\u0000AB\u0001\u0000\u0000\u0000BC\u0001\u0000\u0000"+
		"\u0000CD\u0005\u0003\u0000\u0000DE\u0005\u0012\u0000\u0000EF\u0005\u0004"+
		"\u0000\u0000F\u000b\u0001\u0000\u0000\u0000GH\u0005\u000b\u0000\u0000"+
		"HI\u0007\u0000\u0000\u0000I\r\u0001\u0000\u0000\u0000JK\u0005\u000e\u0000"+
		"\u0000KL\u0005\u0012\u0000\u0000L\u000f\u0001\u0000\u0000\u0000MO\u0005"+
		"\u000f\u0000\u0000NP\u0003\u0012\t\u0000ON\u0001\u0000\u0000\u0000OP\u0001"+
		"\u0000\u0000\u0000PQ\u0001\u0000\u0000\u0000QR\u0005\u0010\u0000\u0000"+
		"R\u0011\u0001\u0000\u0000\u0000SX\u0005\u0013\u0000\u0000TU\u0005\u0011"+
		"\u0000\u0000UW\u0005\u0013\u0000\u0000VT\u0001\u0000\u0000\u0000WZ\u0001"+
		"\u0000\u0000\u0000XV\u0001\u0000\u0000\u0000XY\u0001\u0000\u0000\u0000"+
		"Y\\\u0001\u0000\u0000\u0000ZX\u0001\u0000\u0000\u0000[]\u0005\u0011\u0000"+
		"\u0000\\[\u0001\u0000\u0000\u0000\\]\u0001\u0000\u0000\u0000]\u0013\u0001"+
		"\u0000\u0000\u0000\n\u0017\u001e%*16AOX\\";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
