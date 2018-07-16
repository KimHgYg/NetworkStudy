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

typedef struct _ARG{
    int sock;
    struct sockaddr_in client_sock;
}Arg;

typedef struct _HB_ARg{
	Arg hb;
	char *ID;
}HB;


Arg **arg;
HB **hb;


void *get_connection(void *);
void *heart_beat(void *);

int login(account *);
int logout(char *);
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

pthread_mutex_t a_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t server_sock_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t conA = PTHREAD_COND_INITIALIZER;

pthread_t conn_thread[CONN_MAX];
pthread_t hb_thread[CONN_MAX];

int con_index;
int hb_index;

int server_sock;

int main(int argc, char *argv[]){
    int client_sock;

    struct sockaddr_in sock_addr;
    struct sockaddr_in client_addr;
    int size = sizeof(struct sockaddr_in);
    memset(&client_addr, 0x00, size);
    memset(&sock_addr,0x00,size);

    sock_addr.sin_family = AF_INET;
    sock_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    sock_addr.sin_port = htons(atoi(argv[1]));

    arg = malloc(sizeof(Arg *)*CONN_MAX);
	hb = malloc(sizeof(HB *)*CONN_MAX);

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

	_Bool optval = 1;
	setsockopt(server_sock, SOL_SOCKET,SO_REUSEADDR,(char *)&optval,sizeof(optval));

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
		pthread_cond_wait(&conA, &server_sock_mutex);
    }
	int i=0;
	free(arg);
    mysql_close(conn);
    close(server_sock);
}

void *get_connection(void *arg){
	int ret;
	int client_sock;
	int size = sizeof(struct sockaddr_in);
	char menu;
    char tmp[100] = "";

	struct sockaddr_in client_addr;
    memset(&client_addr,0x00,size);

	account *ac = malloc(sizeof(account));

    read(((Arg *)arg)->sock, &menu, 1);

	if(read(((Arg *)arg)->sock, tmp, sizeof(account))==0){
		printf("Conncetion Cancled\n");
		return (void *)0;
	}
	if(menu == '1'){
		strcpy(ac->Name,strtok(tmp, " "));
		strcpy(ac->ID,strtok(NULL," "));
	}
	else
		strcpy(ac->ID,strtok(tmp," "));
	strcpy(ac->pswd,strtok(NULL," "));

	//create account
	if(menu == '1'){
		pthread_cond_signal(&conA);
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
		update_info((Arg *)arg,ac);
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
			if((client_sock = accept(server_sock, (struct sockaddr *) &client_addr, &size)) <0){
				perror("accept error2 ");
				exit(0);
		    }
	
		    printf("heart_beat connected%d to %d\n",((Arg *)arg)->sock,client_sock);
			hb[hb_index] = malloc(sizeof(HB));
			hb[hb_index]-> ID = malloc(30);
		    hb[hb_index]->hb.sock = client_sock;
		    hb[hb_index]->hb.client_sock = client_addr;
			strcpy(hb[hb_index]->ID,ac->ID);
	
			pthread_create(&hb_thread[con_index],NULL,heart_beat,(void *)hb[hb_index]);
			hb_index++;
		}
		pthread_cond_signal(&conA);
	}
	//request opponent info
	while(1){
		char query[100] = "";
		char reqID[30] = "";
		char option = '0';
		char text[30] = "";

    	if(read(((Arg *)arg)->sock, &option, 1) == 0){
			close(((Arg *)arg)->sock);
			printf("server connection closed %d\n",((Arg *)arg)->sock);
			return (void *)0;
		}

		if(option == '0'){
			memset(query,0x00,30);
			memset(text ,0x00,30);
			if(read(((Arg *)arg)->sock,reqID,30) == 0){
				printf("server connection closed %d\n",((Arg *)arg)->sock);
				close(((Arg *)arg)->sock);
				return (void *)0;
			}
			sprintf(query,"select stat from status where ID = \'%s\';",reqID);
			pthread_mutex_lock(&a_mutex);
			if((ret = mysql_query(conn,query)) != 0){
				printf("Cannot select stat\n");
				return (void *)-1;
			}
			res = mysql_store_result(conn);
			if(res->row_count == 1){
				row = mysql_fetch_row(res);
				strcpy(text,row[0]);
				strcat(text,"\n");
				write(((Arg *)arg)->sock,text,strlen(text));
				if(!strcmp(row[0],"1")){
					memset(text,0x00,30);
					memset(query,0x00,100);
					sprintf(query,"select IP from client where ID = \'%s\';",reqID);
					if(ret = mysql_query(conn,query)){
						printf("Cannot select IP\n");
						return (void *)-1;
					}
					res = mysql_store_result(conn);
					if(res->row_count == 1){
						row = mysql_fetch_row(res);
						strcpy(text,row[0]);
						strcat(text,"\n");
						write(((Arg *)arg)->sock,text,strlen(text));
						printf("export IP = %s\n",row[0]);
					}
				}
				printf("export user info %s to %d\n",reqID,((Arg *)arg)->sock);
			}
			else{
				write(((Arg *)arg)->sock,"2\n",2);
				printf("export FAILED user info %s to %d\n",reqID,((Arg *)arg)->sock);
			}
			pthread_mutex_unlock(&a_mutex);
		}
	}
	free(ac);
	close(((Arg *)arg)->sock);
	free((Arg *)arg);
}

