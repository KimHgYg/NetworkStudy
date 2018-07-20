package client_linux;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Receive extends Thread{

	private DatagramSocket sock;
	private DatagramPacket pack;
	private byte[] bytemsg;
	private Send send;
	private String strmsg;
	private boolean flag = false;
	
	public Receive(Send send, DatagramSocket sock) {
		this.sock = sock;
		bytemsg = new byte[200];
		this.send = send;
	}
	
	public void run() {
		int n = 1;
		send.check_ready();
		while(true) {
			pack = new DatagramPacket(bytemsg, bytemsg.length);
			try {
				sock.receive(pack);
			} catch (IOException e) {
				//e.printStackTrace();
				System.out.println(n + "번 째 시도 중...");
				n++;
				send.check_ready();
				continue;
			}
			strmsg = new String(bytemsg);
			System.out.println(strmsg);
			if(strmsg.equals("-1")) {
				send.ack();
				continue;
			}
			else if(strmsg.equals("-2")) {
				System.out.println("채팅 연결 완료");
				flag = true;
				send.set_send_on();
				break;
			}
		}
		while(flag) {
			pack = new DatagramPacket(bytemsg, bytemsg.length);
			try {
				sock.receive(pack);
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			strmsg = new String(bytemsg);
			System.out.println("상대방 : " + strmsg);
		}
	}
}
