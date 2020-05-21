package net.meanscript.core;
import net.meanscript.java.*;
import net.meanscript.*;
/*

 *

 *    Meanscript ByteAutomata (c) 2020, Meanwhale

 *

 *    GitHub page:     https://github.com/Meanwhale/ByteAutomata

 *    Email:           meanwhale@gmail.com

 *    Twitter:         https://twitter.com/TheMeanwhale

 *

 */
public class MicroLexer {
public static final String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
public static final String numbers = "1234567890";
public static final String whiteSpace = " \t\n\r";
public static final String linebreak = "\n\r";
public static final String expressionBreak = ",;";
public static final String blockStart = "([{";
public static final String blockEnd = ")]}";
public static final int NODE_EXPR = 101;
public static final int NODE_PARENTHESIS = 102;
public static final int NODE_SQUARE_BRACKETS = 103;
public static final int NODE_CURLY_BRACKETS = 104;
public static final int NODE_TEXT = 105;
public static final int NODE_NUMBER = 106;
private static byte stateSpace, stateName, stateNumber;
private static ByteAutomata automata;
private static int lastStart;
private static MNode root;
private static MNode currentBlock;
private static MNode currentExpr;
private static MNode currentToken;
private static void next(byte state)
{
 // transition to a next state
 lastStart = automata.getIndex();
 automata.next(state);
}
private static void stay() throws MException
{
 automata.stay();
}
private static void addExpr()
{
 MNode expr = new MNode(currentBlock, NODE_EXPR, "<EXPR>");
 currentExpr.next = expr;
 currentExpr = expr;
 currentToken = null;
}
private static void addToken(int tokenType) throws MException
{
 String data = automata.getString(lastStart, automata.getIndex() - lastStart);
 MSJava.verbosen("NEW TOKEN: ").print(data).print("\n");
 MNode token = new MNode(currentExpr, tokenType, data);
 if (currentToken == null) currentExpr.child = token;
 else currentToken.next = token;
 currentExpr.numChildren++;
 currentToken = token;
 lastStart = automata.getIndex();
}
private static void exprBreak() throws MException
{
 if (currentBlock != null) currentBlock.numChildren ++;
 lastStart = -1;
 addExpr();
}
private static void addBlock() throws MException
{
 byte inputByte = automata.getInputByte();
 int blockType = 0;
 if (inputByte == '(') blockType = NODE_PARENTHESIS;
 else if (inputByte == '[') blockType = NODE_SQUARE_BRACKETS;
 else if (inputByte == '{') blockType = NODE_CURLY_BRACKETS;
 else { MSJava.assertion(false,null,"unhandled block start: " + inputByte); }
 lastStart = -1;
 MNode block = new MNode(currentExpr, blockType, "<BLOCK>");
 if (currentToken == null) currentExpr.child = block;
 else currentToken.next = block;
 currentExpr.numChildren++;
 currentBlock = block;
 MNode expr = new MNode(currentBlock, 0, "<EXPR>");
 currentBlock.child = expr;
 currentExpr = expr;
 currentToken = null;
}
private static void endBlock() throws MException
{
 MSJava.assertion((currentBlock != null),null,"unexpected block end");
 byte inputByte = automata.getInputByte();
 // Check that block-end character is the right one.
 // The 'type' is block start/end character's ASCII code.
 if (currentBlock.type == NODE_PARENTHESIS ) { MSJava.assertion(inputByte == ')',null,"invalid block end; parenthesis was expected");}
 else if (currentBlock.type == NODE_SQUARE_BRACKETS ) { MSJava.assertion(inputByte == ']',null,"invalid block end; square bracket was expected");}
 else if (currentBlock.type == NODE_CURLY_BRACKETS ) { MSJava.assertion(inputByte == '}',null,"invalid block end; curly bracket was expected");}
 else { MSJava.assertion(false,null,"unhandled block end: " + inputByte); }
 lastStart = -1;
 currentToken = currentBlock;
 currentExpr = currentToken.parent;
 currentBlock = currentExpr.parent;
}
private static void defineTransitions(ByteAutomata ba) throws MException
{
 stateSpace = ba.addState("space");
 stateName = ba.addState("name");
 stateNumber = ba.addState("number");
 ba.transition(stateSpace, whiteSpace, null);
 ba.transition(stateSpace, letters, () -> { next(stateName); });
 ba.transition(stateSpace, numbers, () -> { next(stateNumber); });
 ba.transition(stateSpace, expressionBreak, () -> { exprBreak(); });
 ba.transition(stateSpace, blockStart, () -> { addBlock();});
 ba.transition(stateSpace, blockEnd, () -> { endBlock();});
 ba.transition(stateName, letters, null);
 ba.transition(stateName, whiteSpace, () -> { addToken(NODE_TEXT); next(stateSpace); });
 ba.transition(stateName, blockStart, () -> { addToken(NODE_TEXT); stay(); next(stateSpace); });
 ba.transition(stateName, blockEnd, () -> { addToken(NODE_TEXT); stay(); next(stateSpace); });
 ba.transition(stateName, expressionBreak, () -> { addToken(NODE_TEXT); exprBreak(); next(stateSpace); });
 ba.transition(stateNumber, numbers, null);
 ba.transition(stateNumber, whiteSpace, () -> { addToken(NODE_NUMBER); next(stateSpace); });
 ba.transition(stateNumber, blockStart, () -> { addToken(NODE_NUMBER); stay(); next(stateSpace); });
 ba.transition(stateNumber, blockEnd, () -> { addToken(NODE_NUMBER); stay(); next(stateSpace); });
 ba.transition(stateNumber, expressionBreak, () -> { addToken(NODE_NUMBER); exprBreak(); next(stateSpace); });
}
private static MNode lexInput(MInputStream input) throws MException
{
 ByteAutomata ba = new ByteAutomata();
 automata = ba;
 defineTransitions(ba);
 ba.next((byte)1); // set first state
 root = new MNode(null, 0, "<ROOT>");
 currentExpr = root;
 currentBlock = null;
 currentToken = null;
 lastStart = 0;
 ba.run(input);
 ba.step((byte)' '); // ended cleanly: last white space
 if (currentBlock != null)
 {
  MSJava.print("closing parenthesis missing at the end");
  ba.ok = false;
 }
 if (!ba.ok)
 {
  ba.printError();
  root = null;
  return null;
 }
 MSJava.verbose("\nFINISHED!");
 return root;
}
public static MNode lex (String s) throws MException
{
 MInputArray input = new MInputArray(s);
 return lexInput(input);
}
public static void printTitle (String s) throws MException
{
 MSJava.print("\n    ByteAutomata (c) 2020, Meanwhale");
 MSJava.print("    https://github.com/Meanwhale/ByteAutomata\n");
}
public static void printArgInfo () throws MException
{
 MSJava.print("Command line arguments:");
 MSJava.print("    [cmd] -i            read standard input, eg. [cmd] -i < ../test_script.txt");
 MSJava.print("    [cmd] -t            run default test");
 MSJava.print("    [cmd] \"[string]\"    parse [string]");
}
public static void printStdinInfo () throws MException
{
 MSJava.print("Hit Ctrl-Z (Windows) or ^D (Linux/Mac) at the start of a line and press enter to finish.");
 MSJava.print("Read from standard input...");
}
}
