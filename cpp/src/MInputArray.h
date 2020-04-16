// Auto-generated: do not edit.
namespace meanscript {
class MInputArray
: public MInputStream
{ public:
Array<uint8_t> buffer;
int32_t size;
int32_t index;
MInputArray(std::string & s);
//CLASS_NAME(INT_ARRAY_REF arr);
int32_t getByteCount() override;
uint8_t readByte() override;
bool end() override;
void close() override;
~MInputArray();
};
} // namespace meanscript
// C++ header END
