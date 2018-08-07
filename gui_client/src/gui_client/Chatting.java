package gui_client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Chatting extends JFrame{

	private JTextArea text;
	private JTextField my_text;
	
	public Chatting(UDP_conn udp, String ID){
		super("Chat with " + ID);
		
		Container chat = this.getContentPane();
		
		setBounds(100,100,1000,1000);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				udp.stop();
			}
		});
		
		chat.setLayout(new BorderLayout());
		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		text = new JTextArea("채팅 시작!\n");
		text.setSize(450, 600);
		my_text = new JTextField();
		my_text.setEditable(true);
		JScrollPane bar = new JScrollPane(text);
		bar.setPreferredSize(new Dimension(600, 450));
		text.setLineWrap(true);
		
		pane.add(bar,"North");
		pane.add(my_text, "South");
		
		chat.add(pane,"Center");
		setVisible(true);
	}
	
	public JTextArea get_textArea() {
		return text;
	}
	
	public JTextField get_mytextArea() {
		return my_text;
	}
}
