package gui_client;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import javax.swing.JTextArea;

public class Receive extends Thread{

	private DatagramSocket sock;
	private DatagramPacket pack;
	private byte[] bytemsg;
	private Send send;
	private String strmsg;
	private boolean flag = false;
	private Scanner in;
	private UDP_conn udp;
	private JTextArea text;
	
	public Receive(Send send, DatagramSocket sock, UDP_conn udp, JTextArea text) {
		this.sock = sock;
		bytemsg = new byte[200];
		this.send = send;
		this.in= new Scanner(System.in);
		this.udp = udp;
		this.text = text;
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
				text.append(n + "번 째 시도중...\n");
				text.setCaretPosition(text.getDocument().getLength());
				n++;
				send.check_ready();
				continue;
			}
			strmsg = new String(bytemsg);
			if(strmsg.contains("-1")) {
				while(k < 5) {
					send.ack();
					k++;
					try {
						sock.receive(pack);
						strmsg = new String(bytemsg);
						sleep(3000);
					} catch (IOException e) {
						System.out.println("응답을 기다리는 중 ..." + k);
						text.append("응답을 기다리는 중 ..." + k + "\n");
						text.setCaretPosition(text.getDocument().getLength());
						continue;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(strmsg.contains("-2")) {
						text.append("채팅 연결 완료\n");
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
				text.append("상대방이 떠났습니다");
				text.setCaretPosition(text.getDocument().getLength());
				this.interrupt();
				break;
			}
			strmsg = new String(bytemsg);
			if(strmsg.contains("-1"))
				continue;
			text.append("상대방 : " + strmsg + "\n");
			text.setCaretPosition(text.getDocument().getLength());
		}
	}
	
	public void interrupt() {
		flag = false;
		send.interrupt();
	}
}
