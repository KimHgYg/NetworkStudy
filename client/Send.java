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
		pack = new DatagramPacket("-1".getBytes(), "1".getBytes().length, ia, target_port);
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
		pack = new DatagramPacket("-2".getBytes(), "-2".getBytes().length, ia, target_port);
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
		}
	}

}
