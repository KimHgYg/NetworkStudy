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
				System.out.println("메뉴를 선택하세요\n 1. 회원가입\n 2. 로그인\n 3. 로그아웃\n"
						+ "4. 유저 상태 요청");
				menu = Integer.parseInt(in.nextLine());
				//register
				if(menu == 1) {
					System.out.println("생성하실 이름 및 ID와 password를 입력하세요");
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
					System.out.println("ID와 password를 입력하세요");
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
					System.out.println("원하는 ID를 입력하세요\n");
					ID = in.nextLine();
					gi.reqStat(ID);
				}
			}
		}
}