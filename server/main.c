#include "./main.h"

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
	free(arg);
	mysql_close(conn);
	close(server_sock);
}
