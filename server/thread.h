#include "./struct_type.h"

void *get_connection(void *);
void heart_beat(Arg *, char *);
void req(Arg *, char *);
void port_update(char *, char *);

int login(account *);
int logout(char *);
int client_stat(Arg *, char *);

int create_account(account *);
void update_info(Arg *, account *);

pthread_mutex_t a_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t udp_mutex = PTHREAD_MUTEX_INITIALIZER;

pthread_cond_t conA = PTHREAD_COND_INITIALIZER;

pthread_t hb_thread[CONN_MAX];


int con_index;
int hb_index;

int server_sock;
int udp_sock;

struct sockaddr_in sock_udp;
struct sockaddr_in client_udp;

extern Arg **arg;
extern HB **hb;

extern MYSQL *conn;
extern MYSQL_RES *res;
extern MYSQL_ROW row;

extern void complete_query_select(char *, char *);
extern void complete_query_update(char *, char *, char *, char *);
extern void complete_query_insert(char *, char *, char *, char *);

