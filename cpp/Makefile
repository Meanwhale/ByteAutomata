SRC = main.cpp src/code.cpp
CC = g++
FLAGS = -std=c++14
TARGET_EXE = byteautomata
DEBUG_FLAGS = -fsanitize=leak -fsanitize=address
RELEASE_FLAGS = -Os -s

release: $(SRC)
	@echo --- ByteAutomata RELEASE ---
	$(CC) $(FLAGS) $(RELEASE_FLAGS) $(SRC) -o $(TARGET_EXE) 
	
debug: $(SRC)
	@echo --- ByteAutomata DEBUG ---
	$(CC) $(FLAGS) $(DEBUG_FLAGS) $(SRC) -o $(TARGET_EXE) 

clean:
	@echo --- ByteAutomata CLEAN ---
	rm $(TARGET_EXE)
