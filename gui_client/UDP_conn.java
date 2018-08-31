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
	
	private Send send;
	private Receive rec;
	private get_info gi;
	
	private get_signal gs;

	//sock���� port�� ���� ����� port��Ʈ
	//packet���� port�� ���� port��Ʈ -> ������ ���� port�� ��ſ� ������ ���� �� session�� ����� �� ��Ʈ�� ����.
	
	//constructor���� ������ �ѹ� ������ ��� ���� ���� �Ŀ� �� ������  ���?
	//����ڰ� �������� ���� �ٲ���� ���� ��� ���� ����
	public UDP_conn(get_info gi, InetAddress ia, String ID, int index, Connection conn) throws SocketException {
		sock = new DatagramSocket();
		this.server_ia = ia;
		this.gi = gi;
		this.ID = ID;
		this.index = index;
		this.conn = conn;
		sock.setSoTimeout(5000);
		gs = new get_signal(sock, this);
		gs.start();
	}
	
	public void UDP_ready(InetAddress ia, int target_port,InetAddress pia, int target_p_port, String opp_ID) throws IOException, InterruptedException {
		System.out.println("I'm "+ID);
		String sql = "CREATE TABLE IF NOT EXISTS " + opp_ID + " (\n"
				+ " id text NOT NULL,\n"
				+ " chat text NOT NULL\n"
				+ ");";
		try {
			java.sql.Statement stmt = conn.createStatement();
			stmt.execute(sql);
			System.out.println("table created\n");
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("table create failed\n");
		}
		Chatting chat = new Chatting(this, opp_ID, conn);
		this.target_port = target_port;
		this.target_p_port = target_p_port;
		this.pia = pia;
		this.ia = ia;
		
		if(pia.getHostName().equals(InetAddress.getLocalHost().getHostName())) 
			send = new Send(sock, pia, target_p_port, chat.get_textArea(), chat.get_mytextArea(), conn, opp_ID);
		else
			send = new Send(sock, ia, target_port, chat.get_textArea(), chat.get_mytextArea(), conn, opp_ID);
		rec = new Receive(send, sock, this, chat.get_textArea(), conn, opp_ID);
		this.start();
		this.Wait();
		//chat.dispose();
	}
	//ä�� �� IP�� �ٲ� ��
	/*public void re_set(InetAddress ia, int port) {
		this.ia = ia;
		this.target_port = port;
		send.re_set(ia, port);
	}*/
	
	/*public void reConnect(InetAddress ia, int port) {
	System.out.println("������ �缳�� �մϴ�...");
	try {
		gi.reqStat(ID);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}*/
	
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
				pack = new DatagramPacket(private_info.getBytes(), private_info.getBytes().length, InetAddress.getByName("52.79.185.101"), 3002); //���� public IP�ּ�
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
	
	public void invite(String ID) {
		byte[] buf = new byte[100];
		DatagramPacket p = new DatagramPacket(buf, buf.length);
		gi.reqStat(ID);
		try {
			sock.receive(p);
		} catch (IOException e) {
			System.out.println("���� ������ �������� ���߽��ϴ�.");
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
}