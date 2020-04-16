// Auto-generated: do not edit.
namespace meanscript {
class MInputStream
{ public:
MInputStream();
virtual int32_t getByteCount() = 0;
virtual uint8_t readByte() = 0;
virtual bool end() = 0;
virtual void close() = 0;
int32_t readInt ();
void readArray (Array<int> & trg, int32_t num);
};
} // namespace meanscript
// C++ header END
