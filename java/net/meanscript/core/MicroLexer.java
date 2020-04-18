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
public static final String whitestateSpace = " \t\n\r";
public static final String linebreak = "\n\r";
public static final String expressionBreak = ",;";
public static final String blockStart = "([{";
public static final String blockEnd = ")]}";
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
 MNode expr = new MNode(currentBlock, 0, "<EXPR>");
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
private static void addBlock(int blockType) throws MException
{
 MSJava.verbosen("add block: ").print(blockType).print("\n");
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
private static void endBlock(int blockType) throws MException
{
 MSJava.assertion((currentBlock != null),null,"unexpected block end");
 // Check that block-end character is the right one.
 // The 'type' is block start/end character's ASCII code.
 if (currentBlock.type == 40 ) {MSJava.assertion(blockType == 41,null,"invalid block end; parenthesis was expected");}
 else if (currentBlock.type == 91 ) {MSJava.assertion(blockType == 93,null,"invalid block end; square bracket was expected");}
 else if (currentBlock.type == 123) {MSJava.assertion(blockType == 125,null,"invalid block end; curly bracket was expected");}
 else { MSJava.assertion(false,null,"unhandled block end: " + blockType); }
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
 ba.transition(stateSpace, whitestateSpace, null);
 ba.transition(stateSpace, letters, () -> { next(stateName); });
 ba.transition(stateSpace, numbers, () -> { next(stateNumber); });
 ba.transition(stateSpace, expressionBreak, () -> { exprBreak(); });
 ba.transition(stateSpace, blockStart, () -> { addBlock((int)automata.getInputByte());});
 ba.transition(stateSpace, blockEnd, () -> { endBlock((int)automata.getInputByte());});
 ba.transition(stateName, letters, null);
 ba.transition(stateName, whitestateSpace, () -> { addToken(0); next(stateSpace); });
 ba.transition(stateName, blockStart, () -> { addToken(0); stay(); next(stateSpace); });
 ba.transition(stateName, blockEnd, () -> { addToken(0); stay(); next(stateSpace); });
 ba.transition(stateName, expressionBreak, () -> { addToken(0); exprBreak(); next(stateSpace); });
 ba.transition(stateNumber, numbers, null);
 ba.transition(stateNumber, whitestateSpace, () -> { addToken(0); next(stateSpace); });
 ba.transition(stateNumber, blockStart, () -> { addToken(0); stay(); next(stateSpace); });
 ba.transition(stateNumber, blockEnd, () -> { addToken(0); stay(); next(stateSpace); });
 ba.transition(stateNumber, expressionBreak, () -> { addToken(0); exprBreak(); next(stateSpace); });
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
