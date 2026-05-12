package project_2403;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class MultiplayerClient {
    private static final String SERVER_HOST = System.getProperty(
        "server.host", "https://rainbow-holdem-server.onrender.com"
    );
    private static final int SERVER_PORT = Integer.parseInt(System.getProperty(
        "server.port", "10000"
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
