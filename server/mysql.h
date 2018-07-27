#include "./struct_type.h"

#define SERVER "localhost"
#define USER "user1"
#define PASS "tkfkdgody1!"
#define NAME "client_info"
#define PORT 3306

void init_mysql();

void complete_query_select(char *, char *);
void complete_query_update(char *, char *, char *, char *);
void complete_query_insert(char *, char *, char *, char *);

MYSQL *conn;
MYSQL_RES *res;
MYSQL_ROW row;
