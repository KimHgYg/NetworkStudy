#include <sys/types.h>
#include <sys/time.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <error.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include "/usr/include/mysql/mysql.h"

#define CONN_MAX 100

typedef struct _ARG {
	int sock;
	struct sockaddr_in client_sock;
}Arg;

typedef struct _HB_ARg {
	Arg hb;
	char *ID;
}HB;

typedef struct _ACCOUNT {
	char Name[10];
	char ID[30];
	char pswd[30];
}account;

