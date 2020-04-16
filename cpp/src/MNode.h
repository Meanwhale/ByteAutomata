// Auto-generated: do not edit.
namespace meanscript {
class MNode
{ public:
 int32_t type;
 int32_t numChildren;
 std::string data;
 MNode* next = 0;
 MNode* child = 0;
 MNode* parent = 0;
MNode(MNode* _parent, int32_t _type, const std::string & _data);
void printTree (bool deep);
void printTree (MNode* node, int32_t depth, bool deep);
~MNode();
};
} // namespace meanscript
// C++ header END
