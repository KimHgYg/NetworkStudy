package client_linux;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class main_thread {
	private static InetAddress name;
	private static Scanner in;
	private static Socket conn;
	private static int port;
	private static String ID;
	private static int status;
	
		public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
			in = new Scanner(System.in);
			int menu;
			get_info gi = new get_info(InetAddress.getByName(args[0]), 3000);
			while(true) {
				System.out.println("�޴��� �����ϼ���\n 1. ȸ������\n 2. �α���\n 3. �α׾ƿ�\n"
						+ "4. ���� ���� ��û");
				menu = Integer.parseInt(in.nextLine());
			
				
				//register
				if(menu == 1) {
					System.out.println("�����Ͻ� �̸� �� ID�� password�� �Է��ϼ���");
					status = gi.create_account(in.nextLine(),in.nextLine(), in.nextLine());
					if(status==1) {
						System.out.println("ID exists");
						return;
					}
					else if(status == 0) {
						System.out.println("Account created");
						return;
					}
				}
				//login
				else if(menu == 2) {
					System.out.println("ID�� password�� �Է��ϼ���");
					status = gi.login(in.nextLine(), in.nextLine());
					if ((status == 0) || (status == 1)) {
						//no account   //wrong pswd
						return;
					}
				}
				//logout
				else if(menu == 3) {
					gi.logout();
					return;
				}
				
				else if(menu == 4) {
					String IP = null;
					System.out.println("���ϴ� ID�� �Է��ϼ���\n");
					ID = in.nextLine();
					IP = gi.reqStat(ID);
				}
			}
		}
}