package gui_client;

import java.beans.Statement;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JFrame;

public class UDP_conn extends JFrame{
	
	private DatagramSocket sock;	
	private DatagramPacket pack;
	private InetAddress ia, pia;
	private InetAddress server_ia;
	private int server_port = 3002;
	private int target_port, target_p_port;
	private String ID;
	private boolean avail = true;
	private Connection conn;
	public int index;
	
	private boolean group = false;
	
	private String public_IP;

	private int max_user = 10;
	private user_list[] ul;

	private Send send;
	private Receive rec;
	private get_info gi;
	
	private get_signal gs;

	//sock에서 port는 내가 사용할 port포트
	//packet에서 port는 보낼 port포트 -> 서버에 보넬 port는 통신용 소켓을 만든 후 session을 만들고 그 포트를 전달.
	
	//constructor에는 서버로 한번 보내고 상대 정보 받은 후에 그 정보로  통신?
	//사용자가 움직여서 정보 바뀌었을 때는 어떻게 할지 생각
	public UDP_conn(get_info gi, InetAddress ia, String ID, int index, Connection conn) throws SocketException {
		sock = new DatagramSocket();
		this.ul = new user_list[max_user];
		this.server_ia = ia;
		this.gi = gi;
		this.ID = ID;
		this.index = index;
		this.conn = conn;
		sock.setSoTimeout(5000);
		gs = new get_signal(sock, this);
		gs.start();
	}
	
	public void UDP_ready(InetAddress ia, int target_port,InetAddress pia, int target_p_port, String opp_ID, String group_flag) throws IOException, InterruptedException {
		System.out.println("I'm "+ID);
		
		Chatting chat = new Chatting(this, conn);
		this.target_port = target_port;
		this.target_p_port = target_p_port;
		this.pia = pia;
		this.ia = ia;
		if(avail == true) {
			if(pia.getHostName().equals(InetAddress.getLocalHost().getHostName())) 
				send = new Send(gi, sock, pia, this, target_p_port, chat, conn, ul, ID);
			else
				send = new Send(gi, sock, ia, this, target_port, chat, conn, ul, ID);
			rec = new Receive(send, sock, this, chat.get_textArea(), conn, ul, chat);
			send.insert_user_list(opp_ID, ia, pia, target_port, target_p_port);
			this.start();
			this.Wait();
		}
		else {
			send.insert_user_list(opp_ID, ia, pia, target_port, target_p_port);
			this.sig_Off();
			group = true;
		}
		//chat.dispose();
	}
	//채팅 중 IP등 바뀔 때
	/*public void re_set(InetAddress ia, int port) {
		this.ia = ia;
		this.target_port = port;
		send.re_set(ia, port);
	}*/
	
	/*public void reConnect(InetAddress ia, int port) {
	System.out.println("연결을 재설정 합니다...");
	try {
		gi.reqStat(ID);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}*/
	public void set_my_Public_IP(String IP) {
		this.public_IP = IP;
	}
	
	public String get_my_Public_IP() {
		return this.public_IP;
	}
	
	public String get_myIP() {
		return this.sock.getLocalAddress().getHostAddress();
	}
	
	public String get_myID() {
		return this.ID;
	}
	
	public void update_port_to_server(PrintWriter out) {
		//server udp port = 3002
		if(this.avail) {
			try {
				String private_info = InetAddress.getLocalHost().getHostAddress() + " " + sock.getLocalPort() + " " + index;
				out.print('4');
				out.flush();
				pack = new DatagramPacket(private_info.getBytes(), private_info.getBytes().length, InetAddress.getByName("52.79.185.101"), 3002); //서버 public IP주소
				sock.send(pack);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void start() throws InterruptedException {
		rec.start();
		send.start();
	}

	
	
	public void Wait() {
		try {
			send.join();
			rec.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("stoped\n");
		this.Avail();
	}
	
	public void stop(){
		if(rec.isAlive())
			rec.interrupt();
	}
	
	public void invite(String ID, user_list ul) {
		byte[] buf = new byte[100];
		DatagramPacket p = new DatagramPacket(buf, buf.length);
		gi.reqStat(ID, "true");
		try {
			sock.receive(p);
		} catch (IOException e) {
			System.out.println("유저 정보를 가져오지 못했습니다.");
		}	
	}
	public boolean get_avail() {
		return this.avail;
	}
	
	public void not_Avail() {
		this.avail = false;
		gs.Off();
	}
	public void Avail() {
		this.avail = true;
		gs.On();
	}
	
	public void sig_On() {
		gs.On();
	}
	
	public void sig_Off() {
		gs.Off();
	}
}