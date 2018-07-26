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
void heart_beat(Arg *, char *);
void req(Arg *, char *);
void port_update(char *, char *);

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
pthread_mutex_t udp_mutex = PTHREAD_MUTEX_INITIALIZER;

pthread_cond_t conA = PTHREAD_COND_INITIALIZER;

pthread_t conn_thread[CONN_MAX];
pthread_t hb_thread[CONN_MAX];

int con_index;
int hb_index;

int server_sock;
int udp_sock;

struct sockaddr_in sock_udp;
struct sockaddr_in client_udp;

int main(int argc, char *argv[]){
	int client_sock;

	struct sockaddr_in sock_addr, client_addr;


	int size = sizeof(struct sockaddr_in);

	memset(&client_addr, 0x00, size);
	memset(&sock_addr,0x00,size);

	memset(&sock_udp, 0x00, size);

	sock_addr.sin_family = AF_INET;
	sock_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	sock_addr.sin_port = htons(atoi(argv[1]));

	sock_udp.sin_family = AF_INET;
	sock_udp.sin_addr.s_addr = htonl(INADDR_ANY);
	sock_udp.sin_port = htons(3002);

	arg = malloc(sizeof(Arg *)*CONN_MAX);
	hb = malloc(sizeof(HB *)*CONN_MAX);

	char *msg;

	if(argc != 2){
		fprintf(stderr,"<usage> : server_java.out \"port number\"\n");
		exit(0);
	}

	init_mysql();

	//initiate TCP connection
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

	//initiate UDP connection
	if((udp_sock = socket(PF_INET,SOCK_DGRAM,0)) == -1){
		perror("udp socket failed ");
		exit(0);
	}
	if(bind(udp_sock, (struct sockaddr *)&sock_udp,size) == -1){
		perror("bind udp socket error ");
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

		con_index = client_sock;

		arg[con_index] = malloc(sizeof(Arg));
		arg[con_index]->sock = client_sock;
		arg[con_index]->client_sock = client_addr;

		pthread_create(&conn_thread[con_index],NULL,get_connection,(void *)arg[con_index]);

		//pthread_cond_wait(&conA, &server_sock_mutex);
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
		printf("Conncetion Canceled\n");
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
		//pthread_cond_signal(&conA);
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
			heart_beat((Arg *)arg,ac->ID);
		}
		//pthread_cond_signal(&conA);
	}
	free(ac);
	close(((Arg *)arg)->sock);
	free((Arg *)arg);
}

