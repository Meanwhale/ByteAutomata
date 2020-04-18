// Auto-generated: do not edit.
namespace meanscript {
class ByteAutomata
{ public:
 bool ok;
 Array<uint8_t> tr;
 uint8_t currentInput;
 uint8_t currentState;
 std::map<int, std::string> stateNames;
 void (*actions[64])();
 uint8_t stateCounter;
 uint8_t actionCounter; // 0 = end
// running:
 uint8_t inputByte = 0;
 int32_t index = 0;
 int32_t lineNumber = 0;
 bool stayNextStep = false;
 bool running = false;
 Array<uint8_t> buffer;
 Array<uint8_t> tmp;
// declarations
ByteAutomata();
uint8_t addState(const char *);
void transition(uint8_t state, const char *, void (* action)());
void fillTransition(uint8_t state, void (* action)());
uint8_t addAction(void (* action)());
void next(uint8_t nextState);
void print();
void stay();
void printError();
std::string getString(int32_t,int32_t);
bool step(uint8_t input);
int32_t getIndex();
int32_t getInputByte();
void run(MInputStream & input);
~ByteAutomata();
};
} // namespace meanscript
// C++ header END
