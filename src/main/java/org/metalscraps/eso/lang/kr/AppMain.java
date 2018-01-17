package org.metalscraps.eso.lang.kr;

import java.util.Scanner;

/**
 * Created by 안병길 on 2018-01-17.
 * Whya5448@gmail.com
 */
public class AppMain {

	private Scanner sc;

	AppMain() {
		sc = new Scanner(System.in);
	}

	public static void main(String[] args) {
		new AppMain().start();
	}

	private void start() {
		System.out.println("1. Zanata PO 다운로드");
		System.out.println("2. PO 폰트 매핑/변환");
		System.out.println("3. ㅇㅇㅇㅇㅇ");
		System.out.println("4. ㄷㄷㄷㄷㄷ");
		System.out.println("5. ㄹㄹㄹㄹㄹ");
		this.getCommand();
	}

	private int getCommand() {
		System.out.print("선택 : ");
		String comm = sc.nextLine();
		try {
			return Integer.parseInt(comm);
		} catch (Exception e) {
			System.out.println("올바르지 않은 명령입니다.");
			System.err.println(e.getMessage());
			return getCommand();
		}
	}
}
