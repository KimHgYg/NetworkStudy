package gui_client;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import javax.swing.JTextArea;

public class Receive extends Thread{

	private DatagramSocket sock;
	private DatagramPacket pack;
	private byte[] bytemsg;
	private Send send;
	private String strmsg;
	private boolean flag = true, flag_1 = true, flag_2 = false;
	private Scanner in;
	private UDP_conn udp;
	private JTextArea text;
	private Connection conn;
	private String ID, opp_ID, user_list;
	
	private Chatting chat;
	private user_list[] ul;
	
	/*
	public Receive(Send send, DatagramSocket sock, UDP_conn udp, JTextArea text, Connection conn, String ID, Chatting chat) {
		this.sock = sock;
		this.conn = conn;
		this.chat = chat;
		this.ID = ID;
		this.opp_ID = opp_ID;
		bytemsg = new byte[200];
		this.send = send;
		this.in= new Scanner(System.in);
		this.udp = udp;
		this.text = text;
	}*/
	
	public Receive(Send send, DatagramSocket sock, UDP_conn udp, JTextArea text, Connection conn, user_list[] ul, Chatting chat) {
		this.sock = sock;
		this.conn = conn;
		this.chat = chat;
		this.ul = ul;
		bytemsg = new byte[200];
		this.send = send;
		this.in= new Scanner(System.in);
		this.udp = udp;
		this.text = text;
	}
	
	public void run() {
		int n = 1, k = 0;
		while(flag){
			while(flag_1) {
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
					opp_ID = strmsg.split(" |\n")[1];
					user_list = send.make_user_list();
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
							String sql = "CREATE TABLE IF NOT EXISTS " + user_list + " (\n"
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
							text.append("채팅 연결 완료\n");
							chat.set_chat_log(opp_ID, user_list);
							chat.setName("Chatting with " + user_list);
							flag_2 = true;
							flag_1 = false;
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
			while(flag_2) {
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
				strmsg = (new String(bytemsg));
				strmsg = strmsg.substring(0, strmsg.length());
				String msg = strmsg.split("\n")[0];
				if(strmsg.contains("-1") || strmsg.contains("-2"))
					continue;
				opp_ID = msg.split(" ", 2)[0];
				msg = msg.split(" ", 2)[1];
				text.append(opp_ID + " : " + msg + "\n");
				text.setCaretPosition(text.getDocument().getLength());
				String sql = "INSERT INTO " + user_list + "(id, chat) VALUES(?,?)";
				PreparedStatement pstmt;
				try {
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, opp_ID);
					pstmt.setString(2, msg+"\n");
					pstmt.executeUpdate();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void interrupt() {
		flag = false;
		flag_2 = false;
		send.interrupt();
	}
}
