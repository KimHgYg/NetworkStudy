package client_linux;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class UDP_conn {
	
	private DatagramSocket sock;
	private DatagramPacket pack;
	private InetAddress ia;
	private int port;
	private String IP;

	//sock에서 port는 내가 사용할 port포트
	//packet에서 port는 보낼 port포트 -> 서버에 보넬 port는 통신용 소켓을 만든 후 session을 만들고 그 포트를 전달.
	
	//constructor에는 서버로 한번 보내고 상대 정보 받은 후에 그 정보로  통신?
	//사용자가 움직여서 정보 바뀌었을 때는 어떻게 할지 생각
	public UDP_conn(String IP, int port) throws UnknownHostException, SocketException {
		// TODO Auto-generated constructor stub
		this.IP = IP;
		this.port = port;
		
		ia = InetAddress.getByName(this.IP);
		sock = new DatagramSocket(this.port);
		
	}

}
