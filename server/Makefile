CC = gcc
CFLAGS = -W -g
LINK = -lpthread -lmysqlclient
TARGET = server_exe
OBJECTS = main.o mysql.o thread.o

$(TARGET) : $(OBJECTS)
			$(CC) $(CFLAGS) -o $@ $^ $(LINK)

main.o : main.h main.c struct_type.h
mysql.o : mysql.h mysql.c
thread.o : thread.h thread.c struct_type.h

clean_object :
	rm -f *.o

run :
	./$(TARGET) 3000

clean : 
	rm -f *.o
	rm -f $(TARGET)
