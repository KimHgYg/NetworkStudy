package client_linux;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class UDP_conn {
	
	private DatagramSocket sock;
	private DatagramPacket pack;
	private InetAddress ia;
	private InetAddress server_ia;
	private int server_port = 3002;
	private int my_port;
	private int target_port;
	
	private Send send;
	private Receive rec;

	//sock에서 port는 내가 사용할 port포트
	//packet에서 port는 보낼 port포트 -> 서버에 보넬 port는 통신용 소켓을 만든 후 session을 만들고 그 포트를 전달.
	
	//constructor에는 서버로 한번 보내고 상대 정보 받은 후에 그 정보로  통신?
	//사용자가 움직여서 정보 바뀌었을 때는 어떻게 할지 생각
	public UDP_conn(InetAddress ia) throws SocketException {
		// TODO Auto-generated constructor stub
		my_port = 30000;
		sock = new DatagramSocket();
		//sock = new DatagramSocket(my_port);
		this.server_ia = ia;
	}
	
	public void UDP_ready(InetAddress ia, int target_port) throws SocketException {
		this.target_port = target_port;
		this.ia = ia;
		send = new Send(sock, ia, target_port);
		rec = new Receive(sock);
	}
	
	public void update_port_to_server() throws IOException {
		//server udp port = 3002
		pack = new DatagramPacket("1".getBytes(), 1, InetAddress.getByName("52.79.185.101"), 3002);
		sock.send(pack);
	}
	
	public void start() throws InterruptedException {
		send.start();
		rec.start();
	}
	
	public void stop() throws InterruptedException {
		send.interrupt();
		rec.interrupt();
		send.join();
		rec.join();
	}
}