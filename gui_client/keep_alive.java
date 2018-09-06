package gui_client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class keep_alive extends Thread{
	
	private DatagramSocket sock;
	private DatagramPacket p;
	private InetAddress ia;
	private int port, n = 0;
	private String s;
	private Send send;
	
	public keep_alive(DatagramSocket sock, InetAddress ia, int port, Send send) {
		this.sock = sock;
		this.ia = ia;
		this.port = port;
		this.send = send;
	}
	
	public void set_info(InetAddress ia, int port) {
		this.ia = ia;
		this.port= port;
	}

	public void run() {
		
		while(true) {
			try {
				send.send("-1");
				sleep(3000);
			} catch(InterruptedException e) {
				System.out.println("alive interrupted");
			}
		}
	}
}
