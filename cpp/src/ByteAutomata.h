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
// declarations
ByteAutomata();
uint8_t addState(const char *);
void transition(uint8_t state, const char *, void (* action)());
void fillTransition(uint8_t state, void (* action)());
uint8_t addAction(void (* action)());
void next(uint8_t nextState);
void print();
bool step(uint8_t input);
~ByteAutomata();
};
} // namespace meanscript
// C++ header END
