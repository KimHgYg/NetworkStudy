package gui_client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Send extends Thread{

	private DatagramSocket sock;
	private static DatagramPacket pack, p_pack;
	private Scanner in;
	private static String msg;
	private InetAddress ia;
	private int target_port;
	private boolean flag = false;
	private boolean group = false;
	private JTextArea text;
	private JTextField mytext;
	private keep_alive alive;
	private UDP_conn UDP;
	private Connection conn;
	private String ID, opp_ID, list;
	private user_list[] ul;
	
	private String user_list;
	
	private int max = 0;
	/*
	public Send(DatagramSocket sock, InetAddress ia, int target_port, JTextArea text, JTextField mytext, Connection conn, String ID) {
		this.sock = sock;
		this.conn = conn;
		this.ia = ia;
		this.target_port = target_port;
		this.text = text;
		this.mytext = mytext;
		this.ID = ID;
		in = new Scanner(System.in);
		
		mytext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				msg = mytext.getText();
				if(msg.equals(""))
					return;
				mytext.setText("");
				if(flag) {
					text.append("나 : "+msg+"\n");
					text.setCaretPosition(text.getDocument().getLength());
					Send(msg+"\n", user_list);
				}
				else {
					text.append("아직 연결되지 않았습니다..."+"\n");
					text.setCaretPosition(text.getDocument().getLength());
				}
				
			}
		});
	}*/
	public Send(DatagramSocket sock, InetAddress ia, UDP_conn udp, int target_port, JTextArea text, JTextField mytext, Connection conn, user_list[] ul) {
		this.sock = sock;
		this.conn = conn;
		this.ia = ia;
		this.target_port = target_port;
		this.text = text;
		this.mytext = mytext;
		this.ID = ID;
		in = new Scanner(System.in);
		
		mytext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				msg = mytext.getText();
				if(msg.equals(""))
					return;
				mytext.setText("");
				if(flag) {
					text.append("나 : "+msg+"\n");
					text.setCaretPosition(text.getDocument().getLength());
					send(msg+"\n");
				}
				else {
					text.append("아직 연결되지 않았습니다..."+"\n");
					text.setCaretPosition(text.getDocument().getLength());
				}
				
			}
		});
	}
	
	public void check_ready() {
		String s = "-1" + " " + ID;
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
	
	public void run() {
		while(true) {
			if(flag) {
				
			}
		}
	}
	
	public void group_flag_on() {
		group = true;
	}
	
	public String make_user_list() {
		String user_list = null;
		int i = 0;
		while(true) {
			if(ul[i].ID==null)
				break;
			user_list = user_list + "_" + ul[i++].ID;
		}
		return user_list;
	}
	
	public void insert_user_list(String ID, InetAddress pub_IP, InetAddress pri_IP, int pub_port, int pri_port) {
		ul[max].ID = ID;
		ul[max].pub_IP = pub_IP;
		ul[max].pub_port = pub_port;
		ul[max].pri_IP = pri_IP;
		ul[max].pri_port = pri_port;
		max++;
		this.user_list = this.make_user_list();
	}
	
	public void send(String msg) {
		String smsg = ID + msg;
		InetAddress tmp = null;
		int port = 0;
		int i=0;
		while(ul[i].pri_IP != null) {
			try {
				if(ul[i].pri_IP.getHostName().equals(InetAddress.getLocalHost().getHostName())) {
					tmp = ul[i].pri_IP;
					port = ul[i].pri_port;
				}
				else {
					tmp = ul[i].pub_IP;
					port = ul[i].pub_port;
				}
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pack = new DatagramPacket(smsg.getBytes(), smsg.length(), tmp, port);
			try {
				sock.send(pack);
			} catch (IOException e2) {
				
			}
			i++;
		}
		String sql = "INSERT INTO " + user_list + "(id, chat) VALUES(?,?)";
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "나");
			pstmt.setString(2, msg);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void interrupt() {
		alive.stop();
		this.stop();
		flag = false;
		System.out.println("끝!\n");
	}
}