//check heartbit
int client_stat(Arg *hb, char *ID){
	char stat = -1;
	char query[100] = "";
	int ret;
	int tmp;
	struct timeval tv;

	tv.tv_sec = 6;
	tv.tv_usec = 50;
	setsockopt(((Arg *)hb)->sock,SOL_SOCKET, SO_RCVTIMEO,(const char*)&tv,sizeof(tv));

	while(1){
		if((tmp = read(((Arg *)hb)->sock, &stat, 1)) == -1){
			return logout(ID);
		}
		//alive
		if(stat == '1'){
			sprintf(query,"update status set stat = \'%c\' where ID = \'%s\';",stat, ID);
			pthread_mutex_lock(&a_mutex);
			ret = mysql_query(conn,query);
			pthread_mutex_unlock(&a_mutex);
			memset(query,0x00,100);
			if(ret != 0){
				printf("Cannot update stat=0\n");
				return -1;
			}
    		sprintf(query,"update client set IP = \'%s\',port = \'%d\' where ID = \'%s\';",inet_ntoa(hb->client_sock.sin_addr),ntohs(hb->client_sock.sin_port),ID);
			pthread_mutex_lock(&a_mutex);
			ret = mysql_query(conn,query);
			pthread_mutex_unlock(&a_mutex);
			memset(query,0x00,100);
			if(ret != 0){
				printf("Cannot update stat = 0-2\n");
				return -1;
			}
			printf("HB %d\n",((Arg *)hb)->sock);
		}
		//error
		else if(stat == '0'){
			memset(query,0x00,100);
			printf("client down\n");
			sprintf(query,"update status set stat = \'%c\' where ID = \'%s\';",stat,ID);
			pthread_mutex_lock(&a_mutex);
			ret = mysql_query(conn,query);
			pthread_mutex_unlock(&a_mutex);
			if(ret != 0){
				printf("Cannot update stat = 2\n");
				return -1;
			}
			break;
		}
		stat = 0;
		tmp = 0;
	}
	return 0;
}

int logout(char *ID){
	pthread_mutex_lock(&a_mutex);
	char query[100] = "";
	int ret;
	sprintf(query,"update status set stat = \'0\' where ID = \'%s\';",ID);
	ret = mysql_query(conn,query);
	if(ret != 0)
		printf("logout failed %s\n",ID);
	else
		printf("logout!\n");
	pthread_mutex_unlock(&a_mutex);
	return ret;
}

void update_info(Arg *arg, account *ac){
//get client table info;
	char *IP = inet_ntoa(arg->client_sock.sin_addr);
    char port[6]="";
    sprintf(port,"%d",ntohs(arg->client_sock.sin_port));
    char query[100]="";

    complete_query_select(query,ac->ID);
	pthread_mutex_lock(&a_mutex);
    if(mysql_query(conn,query) != 0){
		perror("mysql_query error ");
		pthread_mutex_unlock(&a_mutex);
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
	pthread_mutex_unlock(&a_mutex);
}

int create_account(account *ac){
	char query[100]="";
	int ret;
	memset(query,0x00,100);
	sprintf(query,"select ID from login where ID = '%s';",ac->ID);
	pthread_mutex_lock(&a_mutex);
	if(mysql_query(conn,query)==0){
		res = mysql_store_result(conn);
		if(res->row_count == 0){
			memset(query,0x00,100);
			sprintf(query,"insert into login values(\'%s\', \'%s\', \'%s\');",ac->Name, ac->ID,ac->pswd);
			mysql_query(conn,query);
			memset(query,0x00,100);
			sprintf(query,"insert into status values(\'%s\', '0');",ac->ID);
			mysql_query(conn,query);
			ret = 0;
		}
		else
			ret = 1;
	}
	mysql_free_result(res);
	pthread_mutex_unlock(&a_mutex);
	return ret;
}

int login(account *ac){
	char query[100]="";
	int ret;
	memset(query,0x00,100);
	sprintf(query,"select pswd from login where ID = '%s';",ac->ID);
	
	pthread_mutex_lock(&a_mutex);
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
			sprintf(query,"update status set stat = \'1\' where ID = \'%s\';",ac->ID);
			ret = 2;
		}
		else{
			mysql_free_result(res);
			ret = 1;
		}
	}
	pthread_mutex_unlock(&a_mutex);
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

void *heart_beat(void *hb){
	client_stat(&(((HB *)hb)->hb),((HB *)hb)->ID);
	close(((HB *)hb)->hb.sock);
	printf("connection endded%d\n",((HB *)hb)->hb.sock);
}
