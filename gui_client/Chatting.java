package gui_client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Chatting extends JFrame{

	private JTextArea text;
	private JTextField my_text;
	private JButton add;
	private Connection conn;
	
	public Chatting(UDP_conn udp, Connection conn){
		super("chat preparing");
		
		Container chat = this.getContentPane();
		this.conn = conn;
		setBounds(100,100,250,600);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				String obj[] = {"Yes","No"};
				int prompt = JOptionPane.showOptionDialog(null, "채팅을 끝내시겠습니까?", null, 
						JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, obj, obj[1]);
				if(prompt == 0) {
					udp.stop();
					dispose();
				}
			}
		});
		
		chat.setLayout(new BorderLayout());
		
		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		text = new JTextArea("채팅 시작!\n");
		text.setSize(200, 300);
		my_text = new JTextField();
		my_text.setEditable(true);
		
		add = new JButton("Invite");
		
		text.setEditable(false);
		JScrollPane bar = new JScrollPane(text);
		bar.setPreferredSize(new Dimension(200, 300));
		text.setLineWrap(true);
		
		pane.add(bar,"North");
		pane.add(my_text, "South");
		pane.add(add);
		
		chat.add(pane,"Center");
		
		
		setVisible(true);
	}
	
	public void set_chat_log(String ID, String user_list) {
		String sql = "SELECT id, chat FROM " + user_list;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()){
				text.append(rs.getString("id") + " : " + rs.getString("chat"));
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		text.setCaretPosition(text.getDocument().getLength());
	}
	
	public JTextArea get_textArea() {
		return text;
	}
	
	public JTextField get_mytextArea() {
		return my_text;
	}
	
	public JButton get_invite_button() {
		return add;
	}
}
