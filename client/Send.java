package client_linux;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Send extends Thread{

	private DatagramSocket sock;
	private static DatagramPacket pack, p_pack;
	private Scanner in;
	private static String msg;
	private InetAddress ia;
	private int target_port;
	private boolean flag = false;
	private keep_alive alive;
	private UDP_conn UDP;
	
	public Send(DatagramSocket sock, InetAddress ia, int target_port) {
		this.sock = sock;
		this.ia = ia;
		this.target_port = target_port;
		in = new Scanner(System.in);
	}
	
	public void check_ready() {
		String s = "-1";
		pack = new DatagramPacket(s.getBytes(), s.getBytes().length, ia, target_port);
		try {
			sock.send(pack);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void re_set(InetAddress ia, int port) {
		this.ia = ia;
		this.target_port = port;
	}
	
	public void ack() {
		String s = "-2";
		pack = new DatagramPacket(s.getBytes(), s.getBytes().length, ia, target_port);
		try {
			sock.send(pack);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void set_send_on() {
		this.flag = true;
		alive = new keep_alive(sock, ia, target_port);
		alive.start();
	}
	
	public void logout() {
		String s = "qpwo";
		pack = new DatagramPacket(s.getBytes(), s.getBytes().length, ia, target_port);
		try {
			sock.send(pack);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {
		while(true) {
			if(flag) {
				System.out.println("³ª : ");
				msg = in.nextLine();
				pack = new DatagramPacket(msg.getBytes(), msg.length(), ia, target_port);
				try {
					sock.send(pack);
				} catch (IOException e) {
					break;
				}
			}
			try {
				sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				break;
			}
		}
	}
}
