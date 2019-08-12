package client_linux;

import java.net.InetAddress;
import java.util.Scanner;


public class main_thread {
	private static Scanner in;
	private static String ID;
	private static int status;
	
	private static UDP_conn[] udp;
	
		public static void main(String[] args) throws Exception{
			in = new Scanner(System.in);
			int menu;
			get_info gi = new get_info(InetAddress.getByName(args[0]), 3000, args[0]);
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
					udp = gi.login(in.nextLine(), in.nextLine());
					if(udp == null) {
						return;
					}
				}
				//logout
				else if(menu == 3) {
					gi.logout();
					return;
				}
				
				else if(menu == 4) {
					System.out.println("���ϴ� ID�� �Է��ϼ���\n");
					ID = in.nextLine();
					gi.reqStat(ID);
				}
			}
		}
}