#include <sys/types.h>
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

#define SERVER "localhost"
#define USER "user1"
#define PASS "tkfkdgody1!"
#define NAME "client_info"
#define PORT 3306

#define CONN_MAX 100

typedef struct _ACCOUNT{
	char Name[10];
	char ID[30];
	char pswd[30];
}account;

typedef struct _arg{
    int sock;
    struct sockaddr_in client_sock;
}Arg;

Arg **arg;

void *get_connection(void *);
void *get_info(void *);
int login(account *);
int client_stat(Arg *, char *);
int create_account(account *);
void update_info(Arg *,account *);
void init_mysql();
void complete_query_select(char *, char *);
void complete_query_update(char *, char *, char *, char *);
void complete_query_insert(char *, char *, char *, char *);

MYSQL *conn;
MYSQL_RES *res;
MYSQL_ROW row;


int main(int argc, char *argv[]){
    int server_sock;
    int client_sock;

	int con_index = 0;


    pthread_t conn_thread[CONN_MAX];
    pthread_t info_thread;

    struct sockaddr_in sock_addr;
    struct sockaddr_in client_addr;
    int size = sizeof(struct sockaddr_in);
    memset(&client_addr, 0x00, size);
    memset(&sock_addr,0x00,size);

    sock_addr.sin_family = AF_INET;
    sock_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    sock_addr.sin_port = htons(atoi(argv[1]));


    arg = malloc(sizeof(Arg *)*CONN_MAX);

    char *msg;

    if(argc != 2){
	fprintf(stderr,"<usage> : server_java.out \"port number\"\n");
	exit(0);
    }
    init_mysql();

    if((server_sock = socket(AF_INET,SOCK_STREAM,0)) == -1){
	perror("socket error ");
	exit(0);
    }

    if(bind(server_sock,(struct sockaddr *)&sock_addr , size) == -1){
	perror("bind error ");
	exit(0);
    }

    if(listen(server_sock, 5) == -1){
	perror("listen error ");
	exit(0);
    }

    printf("connection ready\n");

    while(1){
    	if((client_sock = accept(server_sock, (struct sockaddr *) &client_addr, &size)) <0){
		perror("accept error ");
		exit(0);
	    }

	    printf("client connected\n");
		arg[con_index] = malloc(sizeof(Arg));
	    arg[con_index]->sock = client_sock;
	    arg[con_index]->client_sock = client_addr;
	
	    pthread_create(&conn_thread[con_index],NULL,get_connection,(void *)arg[con_index]);
		con_index++;
    }
    mysql_close(conn);
    close(server_sock);
}

void *get_connection(void *arg){
	int ret;
	char menu;
    char tmp[100] = "";
	account *ac = malloc(sizeof(account));

    read(((Arg *)arg)->sock, &menu, 1);

	read(((Arg *)arg)->sock, tmp, sizeof(account));
	if(menu == '1'){
		strcpy(ac->Name,strtok(tmp, " "));
		strcpy(ac->ID,strtok(NULL," "));
	}
	else
		strcpy(ac->ID,strtok(tmp," "));
	strcpy(ac->pswd,strtok(NULL," "));

	//create account
	if(menu == '1'){
		update_info((Arg *)arg, ac);
		ret = create_account(ac);
		if(ret == 0){
			write(((Arg *)arg)->sock,"0\n",2);
		}
		else if(ret == 1){
			write(((Arg *)arg)->sock,"1\n",2);
		}
		close(((Arg *)arg)->sock);
		printf("connection end %d\n",((Arg *)arg)->sock);
		pthread_exit((void *)0);
	}
	//log in
	else if(menu == '2'){
		ret = login(ac);
		if(ret == 0){
			write(((Arg *)arg)->sock,"0\n",2);
		}
		else if(ret == 1){
			write(((Arg *)arg)->sock,"1\n",2);
		}
		else{
			write(((Arg *)arg)->sock,"2\n",2);
		}
		if((ret != 0) || (ret != 1)){
			if(client_stat(arg, ac->ID)==-1){
				return (void *)-1;
			}
		}
	}
	//log out
	else if(menu == '3'){

	}
}

