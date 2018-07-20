package client_linux;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class UDP_conn {
	
	private DatagramSocket sock;
	private DatagramPacket pack;
	private InetAddress ia, pia;
	private InetAddress server_ia;
	private int server_port = 3002;
	private int target_port, target_p_port;
	private String ID;
	
	private Send send;
	private Receive rec;
	private get_info gi;
	
	private get_signal gs;

	//sock에서 port는 내가 사용할 port포트
	//packet에서 port는 보낼 port포트 -> 서버에 보넬 port는 통신용 소켓을 만든 후 session을 만들고 그 포트를 전달.
	
	//constructor에는 서버로 한번 보내고 상대 정보 받은 후에 그 정보로  통신?
	//사용자가 움직여서 정보 바뀌었을 때는 어떻게 할지 생각
	public UDP_conn(get_info gi, InetAddress ia, String ID) throws SocketException {
		sock = new DatagramSocket();
		this.server_ia = ia;
		this.gi = gi;
		this.ID = ID;
		sock.setSoTimeout(5000);
		gs = new get_signal(sock, this);
		gs.start();
	}
	
	public void UDP_ready(InetAddress ia, int target_port,InetAddress pia, int target_p_port) throws IOException, InterruptedException {
		this.target_port = target_port;
		this.target_p_port = target_p_port;
		this.pia = pia;
		this.ia = ia;
		if(ia == pia) 
			send = new Send(sock, pia, target_p_port);
		else
			send = new Send(sock, ia, target_port);
		rec = new Receive(send, sock);
		this.start();
	}
	
	public void re_set(InetAddress ia, int port) {
		this.ia = ia;
		this.target_port = port;
		send.re_set(ia, port);
	}
	
	public String get_myIP() {
		return this.sock.getLocalAddress().getHostAddress();
	}
	
	public void update_port_to_server(PrintWriter out) {
		//server udp port = 3002
		try {
			String private_info = InetAddress.getLocalHost().getHostAddress() + " " + sock.getLocalPort();
			out.print('4');
			out.flush();
			pack = new DatagramPacket(private_info.getBytes(), private_info.getBytes().length, InetAddress.getByName("52.79.185.101"), 3002);
			sock.send(pack);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start() throws InterruptedException {
		rec.start();
		send.start();
	}
	
	/*public void reConnect(InetAddress ia, int port) {
		System.out.println("연결을 재설정 합니다...");
		try {
			gi.reqStat(ID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	
	public void stop() throws InterruptedException {
		send.interrupt();
		rec.interrupt();
		send.join();
		rec.join();
	}
}