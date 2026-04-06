package project_2403;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

/**
 * 서버와의 소켓 통신을 담당하는 싱글톤 클라이언트
 *
 * ★ Railway 배포 후 아래 두 값을 수정하세요 ★
 * Railway 대시보드 → 서비스 → Settings → Networking → TCP Proxy 에서 확인
 *
 * 실행 시 JVM 옵션으로도 지정 가능:
 *   java -Dserver.host=monorail.proxy.rlwy.net -Dserver.port=54321 -jar client.jar
 */
public class MultiplayerClient {
    private static final String SERVER_HOST = System.getProperty(
        "server.host", "localhost"          // ← Railway 호스트로 변경
    );
    private static final int SERVER_PORT = Integer.parseInt(System.getProperty(
        "server.port", "12345"              // ← Railway TCP Proxy 포트로 변경
    ));

    private static MultiplayerClient instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<String> messageListener;
    private boolean connected = false;

    private MultiplayerClient() {}

    public static MultiplayerClient getInstance() {
        if (instance == null) instance = new MultiplayerClient();
        return instance;
    }

    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            connected = true;

            Thread receiver = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (messageListener != null) {
                            final String msg = line;
                            javax.swing.SwingUtilities.invokeLater(() -> messageListener.accept(msg));
                        }
                    }
                } catch (IOException e) {
                    connected = false;
                    if (messageListener != null)
                        javax.swing.SwingUtilities.invokeLater(() -> messageListener.accept("DISCONNECTED"));
                }
            });
            receiver.setDaemon(true);
            receiver.start();
            return true;
        } catch (IOException e) {
            connected = false;
            return false;
        }
    }

    public void setMessageListener(Consumer<String> listener) { this.messageListener = listener; }
    public void send(String msg) { if (out != null) out.println(msg); }
    public boolean isConnected() { return connected; }

    public void disconnect() {
        connected = false;
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
        instance = null;
    }
}