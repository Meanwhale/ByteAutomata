#ifndef _ALLOC_COUNTER_H_
#define _ALLOC_COUNTER_H_

#include <new>
#include <cstdlib>
#include <iostream>
#include <string>
#include <vector>
#include <sstream>

void* operator new  (std::size_t count );
void* operator new[](std::size_t count );
void* operator new  (std::size_t count, const std::nothrow_t& tag);
void* operator new[](std::size_t count, const std::nothrow_t& tag);

void operator delete  (void* ptr); 	
void operator delete[](void* ptr);
void operator delete  (void* ptr, const std::nothrow_t& tag);
void operator delete[](void* ptr, const std::nothrow_t& tag);
void operator delete  (void* ptr, std::size_t sz);
void operator delete[](void* ptr, std::size_t sz);

#endif