#include "./mysql.h"

void init_mysql() {
	if (!(conn = mysql_init((MYSQL *)NULL))) {
		perror("mysql_init error ");
		exit(1);
	}

	if (mysql_real_connect(conn, SERVER, USER, PASS, NULL, PORT, NULL, 0) == NULL) {
		fprintf(stderr, "mysql_real_connect error : %s\n", mysql_error(conn));
		exit(1);
	}

	if (mysql_select_db(conn, NAME) != 0) {
		mysql_close(conn);
		perror("mysql_select_db error ");
		exit(1);
	}

	printf("mysql_initiated\n");
}

void complete_query_select(char *query, char *ID) {
	memset(query, 0x00, 150);
	sprintf(query, "select ID, IP, port from client where ID = \'%s\';", ID);
}

void complete_query_insert(char *query, char *ID, char *IP, char *port) {
	memset(query, 0x00, 150);
	sprintf(query, "insert into client(ID, IP, port) values(\'%s\',\'%s\',\'%s\');", ID, IP, port);
}

void complete_query_update(char *query, char *ID, char *IP, char *port) {
	memset(query, 0x00, 150);
	sprintf(query, "update client set IP = \'%s\',port = \'%s\' where ID = \'%s\';", IP, port, ID);
}