void req(Arg *arg, char *ID){
	char query[150] = "";
	char reqID[30] = "";
	char option = '0';
	char text[100] = "";
	char IP[30] = "";
	char tmp[40] = "";
	char index[5] = "";
	char oindex[5] = "";

	char oIP[30]="",oport[5]="",opIP[30]="",opport[5]="";
	int ret;

	if(read(((Arg *)arg)->sock, &option, 1) == 0){
		close(((Arg *)arg)->sock);
		printf("server connection closed %d\n",((Arg *)arg)->sock);
		return (void)0;
	}

	if(option == '0'){
		memset(query,0x00,150);
		memset(text ,0x00,100);
		if(read(((Arg *)arg)->sock,tmp,40) == 0){
			printf("server connection closed %d\n",((Arg *)arg)->sock);
			close(((Arg *)arg)->sock);
			return (void)0;
		}
		strcpy(reqID,strtok(tmp," "));
		strcpy(index,strtok(NULL, " "));
		sprintf(query,"select stat from status where ID = \'%s\';",reqID);
		pthread_mutex_lock(&a_mutex);
		if((ret = mysql_query(conn,query)) != 0){
			printf("Cannot select stat\n");
			return (void)-1;
		}
		res = mysql_store_result(conn);
		if(res->row_count == 1){
			row = mysql_fetch_row(res);
			strcpy(text,row[0]);
			strcat(text,"\n");
			write(((Arg *)arg)->sock,text,strlen(text));
			if(!strcmp(row[0],"1")){
				memset(text,0x00,100);
				memset(query,0x00,150);
				sprintf(query,"select IP, port, private_IP, private_port, IND from client where ID = \'%s\' and available = \'1\';",reqID);
				if(ret = mysql_query(conn,query)){
					printf("Cannot select IP\n");
					return (void)-1;
				}
				res = mysql_store_result(conn);
				if(res->row_count > 0){
					row = mysql_fetch_row(res);
					//oppnent info
					strcpy(oIP,row[0]);strcpy(oport,row[1]);strcpy(opIP,row[2]);strcpy(opport,row[3]);strcpy(oindex,row[4]);
					sprintf(text,"%s %s %s %s\n",oIP,oport,opIP,opport);
					memset(query,0x00,150);
					sprintf(query,"update client set available = \'0\' where ID = \'%s\' and IND = %s;",reqID,oindex);
					if((ret = mysql_query(conn,query)) != 0){
						printf("cannot disable port\n");
						return (void ) -1;
					}
					memset(query,0x00,150);
					sprintf(query,"select IP, port, private_IP, private_port, IND from client where ID = \'%s\' and available = \'1\';",ID);
					if((ret = mysql_query(conn,query)) != 0){
						printf("Cannot select socket_number\n");
						return (void)-1;
					}
					res = mysql_store_result(conn);
					//my info
					if(res->row_count > 0){
						row=mysql_fetch_row(res);
						//my sock info
						client_udp.sin_family = AF_INET;
						client_udp.sin_addr.s_addr = inet_addr(row[0]);
						client_udp.sin_port = htons(atoi(row[1]));
						sendto(udp_sock,text,strlen(text),0,(struct sockaddr *)&client_udp,sizeof(struct sockaddr_in));
						memset(text,0x00,100);
						sprintf(text,"%s %s %s %s\n",row[0], row[1],row[2],row[3]);//IP port
					}
				}
			}
			//oppnent sock info
			memset(&client_udp,0x00,sizeof(struct sockaddr_in));
			client_udp.sin_family = AF_INET;
			client_udp.sin_addr.s_addr = inet_addr(oIP);
			client_udp.sin_port = htons(atoi(oport));

			printf("export user info %s, IP %s port %s to %s\n",reqID,oIP, oport, ID);
			printf("signal %s sent to %s %s\n",text, oIP,reqID);
			sendto(udp_sock,text,strlen(text),0,(struct sockaddr *)&client_udp,sizeof(struct sockaddr_in));
			memset(query,0x00,150);
			sprintf(query,"update client set available = \'0\' where ID = \'%s\' and IND = %s;",ID, index);
			if(mysql_query(conn,query) != 0){
				printf("cannot disable port2\n");
				return (void )-1;
			}
		}
		else{
			write(((Arg *)arg)->sock,"2\n",2);
			printf("export FAILED user info %s to %d\n",reqID,((Arg *)arg)->sock);
		}
		pthread_mutex_unlock(&a_mutex);
	}
}
//check heartbit
int client_stat(Arg *hb, char *ID){
	char stat = -1;
	char query[150] = "";
	char text[30] = "";
	char port[7] = "";
	int ret;
	int tmp;
	char len;
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
			//read(((Arg *)hb)->sock, &len, 1);
			//read(((Arg *)hb)->sock, port, (int)(len-'0'));
			sprintf(query,"update status set stat = \'%c\' where ID = \'%s\';",stat,ID);
			pthread_mutex_lock(&a_mutex);
			ret = mysql_query(conn,query);
			pthread_mutex_unlock(&a_mutex);
			memset(query,0x00,150);
			if(ret != 0){
				printf("Cannot update stat=0\n");
				return -1;
			}
			memset(query,0x00,150);
			memset(text,0x00,30);
			sprintf(text,"%s\n",inet_ntoa(hb->client_sock.sin_addr));
			write(((Arg *)hb)->sock, text, strlen(text));
			printf("HB %d port %d\n",((Arg *)hb)->sock,ntohs(((Arg *)hb)->client_sock.sin_port));
		}
		//error
		else if(stat == '0'){
			memset(query,0x00,150);
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
		else if(stat == '3'){
			req(hb, ID);
		}
		else if(stat == '4'){
			port_update(inet_ntoa(hb->client_sock.sin_addr),ID);
		}
		stat = 0;
		tmp = 0;
	}
	return 0;
}

