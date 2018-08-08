package gui_client;

import java.awt.Container;
import java.awt.DisplayMode;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class Login_page extends JFrame{

	private get_info gi;
	
	public Login_page() {
		super("KKaKKao TTalk");
				
		try {
			gi = new get_info(InetAddress.getByName("52.79.185.101"), 3000, "52.79.185.101");
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		setBounds(100,100,500,200);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new FlowLayout());
		JPanel pane = new JPanel();

		//�α��� ��ư
		JButton Login_button = new JButton("�α���");
		Login_button.setMnemonic('L');
		
		//ID �Է�
		JTextField text_ID = new JTextField(10);
		JLabel label_ID = new JLabel("ID : ");
		
		//Password �Է�
		JPasswordField text_PW = new JPasswordField(10);
		JLabel label_PW = new JLabel("Password : ");
		
		pane.add(label_ID);
		pane.add(text_ID);
		pane.add(label_PW);
		pane.add(text_PW);
		pane.add(Login_button);
		
		contentPane.add(pane);
		
		//�α��� �� ȭ��
		JPanel menu_pane =  new JPanel();
		
		JButton logout_button = new JButton("Logout");
		JButton chat_button = new JButton("Start Chatting");
		
		menu_pane.add(logout_button);
		menu_pane.add(chat_button);
		
		//start chatting ���� �� ȭ��
		JPanel chat_pane =  new JPanel();
		chat_pane.setLayout(new FlowLayout());
		JLabel chat_label = new JLabel("���ϴ� ����� ID�� �Է��� �ּ��� : ");
		JTextField req_ID_text = new JTextField(10);
		JButton connect_button = new JButton("�� ��");
		
		chat_pane.add(chat_label);
		chat_pane.add(req_ID_text);
		chat_pane.add(connect_button);
		
		
		//�α���
		Login_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String ID = text_ID.getText();
				String pswd = text_PW.getText();
				int stat = 0;
				try {
					stat = gi.login(ID, pswd);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				if (stat == 0) {
					JOptionPane.showMessageDialog(null, "No Account");
				}
				else if(stat == 1) {
					JOptionPane.showMessageDialog(null, "Wrong Password");
				}
				else if(stat == 2){
					contentPane.removeAll();
					contentPane.add(menu_pane);
					revalidate();
					repaint();
				}
			}
		});
		
		//�α׾ƿ�
		logout_button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				gi.logout();
				contentPane.removeAll();
				contentPane.add(pane);
				revalidate();
				repaint();
			}
		});
		
		//ä��
		chat_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//ä�� ��ư Ŭ�� �� ȭ��
				contentPane.removeAll();
				contentPane.add(chat_pane);
				revalidate();
				repaint();
				
			}
		});
		
		//���� �� �޴���
		connect_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String reqID = req_ID_text.getText();
				if(reqID.equals("")) {
					return;
				}
				gi.reqStat(reqID);
				contentPane.removeAll();
				contentPane.add(menu_pane);
				revalidate();
				repaint();
			}
		});
		
		setVisible(true);
	}
}