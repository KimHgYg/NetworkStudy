package client_linux;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Send extends Thread{

	private DatagramSocket sock;
	private static DatagramPacket pack;
	private Scanner in;
	private static String msg;
	private InetAddress ia;
	private int target_port;
	
	public Send(DatagramSocket sock, InetAddress ia, int target_port) {
		// TODO Auto-generated constructor stub
		this.sock = sock;
		this.ia = ia;
		this.target_port = target_port;
		in = new Scanner(System.in);
	}
	
	public void run() {
		while(true) {
			System.out.println("³ª : ");
			msg = in.nextLine();
			pack = new DatagramPacket(msg.getBytes(), msg.length(), ia, target_port);
			try {
				sock.send(pack);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		}
	}

}