void port_update(char *IP, char *ID){
	char buf[25] = "";
	char pri_IP[15] = "";
	char pri_port[5] = "";
	char query[150] = "";
	char index[3];
	int port,ret;
	int size = sizeof(struct sockaddr_in);
	struct sockaddr_in client;

	memset(&client,0x00,size);

	pthread_mutex_lock(&udp_mutex);
	recvfrom(udp_sock,buf,sizeof(buf),0,(struct sockaddr *)&client,&size);

	pthread_mutex_unlock(&udp_mutex);
	strcpy(pri_IP, strtok(buf," "));
	strcpy(pri_port , strtok(NULL, " "));
	strcpy(index, strtok(NULL, " "));
	port = ntohs(client.sin_port);
	pthread_mutex_lock(&a_mutex);
	memset(query,0x00,150);
	sprintf(query,"select * from client where ID = \'%s\' and IND = %s;",ID, index);
	if((ret = mysql_query(conn,query)) == 0){
		res = mysql_store_result(conn);
		if(res->row_count != 0){
			memset(query,0x00,150);
			sprintf(query,"update client set IP = \'%s\', port = \'%d\', private_IP = \'%s\', private_port = \'%s\' where ID = \'%s\' and IND = %s;",IP,port,pri_IP,pri_port,ID,index);
		}
		else{
			memset(query,0x00,150);
			sprintf(query,"insert into client values(\'%s\', \'%s\', \'%d\', \'%s\', \'%s\', %s, \'1\');",ID, IP, port, pri_IP, pri_port, index);
			if(mysql_query(conn,query) != 0){
				printf("port update failed\n");
				return ;
			}
		}
		printf("port updated %s\n",ID);
	}
	else{
		printf("port update failed 2\n");
	}
	pthread_mutex_unlock(&a_mutex);
}

int logout(char *ID){
	pthread_mutex_lock(&a_mutex);
	char query[150] = "";
	int ret;
	sprintf(query,"delete from client where ID = \'%s\';",ID);
	ret = mysql_query(conn,query);
	if(ret != 0)
		printf("user info delete failed %s\n",ID);

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
	char query[150]="";

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
	char query[150]="";
	int ret;
	memset(query,0x00,150);
	sprintf(query,"select ID from login where ID = '%s';",ac->ID);
	pthread_mutex_lock(&a_mutex);
	if(mysql_query(conn,query)==0){
		res = mysql_store_result(conn);
		if(res->row_count == 0){
			memset(query,0x00,150);
			sprintf(query,"insert into login values(\'%s\', \'%s\', \'%s\');",ac->Name, ac->ID,ac->pswd);
			mysql_query(conn,query);
			memset(query,0x00,150);
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
	char query[150]="";
	int ret;
	memset(query,0x00,150);
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
			memset(query,0x00,150);
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
	memset(query,0x00,150);
	sprintf(query,"select ID, IP, port from client where ID = \'%s\';",ID);
}

void complete_query_insert(char *query, char *ID, char *IP, char *port){
	memset(query,0x00,150);
	sprintf(query,"insert into client(ID, IP, port) values(\'%s\',\'%s\',\'%s\');",ID,IP,port);
}

void complete_query_update(char *query, char *ID, char *IP, char *port){
	memset(query,0x00,150);
	sprintf(query,"update client set IP = \'%s\',port = \'%s\' where ID = \'%s\';",IP,port,ID);
}

void heart_beat(Arg *arg, char *ID){
	client_stat(arg,ID);
	close(arg->sock);
	printf("connection endded%d\n",arg->sock);
}
