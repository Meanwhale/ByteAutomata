#ifndef _ARRAY_H_
#define _ARRAY_H_
#include "main.hpp"
template <class T> class Array
{
protected:

	T*				data;
	int				size; // size of reserved array; 0 if not allocated (, negative on error?)


public:

	explicit Array() :
		data(0),
		size(0)
	{
	}
	explicit Array(int s) :
		data(new T[s]),
		size(s)
	{
	}
	explicit Array(T * a, int s) :
		data(a),
		size(s)
	{
	}

	~Array()
	{
		clear();
	}

	void clear()
	{
		if (data != 0)
		{
			delete[] data;
			data = 0;
			size = 0;
		}
	}

	void clone(T * a, int s)
	{
		delete[] data;
		data = new T[s];
		size = s;
		for (int i=0; i<s; i++) data[i] = a[i];
	}

	void reset(int s)
	{
		delete[] data;
		data = new T[s];
		size = s;
	}
	bool inRange(int i)
	{
		return i >= 0 && i < size;
	}

	void fill(T a)
	{
		for (int i = 0; i < size; i++)
			data[i] = a;
	}


	// ACCESSORS

	T * get() const
	{
		return data;
	}
	T & get(int i)
	{
		ASSERT(inRange(i), "out of range");
		return data[i];
	}
	T & operator[] (int i)
	{
		ASSERT(inRange(i), "out of range");
		return get(i);
	}
	int length()
	{
		return size;
	}

private:
};
#endif // _ARRAY_H_
