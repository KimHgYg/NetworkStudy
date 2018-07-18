package client_linux;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Receive extends Thread{

	private DatagramSocket sock;
	private byte[] msg;
	
	public Receive(DatagramSocket sock) {
		// TODO Auto-generated constructor stub
		this.sock = sock;
		msg = new byte[200];
	}
	
	public void run() {
		while(true) {
			DatagramPacket pack = new DatagramPacket(msg, msg.length);
			try {
				sock.receive(pack);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
			System.out.println("»ó´ë¹æ : " + pack.getData().toString());
		}
	}

}
