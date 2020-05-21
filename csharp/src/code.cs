// C-Sharp 
namespace meanscript { 
public class ByteAutomata {
internal bool ok;
internal byte[] tr;
internal byte currentInput;
internal byte currentState;
internal System.Collections.Generic.Dictionary<int, string> stateNames = new System.Collections.Generic.Dictionary<int, string>();
internal MeanCS.MAction [] actions=new MeanCS.MAction [64];
internal byte stateCounter;
internal byte actionCounter; // 0 = end
// running:
internal byte inputByte = 0;
internal int index = 0;
internal int lineNumber = 0;
internal bool stayNextStep = false;
internal bool running = false;
internal byte[] buffer;
internal byte[] tmp;
public const int MAX_STATES = 32;
public const int BUFFER_SIZE = 512;
public const int CFG_MAX_NAME_LENGTH = 128;
public ByteAutomata()
{
 ok = true;
 currentInput = 0;
 currentState = 0;
 stateCounter = 0;
 actionCounter = 0;
 tr = new byte[MAX_STATES * 256];
 for (int i=0; i<MAX_STATES * 256; i++) tr[i] = (byte)0xff;
 inputByte = 0;
 index = 0;
 lineNumber = 0;
 stayNextStep = false;
 running = false;
 buffer = new byte[BUFFER_SIZE];
 tmp = new byte[BUFFER_SIZE];
}

public void print ()
{
 for (int i = 0; i <= stateCounter; i++)
 {
  MeanCS.verbosen("state: ").print(i).print("\n");
  for (int n = 0; n < 256; n++)
  {
   byte foo = tr[(i * 256) + n];
   if (foo == 0xff) MeanCS.verbosen(".");
   else MeanCS.verbosen(foo);
  }
  MeanCS.verbose("");
 }
}
public byte addState (string stateName)
{
 stateCounter++;
 stateNames[(int)stateCounter] = stateName;
 return stateCounter;
}
public void transition (byte state, string input, MeanCS.MAction action)
{
 byte actionIndex = 0;
 if (action != null)
 {
  actionIndex = addAction(action);
 }
 byte[] bytes = System.Text.Encoding.ASCII.GetBytes(input);
 int i = 0;
 while (i<input.Length)
 {
  tr[(state * 256) + bytes[i]] = actionIndex;
  i++;
 }
 //DEBUG(VR("New Transition added: id ")X(actionIndex)XO);
}
public void fillTransition (byte state, MeanCS.MAction action)
{
 byte actionIndex = 0;
 if (action != null) actionIndex = addAction(action);
 for (int i=0; i<256; i++)
 {
  tr[(state * 256) + i] = actionIndex;
 }
 //DEBUG(VR("New Transition filled: id ")X(actionIndex)XO);
}
public byte addAction (MeanCS.MAction action)
{
 actionCounter++;
 actions[actionCounter] = action;
 return actionCounter;
}
public void next (byte nextState)
{
 currentState = nextState;
 {if (MeanCS.debug) {MeanCS.verbosen("Next state: ").print(stateNames[(int)currentState]).print("\n");}};
}
public bool step (byte input)
{
 currentInput = input;
 int index = (currentState * 256) + input;
 byte actionIndex = tr[index];
 if (actionIndex == 0) return true; // stay on same state and do nothing else
 if (actionIndex == 0xff||actionIndex < 0)
 {
  ok = false;
  return false; // end
 }
 MeanCS.MAction act = actions[actionIndex];
 if (act == null)
 {
  System.Diagnostics.Debug.Assert(false,"invalid action index");
 }
 act();
 return true;
}
public int getIndex ()
{
 return index;
}
public byte getInputByte ()
{
 return inputByte;
}
public void stay ()
{
 // same input byte on next step
 System.Diagnostics.Debug.Assert(!stayNextStep,"'stay' is called twice");
 stayNextStep = true;
}
public string getString (int start, int length)
{
 MeanCS.assertion(length < CFG_MAX_NAME_LENGTH,null,"name is too long");
 int i = 0;
 for (; i < length; i++)
 {
  tmp[i] = buffer[start++ % BUFFER_SIZE];
 }
 return System.Text.Encoding.UTF8.GetString(tmp,0,length);
}
public void run (MInputStream input)
{
 inputByte = 0;
 index = 0;
 lineNumber = 1;
 stayNextStep = false;
 running = true;
 while ((!input.end() || stayNextStep) && running && ok)
 {
  if (!stayNextStep)
  {
   index ++;
   inputByte = input.readByte();
   buffer[index % BUFFER_SIZE] = inputByte;
   if (inputByte == '\n') lineNumber++;
  }
  else
  {
   stayNextStep = false;
  }
  MeanCS.printn("[ ").print((char)(inputByte)).print(" ]").print("\n");
  running = step(inputByte);
 }
 if (!stayNextStep) index++;
}
public void printError ()
{
 MeanCS.printn("ERROR: parser state [").print(stateNames[(int)currentState]).print("]").print("\n");
 MeanCS.printn("Line ").print(lineNumber).print(": \"");
 // print nearby code
 int start = index-1;
 while (start > 0 && index - start < BUFFER_SIZE && (char)buffer[start % BUFFER_SIZE] != '\n')
 {
  start --;
 }
 while (++start < index)
 {
  MeanCS.verbosen((char)(buffer[start % BUFFER_SIZE]));
 }
 MeanCS.print("\"");
}
}
public class MicroLexer {
public const string letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
public const string numbers = "1234567890";
public const string whiteSpace = " \t\n\r";
public const string linebreak = "\n\r";
public const string expressionBreak = ",;";
public const string blockStart = "([{";
public const string blockEnd = ")]}";
public const int NODE_EXPR = 101;
public const int NODE_PARENTHESIS = 102;
public const int NODE_SQUARE_BRACKETS = 103;
public const int NODE_CURLY_BRACKETS = 104;
public const int NODE_TEXT = 105;
public const int NODE_NUMBER = 106;
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
private static void stay()
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
private static void addToken(int tokenType)
{
 string data = automata.getString(lastStart, automata.getIndex() - lastStart);
 MeanCS.verbosen("NEW TOKEN: ").print(data).print("\n");
 MNode token = new MNode(currentExpr, tokenType, data);
 if (currentToken == null) currentExpr.child = token;
 else currentToken.next = token;
 currentExpr.numChildren++;
 currentToken = token;
 lastStart = automata.getIndex();
}
private static void exprBreak()
{
 if (currentBlock != null) currentBlock.numChildren ++;
 lastStart = -1;
 addExpr();
}
private static void addBlock()
{
 byte inputByte = automata.getInputByte();
 int blockType = 0;
 if (inputByte == '(') blockType = NODE_PARENTHESIS;
 else if (inputByte == '[') blockType = NODE_SQUARE_BRACKETS;
 else if (inputByte == '{') blockType = NODE_CURLY_BRACKETS;
 else { MeanCS.assertion(false,null,"unhandled block start: " + inputByte); }
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
private static void endBlock()
{
 MeanCS.assertion((currentBlock != null),null,"unexpected block end");
 byte inputByte = automata.getInputByte();
 // Check that block-end character is the right one.
 // The 'type' is block start/end character's ASCII code.
 if (currentBlock.type == NODE_PARENTHESIS ) { MeanCS.assertion(inputByte == ')',null,"invalid block end; parenthesis was expected");}
 else if (currentBlock.type == NODE_SQUARE_BRACKETS ) { MeanCS.assertion(inputByte == ']',null,"invalid block end; square bracket was expected");}
 else if (currentBlock.type == NODE_CURLY_BRACKETS ) { MeanCS.assertion(inputByte == '}',null,"invalid block end; curly bracket was expected");}
 else { MeanCS.assertion(false,null,"unhandled block end: " + inputByte); }
 lastStart = -1;
 currentToken = currentBlock;
 currentExpr = currentToken.parent;
 currentBlock = currentExpr.parent;
}
private static void defineTransitions(ByteAutomata ba)
{
 stateSpace = ba.addState("space");
 stateName = ba.addState("name");
 stateNumber = ba.addState("number");
 ba.transition(stateSpace, whiteSpace, null);
 ba.transition(stateSpace, letters, () => { next(stateName); });
 ba.transition(stateSpace, numbers, () => { next(stateNumber); });
 ba.transition(stateSpace, expressionBreak, () => { exprBreak(); });
 ba.transition(stateSpace, blockStart, () => { addBlock();});
 ba.transition(stateSpace, blockEnd, () => { endBlock();});
 ba.transition(stateName, letters, null);
 ba.transition(stateName, whiteSpace, () => { addToken(NODE_TEXT); next(stateSpace); });
 ba.transition(stateName, blockStart, () => { addToken(NODE_TEXT); stay(); next(stateSpace); });
 ba.transition(stateName, blockEnd, () => { addToken(NODE_TEXT); stay(); next(stateSpace); });
 ba.transition(stateName, expressionBreak, () => { addToken(NODE_TEXT); exprBreak(); next(stateSpace); });
 ba.transition(stateNumber, numbers, null);
 ba.transition(stateNumber, whiteSpace, () => { addToken(NODE_NUMBER); next(stateSpace); });
 ba.transition(stateNumber, blockStart, () => { addToken(NODE_NUMBER); stay(); next(stateSpace); });
 ba.transition(stateNumber, blockEnd, () => { addToken(NODE_NUMBER); stay(); next(stateSpace); });
 ba.transition(stateNumber, expressionBreak, () => { addToken(NODE_NUMBER); exprBreak(); next(stateSpace); });
}
private static MNode lexInput(MInputStream input)
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
  MeanCS.print("closing parenthesis missing at the end");
  ba.ok = false;
 }
 if (!ba.ok)
 {
  ba.printError();
  root = null;
  return null;
 }
 MeanCS.verbose("\nFINISHED!");
 return root;
}
static internal MNode lex (string s)
{
 MInputArray input = new MInputArray(s);
 return lexInput(input);
}
static internal void printTitle (string s)
{
 MeanCS.print("\n    ByteAutomata (c) 2020, Meanwhale");
 MeanCS.print("    https://github.com/Meanwhale/ByteAutomata\n");
}
static internal void printArgInfo ()
{
 MeanCS.print("Command line arguments:");
 MeanCS.print("    [cmd] -i            read standard input, eg. [cmd] -i < ../test_script.txt");
 MeanCS.print("    [cmd] -t            run default test");
 MeanCS.print("    [cmd] \"[string]\"    parse [string]");
}
static internal void printStdinInfo ()
{
 MeanCS.print("Hit Ctrl-Z (Windows) or ^D (Linux/Mac) at the start of a line and press enter to finish.");
 MeanCS.print("Read from standard input...");
}
}
public class MNode {
internal int type;
internal int numChildren;
internal string data;
internal MNode next = null;
internal MNode child = null;
internal MNode parent = null;
public MNode (MNode _parent, int _type, string _data)
{
 data = _data;
 parent = _parent;
 type = _type;
 numChildren = 0;
}
public void printTree (bool deep)
{
 printTree(this, 0, deep);
 if (!deep) MeanCS.verbose("");
}
public void printTree (MNode _node, int depth, bool deep)
{
 System.Diagnostics.Debug.Assert(_node != null,"<printTree: empty node>");
 MNode node = _node;
 for (int i = 0; i < depth; i++) MeanCS.verbosen("  ");
 MeanCS.verbosen("[").print(node.data).print("]");
 // if (node.numChildren > 0) { VR(" + ")X(node.numChildren); }
 if (deep) MeanCS.verbose("");
 if (node.child != null && deep) printTree(node.child, depth + 1, deep);
 if (node.next != null) printTree(node.next, depth, deep);
}

}
public class NodeIterator {
internal MNode node;
public NodeIterator (MNode _node)
{
 node = _node;
}
public NodeIterator copy ()
{
 return new NodeIterator(node);
}
public int type ()
{
 return node.type;
}
public string data ()
{
 return node.data;
}
public MNode getChild()
{
 return node.child;
}
public MNode getNext()
{
 return node.next;
}
public MNode getParent()
{
 return node.parent;
}
public int numChildren ()
{
 return node.numChildren;
}
public bool hasNext()
{
 return node.next != null;
}
public bool hasChild()
{
 return node.child != null;
}
public bool hasParent()
{
 return node.parent != null;
}
public int nextType()
{
 System.Diagnostics.Debug.Assert(hasNext(),"nextType: no next");
 return node.next.type;
}
public void toNext()
{
 System.Diagnostics.Debug.Assert(hasNext(),"toNext: no next");
 node = node.next;
}
public bool toNextOrFalse()
{
 if (!hasNext()) return false;
 node = node.next;
 return true;
}
public void toChild()
{
 System.Diagnostics.Debug.Assert(hasChild(),"toChild: no child");
 node = node.child;
}
public void toParent()
{
 System.Diagnostics.Debug.Assert(hasParent(),"toParent: no parent");
 node = node.parent;
}
public void printTree(bool deep)
{
 node.printTree(deep);
}
}
public abstract class MInputStream {
public MInputStream ()
{
}
public abstract int getByteCount ();
public abstract byte readByte () ;
public abstract bool end ();
public abstract void close ();
public int readInt ()
{
 // bytes:	b[0] b[1] b[2] b[3] b[4] b[5] b[6] b[7]   ...
 // ints:	_________i[0]______|_________i[1]______|_ ...
 int i = 0;
 i |= (int)((readByte() << 24) & 0xff000000);
 i |= (int)((readByte() << 16) & 0x00ff0000);
 i |= (int)((readByte() << 8) & 0x0000ff00);
 i |= (int)((readByte()) & 0x000000ff);
 return i;
}
public void readArray (int [] trg, int numInts)
{
 MeanCS.assertion(numInts <= (getByteCount() * 4) + 1,null,"readArray: buffer overflow");
 for (int i=0; i < numInts; i++)
 {
  trg[i] = readInt();
 }
 System.Diagnostics.Debug.Assert(end(),"all bytes not read");
}
}
public class MInputArray : MInputStream {
byte [] buffer;
int size;
int index;
public MInputArray (string s)
{
 buffer = System.Text.Encoding.ASCII.GetBytes(s);
 size = buffer.Length;
 index = 0;
}
override
public int getByteCount ()
{
 return size;
}
override
public byte readByte ()
{
 MeanCS.assertion(!end(),null,"readInt: buffer overflow");
 return buffer[index++];
}
override
public bool end ()
{
 return index >= size;
}
override
public void close ()
{
}

}
} 