int client_stat(Arg *arg, char *ID){
	char stat;
	char query[100] = "";
	int ret;
	while(1){
		read(((Arg *)arg)->sock, &stat, 1);
		if(stat == '0'){
			sprintf(query,"update table status set stat \'%c\' where ID = %s;",stat, ID);
			ret = mysql_query(conn,query);
			memset(query,0x00,100);
			if(ret != 0){
				printf("Cannot update stat=0\n");
				return -1;
			}
		}
		else{
			sprintf(query,"update table status set stat \'%c\' where ID = %s;",stat, ID);
			ret = mysql_query(conn, query);
			memset(query,0x00,100);
			if(ret != 0){
				printf("Cannot update stat=1\n");
				return -1;
			}
		}
	}
}

void update_info(Arg *arg, account *ac){
//get client table info;
	char *IP = inet_ntoa(arg->client_sock.sin_addr);
    char port[6]="";
    sprintf(port,"%d",ntohs(arg->client_sock.sin_port));
    char query[100]="";

    complete_query_select(query,ac->Name);
    if(mysql_query(conn,query) != 0){
		perror("mysql_query error ");
		return ;
    }
    else{
		res = mysql_store_result(conn);
		if(res->row_count == 0){
			complete_query_insert(query, ac->ID, IP, port);
			mysql_query(conn,query);
		}
		else{
			complete_query_update(query, ac->ID, IP, port);
			mysql_query(conn,query);
		}
    }
    mysql_free_result(res);
}

int create_account(account *ac){
	char query[100]="";
	int ret;
	memset(query,0x00,100);
	sprintf(query,"select ID from login where ID = '%s';",ac->ID);

	if(mysql_query(conn,query)==0){
		res = mysql_store_result(conn);
		if(res->row_count == 0){
			memset(query,0x00,100);
			sprintf(query,"insert into login values(\'%s\', \'%s\', \'%s\');",ac->Name, ac->ID,ac->pswd);
			mysql_query(conn,query);
			memset(query,0x00,100);
			sprintf(query,"insert into status values(\'%s\', '1');",ac->ID);
			mysql_query(conn,query);
			ret = 0;
		}
		else
			ret = 1;
	}
	mysql_free_result(res);
	return ret;
}

int login(account *ac){
	char query[100]="";
	int ret;
	memset(query,0x00,100);
	sprintf(query,"select pswd from login where ID = '%s';",ac->ID);

	mysql_query(conn,query);
	res = mysql_store_result(conn);
	if(res->row_count == 0){
		mysql_free_result(res);
		ret = 0;
	}
	else{
		row = mysql_fetch_row(res);
		if(strcmp(ac->pswd,row[0])==0){
			mysql_free_result(res);
			memset(query,0x00,100);
			sprintf(query,"update login set status = 1 where ID = '%s' and pswd = '%s';",ac->ID,ac->pswd);
			ret = 2;
		}
		else{
			mysql_free_result(res);
			ret = 1;
		}
	}
	return ret;
}

void init_mysql(){
    if(!(conn = mysql_init((MYSQL *)NULL))){
	perror("mysql_init error ");
	exit(1);
    }
    
    if(mysql_real_connect(conn,SERVER,USER,PASS,NULL,PORT,NULL,0) == NULL){
	fprintf(stderr,"mysql_real_connect error : %s\n",mysql_error(conn));
	exit(1);
    }

    if(mysql_select_db(conn,NAME) != 0){
	mysql_close(conn);
	perror("mysql_select_db error ");
	exit(1);
    }

    printf("mysql_initiated\n");
}

void complete_query_select(char *query, char *ID){
    memset(query,0x00,100);
    sprintf(query,"select ID, IP, port from client where ID = \'%s\';",ID);
}

void complete_query_insert(char *query, char *ID, char *IP, char *port){
    memset(query,0x00,100);
    sprintf(query,"insert into client(ID, IP, port) values(\'%s\',\'%s\',\'%s\');",ID,IP,port);
}

void complete_query_update(char *query, char *ID, char *IP, char *port){
    memset(query,0x00,100);
    sprintf(query,"update client set IP = \'%s\',port = \'%s\' where ID = \'%s\';",IP,port,ID);
}
