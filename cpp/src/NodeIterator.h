// Auto-generated: do not edit.
namespace meanscript {
class NodeIterator
{ public:
 MNode* node;
MNode* getChild();
MNode* getNext();
MNode* getParent();
int32_t type();
int32_t nextType();
int32_t numChildren();
std::string data();
bool hasNext();
bool hasChild();
bool hasParent();
bool toNextOrFalse();
void toNext();
void toChild();
void toParent();
void printTree(bool);
NodeIterator copy();
NodeIterator(MNode* _node);
NodeIterator (const NodeIterator & x)
{
 node = x.node;
};
private: // hide
//CLASS_NAME(const CLASS_NAME&) = default;
NodeIterator() = delete;
NodeIterator & operator = (const NodeIterator &) = delete;
NodeIterator & operator & () = delete;
NodeIterator * operator * () = delete;
};
} // namespace meanscript
// C++ header END
