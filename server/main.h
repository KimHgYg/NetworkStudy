#include "./struct_type.h"

pthread_t conn_thread[CONN_MAX];

extern struct sockaddr_in sock_udp;

extern int con_index;
extern int hb_index;

extern int server_sock;
extern int udp_sock;

extern MYSQL *conn;

Arg **arg;
HB **hb;

extern void init_mysql();
extern void *get_connection(void *);
