package client_linux;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class keep_alive extends Thread{
	
	private DatagramSocket sock;
	private DatagramPacket p,r;
	private InetAddress ia;
	private int port, n = 0;
	private String s;
	
	public keep_alive(DatagramSocket sock, InetAddress ia, int port) {
		this.sock = sock;
		this.ia = ia;
		this.port = port;
	}
	
	public void set_info(InetAddress ia, int port) {
		this.ia = ia;
		this.port= port;
	}

	public void run() {
		s = "-1";
		byte[] buf = s.getBytes();
		byte[] buf2 = new byte[100];
		p = new DatagramPacket(buf, buf.length,ia,port);
		r = new DatagramPacket(buf2, buf2.length);
		while(true) {
			try {
				sock.send(p);
				sleep(3000);
			} catch (IOException e) {
				e.printStackTrace();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
