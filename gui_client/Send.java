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

import javax.swing.JButton;
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
	
	private String name;
	
	private JTextArea text;
	private JTextField mytext;
	private JButton add_but;
	
	private keep_alive alive;
	private UDP_conn UDP;
	private get_info gi;
	private Connection conn;
	private String myID, opp_ID, list;
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
	public Send(get_info gi, DatagramSocket sock, InetAddress ia, UDP_conn udp, int target_port, Chatting chat, Connection conn, user_list[] ul, String myID) {
		this.sock = sock;
		this.conn = conn;
		this.ia = ia;
		this.target_port = target_port;
		this.text = chat.get_textArea();
		this.mytext = chat.get_mytextArea();
		this.add_but = chat.get_invite_button();
		this.myID = myID;
		this.ul = ul;
		this.gi = gi;
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
		
		add_but.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				name = JOptionPane.showInputDialog(chat, "초대할 사용자의 ID를 입력해주세요",null);
				if(group == false) {
					group = true;
				}
				else {
					udp.sig_On();
					gi.reqStat(name, "true");
				}
			}
		});
	}
	
	public void check_ready() {
		String s = "-1" + " " + myID;
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
		alive = new keep_alive(sock, ia, target_port, this);
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
			if(ul[i]==null)
				break;
			user_list = user_list + "_" + ul[i++].ID;
		}
		return user_list;
	}
	
	public void insert_user_list(String ID, InetAddress pub_IP, InetAddress pri_IP, int pub_port, int pri_port) {
		ul[max] = new user_list(ID, pub_IP, pri_IP, pub_port, pri_port);
		/*ul[max].ID = new String(ID);
		ul[max].pub_IP = pub_IP;
		ul[max].pub_port = pub_port;
		ul[max].pri_IP = pri_IP;
		ul[max].pri_port = pri_port;*/
		max++;
		this.user_list = this.make_user_list();
	}
	
	public void send(String msg) {
		String smsg = myID + " "+ msg;
		InetAddress tmp = null;
		int port = 0;
		int i=0;
		while(ul[i] != null) {
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
		if(!msg.equals("-1")) {
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
	}
	
	public void interrupt() {
		alive.stop();
		this.stop();
		flag = false;
		System.out.println("끝!\n");
	}
}
