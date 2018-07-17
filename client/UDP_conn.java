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

	//sock���� port�� ���� ����� port��Ʈ
	//packet���� port�� ���� port��Ʈ -> ������ ���� port�� ��ſ� ������ ���� �� session�� ����� �� ��Ʈ�� ����.
	
	//constructor���� ������ �ѹ� ������ ��� ���� ���� �Ŀ� �� ������  ���?
	//����ڰ� �������� ���� �ٲ���� ���� ��� ���� ����
	public UDP_conn(String IP, int port) throws UnknownHostException, SocketException {
		// TODO Auto-generated constructor stub
		this.IP = IP;
		this.port = port;
		
		ia = InetAddress.getByName(this.IP);
		sock = new DatagramSocket(this.port);
		
	}

}
