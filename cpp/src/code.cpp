#include "../main.hpp" 
namespace meanscript { 
int32_t MAX_STATES = 32;
ByteAutomata::ByteAutomata()
{
 ok = true;
 currentInput = 0;
 currentState = 0;
 stateCounter = 0;
 actionCounter = 0;
 tr.reset(MAX_STATES * 256);
 for (int32_t i=0; i<MAX_STATES * 256; i++) tr[i] = (uint8_t)0xff;
}
ByteAutomata::~ByteAutomata() { }
void ByteAutomata::print ()
{
 for (int32_t i = 0; i <= stateCounter; i++)
 {
  vrbout()<<("state: ")<<(i)<<std::endl;
  for (int32_t n = 0; n < 256; n++)
  {
   uint8_t foo = tr[(i * 256) + n];
   if (foo == 0xff) vrbout()<<(".");
   else vrbout()<<(foo);
  }
  vrbout()<<("")<<std::endl;
 }
}
uint8_t ByteAutomata::addState (const char * stateName)
{
 stateCounter++;
 stateNames.insert(std::make_pair((int32_t)stateCounter,stateName));;
 return stateCounter;
}
void ByteAutomata::transition (uint8_t state, const char * input, void (* action)())
{
 uint8_t actionIndex = 0;
 if (action != 0)
 {
  actionIndex = addAction(action);
 }
 uint8_t * bytes = (uint8_t *)input;
 int32_t i = 0;
 while (bytes[i] != 0)
 {
  tr[(state * 256) + bytes[i]] = actionIndex;
  i++;
 }
 //DEBUG(VR("New Transition added: id ")X(actionIndex)XO);
}
void ByteAutomata::fillTransition (uint8_t state, void (* action)())
{
 uint8_t actionIndex = 0;
 if (action != 0) actionIndex = addAction(action);
 for (int32_t i=0; i<256; i++)
 {
  tr[(state * 256) + i] = actionIndex;
 }
 //DEBUG(VR("New Transition filled: id ")X(actionIndex)XO);
}
uint8_t ByteAutomata::addAction (void (* action)())
{
 actionCounter++;
 actions[actionCounter] = action;
 return actionCounter;
}
void ByteAutomata::next (uint8_t nextState)
{
 currentState = nextState;
 {vrbout()<<("Next state: ")<<(stateNames[(int32_t)currentState])<<std::endl;};
}
bool ByteAutomata::step (uint8_t input)
{
 currentInput = input;
 int32_t index = (currentState * 256) + input;
 uint8_t actionIndex = tr[index];
 if (actionIndex == 0) return true; // stay on same state and do nothing else
 if (actionIndex == 0xff||actionIndex < 0)
 {
  ok = false;
  return false; // end
 }
 void (* act)() = actions[actionIndex];
 if (act == 0)
 {
  ASSERT(false, "invalid action index");
 }
 act();
 return true;
}
const char * letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
const char * numbers = "1234567890";
const char * whitestateSpace = " \t\n\r";
const char * linebreak = "\n\r";
const char * expressionBreak = ",;";
const char * blockStart = "([{";
const char * blockEnd = ")]}";
constexpr int32_t BUFFER_SIZE = 512;
constexpr int32_t CFG_MAX_NAME_LENGTH = 128;
Array<uint8_t> tmp(BUFFER_SIZE);
Array<uint8_t> buffer(BUFFER_SIZE);
uint8_t stateSpace, stateName, stateNumber;
ByteAutomata* automata;
bool stayNextStep, running;
int32_t index, lastStart;
uint8_t inputByte = 0;
MNode* root;
MNode* currentBlock;
MNode* currentExpr;
MNode* currentToken;
void next(uint8_t state)
{
 lastStart = index;
 (*automata).next(state);
}
void stay()
{
 ASSERT(!stayNextStep, "can't go backwards twice");
 stayNextStep = true;
}
std::string getNewName()
{
 int32_t start = lastStart;
 int32_t length = index - start;
 {if (!(length < CFG_MAX_NAME_LENGTH)) EXIT("name is too long")};
 int32_t i = 0;
 for (; i < length; i++)
 {
  tmp[i] = buffer[start++ % BUFFER_SIZE];
 }
 tmp[i] = '\0';
 return std::string((char*)tmp.get());
}
void addExpr()
{
 MNode* expr = new MNode(currentBlock, 0, "<EXPR>");
 (*currentExpr).next = expr;
 currentExpr = expr;
 currentToken = 0;
}
void addToken(int32_t tokenType)
{
 std::string data = getNewName();
 vrbout()<<("NEW TOKEN: ")<<(data)<<std::endl;
 MNode* token = new MNode(currentExpr, tokenType, data);
 if (currentToken == 0) (*currentExpr).child = token;
 else (*currentToken).next = token;
 (*currentExpr).numChildren++;
 currentToken = token;
 lastStart = index;
}
void endBlock(int32_t blockType)
{
 {if (!((currentBlock != 0))) EXIT("unexpected block end")};
 // Check that block-end character is the right one.
 // The 'type' is block start/end character's ASCII code.
 if ((*currentBlock).type == 40 ) {{if (!(blockType == 41)) EXIT("invalid block end; parenthesis was expected")}}
 else if ((*currentBlock).type == 91 ) {{if (!(blockType == 93)) EXIT("invalid block end; square bracket was expected")}}
 else if ((*currentBlock).type == 123) {{if (!(blockType == 125)) EXIT("invalid block end; curly bracket was expected")}}
 else { {if (!(false)) EXIT("unhandled block end: " << blockType)}; }
 lastStart = -1;
 currentToken = currentBlock;
 currentExpr = (*currentToken).parent;
 currentBlock = (*currentExpr).parent;
}
void exprBreak()
{
 if (currentBlock != 0) (*currentBlock).numChildren ++;
 lastStart = -1;
 addExpr();
}
void addBlock(int32_t blockType)
{
 vrbout()<<("add block: ")<<(blockType)<<std::endl;
 lastStart = -1;
 std::string tmp123("<BLOCK>");
 MNode* block = new MNode(currentExpr, blockType, tmp123);
 if (currentToken == 0)
 {
  (*currentExpr).child = block;
  currentToken = block;
 }
 else
 {
  (*currentToken).next = block;
 }
 (*currentExpr).numChildren++;
 currentBlock = block;
 MNode* expr = new MNode(currentBlock, 0, "<EXPR>");
 (*currentBlock).child = expr;
 currentExpr = expr;
 currentToken = 0;
}
void defineTransitions(ByteAutomata & ba)
{
 stateSpace = ba.addState("space");
 stateName = ba.addState("name");
 stateNumber = ba.addState("number");
 ba.transition(stateSpace, whitestateSpace, 0);
 ba.transition(stateSpace, letters, []() { next(stateName); });
 ba.transition(stateSpace, numbers, []() { next(stateNumber); });
 ba.transition(stateSpace, expressionBreak, []() { exprBreak(); });
 ba.transition(stateSpace, blockStart, []() { addBlock((int)inputByte);});
 ba.transition(stateSpace, blockEnd, []() { endBlock((int)inputByte);});
 ba.transition(stateName, letters, 0);
 ba.transition(stateName, whitestateSpace, []() { addToken(0); next(stateSpace); });
 ba.transition(stateName, blockStart, []() { addToken(0); stay(); next(stateSpace); });
 ba.transition(stateName, blockEnd, []() { addToken(0); stay(); next(stateSpace); });
 ba.transition(stateName, expressionBreak, []() { addToken(0); exprBreak(); next(stateSpace); });
 ba.transition(stateNumber, numbers, 0);
 ba.transition(stateNumber, whitestateSpace, []() { addToken(0); next(stateSpace); });
 ba.transition(stateNumber, blockStart, []() { addToken(0); stay(); next(stateSpace); });
 ba.transition(stateNumber, blockEnd, []() { addToken(0); stay(); next(stateSpace); });
 ba.transition(stateNumber, expressionBreak, []() { addToken(0); exprBreak(); next(stateSpace); });
}
MNode* lexInput(MInputStream & input)
{
 ByteAutomata ba = ByteAutomata();
 automata = (&(ba));
 defineTransitions(ba);
 ba.next((uint8_t)1);
 root = new MNode(0, 0, "<ROOT>");
 currentExpr = root;
 currentBlock = 0;
 currentToken = 0;
 lastStart = 0;
 running = true;
 stayNextStep = false;
 int32_t lineNumber = 1;
 inputByte = 0;
 index = 0;
 while ((!input.end() || stayNextStep) && running && ba.ok)
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
  std::cout<<("[ ")<<((char)(inputByte))<<(" ]")<<std::endl;
  running = ba.step(inputByte);
 }
 if (!stayNextStep) index++;
 ba.step((uint8_t)' '); // ended cleanly: last white space
 if (currentBlock != 0)
 {
  std::cout<<("closing parenthesis missing at the end")<<std::endl;
  ba.ok = false;
 }
 if (!running || !(ba.ok))
 {
  std::cout<<("ERROR: parser state [")<<(ba.stateNames[(int32_t)ba.currentState])<<("]")<<std::endl;
  std::cout<<("Line ")<<(lineNumber)<<(": \"");
  // print nearby code
  int32_t start = index-1;
  while (start > 0 && index - start < BUFFER_SIZE && (char)buffer[start % BUFFER_SIZE] != '\n')
  {
   start --;
  }
  while (++start <= index)
  {
   vrbout()<<((char)(buffer[start % BUFFER_SIZE]));
  }
  std::cout<<("\"")<<std::endl;
  { delete root; root = 0; };
  return 0;
 }
 vrbout()<<("\nFINISHED!")<<std::endl;
 return root;
}
MNode* MicroLexer::lex (std::string s)
{
 MInputArray input = MInputArray(s);
 return lexInput(input);
}
MNode::MNode (MNode* _parent, int32_t _type, const std::string & _data)
  : data(_data)
{
 parent = _parent;
 type = _type;
 numChildren = 0;
}
void MNode::printTree (bool deep)
{
 printTree(this, 0, deep);
 if (!deep) vrbout()<<("")<<std::endl;
}
void MNode::printTree (MNode* _node, int32_t depth, bool deep)
{
 ASSERT(_node != 0, "<printTree: empty node>");
 MNode & node = (*_node);
 for (int32_t i = 0; i < depth; i++) vrbout()<<("  ");
 vrbout()<<("[")<<(node.data)<<("]");
 // if (node.numChildren > 0) { VR(" + ")X(node.numChildren); }
 if (deep) vrbout()<<("")<<std::endl;
 if (node.child != 0 && deep) printTree(node.child, depth + 1, deep);
 if (node.next != 0) printTree(node.next, depth, deep);
}
MNode::~MNode() { delete next; delete child; };
NodeIterator::NodeIterator (MNode* _node)
{
 node = _node;
}
NodeIterator NodeIterator::copy ()
{
 return NodeIterator(node);
}
int32_t NodeIterator::type ()
{
 return (*node).type;
}
std::string NodeIterator::data ()
{
 return (*node).data;
}
MNode* NodeIterator::getChild()
{
 return (*node).child;
}
MNode* NodeIterator::getNext()
{
 return (*node).next;
}
MNode* NodeIterator::getParent()
{
 return (*node).parent;
}
int32_t NodeIterator::numChildren ()
{
 return (*node).numChildren;
}
bool NodeIterator::hasNext()
{
 return (*node).next != 0;
}
bool NodeIterator::hasChild()
{
 return (*node).child != 0;
}
bool NodeIterator::hasParent()
{
 return (*node).parent != 0;
}
int32_t NodeIterator::nextType()
{
 ASSERT(hasNext(), "nextType: no next");
 return (*(*node).next).type;
}
void NodeIterator::toNext()
{
 ASSERT(hasNext(), "toNext: no next");
 node = (*node).next;
}
bool NodeIterator::toNextOrFalse()
{
 if (!hasNext()) return false;
 node = (*node).next;
 return true;
}
void NodeIterator::toChild()
{
 ASSERT(hasChild(), "toChild: no child");
 node = (*node).child;
}
void NodeIterator::toParent()
{
 ASSERT(hasParent(), "toParent: no parent");
 node = (*node).parent;
}
void NodeIterator::printTree(bool deep)
{
 (*node).printTree(deep);
}
MInputStream::MInputStream ()
{
}
int32_t MInputStream::readInt ()
{
 // bytes:	b[0] b[1] b[2] b[3] b[4] b[5] b[6] b[7]   ...
 // ints:	_________i[0]______|_________i[1]______|_ ...
 int32_t i = 0;
 i |= (readByte() << 24) & 0xff000000;
 i |= (readByte() << 16) & 0x00ff0000;
 i |= (readByte() << 8) & 0x0000ff00;
 i |= (readByte()) & 0x000000ff;
 return i;
}
void MInputStream::readArray (Array<int> & trg, int32_t numInts)
{
 {if (!(numInts <= (getByteCount() * 4) + 1)) EXIT("readArray: buffer overflow")};
 for (int32_t i=0; i < numInts; i++)
 {
  trg[i] = readInt();
 }
 ASSERT(end(), "all bytes not read");
}
MInputArray::MInputArray (std::string & s)
{
 buffer.clone((uint8_t*)s.c_str(), s.length());
 size = buffer.length();
 index = 0;
}

int32_t MInputArray::getByteCount ()
{
 return size;
}

uint8_t MInputArray::readByte ()
{
 {if (!(!end())) EXIT("readInt: buffer overflow")};
 return buffer[index++];
}

bool MInputArray::end ()
{
 return index >= size;
}

void MInputArray::close ()
{
}
MInputArray::~MInputArray() { ; }
} 
