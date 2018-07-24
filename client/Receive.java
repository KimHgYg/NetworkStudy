package client_linux;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Receive extends Thread{

	private DatagramSocket sock;
	private DatagramPacket pack;
	private byte[] bytemsg;
	private Send send;
	private String strmsg;
	private boolean flag = false;
	private Scanner in;
	private UDP_conn udp;
	
	public Receive(Send send, DatagramSocket sock, UDP_conn udp) {
		this.sock = sock;
		bytemsg = new byte[200];
		this.send = send;
		this.in= new Scanner(System.in);
		this.udp = udp;
	}
	
	public void run() {
		int n = 1, k = 0;
		while(true) {
			bytemsg = new byte[10];
			send.check_ready();
			pack = new DatagramPacket(bytemsg, bytemsg.length);
			try {
				sock.receive(pack);
			} catch (IOException e) {
				System.out.println(n + "번 째 시도 중...");
				n++;
				send.check_ready();
				continue;
			}
			strmsg = new String(bytemsg);
			//System.out.print(strmsg);
			if(strmsg.contains("-1")) {
				while(k < 5) {
					send.ack();
					k++;
					try {
						sock.receive(pack);
						strmsg = new String(bytemsg);
						//System.out.print(strmsg);
						sleep(3000);
					} catch (IOException e) {
						System.out.println("응답을 기다리는 중 ..." + k);
						continue;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(strmsg.contains("-2")) {
						System.out.println("채팅 연결 완료");
						flag = true;
						send.set_send_on();
						k = 10;
						break;
					}
				}
				if(k == 10) {
					break;
				}
				else
					k=0;
			}
			try {
				sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		while(flag) {
			bytemsg = new byte[100];
			pack = new DatagramPacket(bytemsg, bytemsg.length);
			try {
				sock.receive(pack);
			} catch (IOException e) {
				send.interrupt();
				System.out.println("상대방이 떠났습니다");
				break;
			} 
			strmsg = new String(bytemsg);
			if(strmsg.contains("-1"))
				continue;
			else if(strmsg.contains("qpwo")) {
				System.out.println("상대방이 떠났습니다");
				send.interrupt();
				break;
			}
			System.out.println("상대방 : " + strmsg);
		}
	}
}
