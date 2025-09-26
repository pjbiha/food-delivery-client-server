import java.io.*;
import java.net.*;
import java.util.*;

public class MasterServer {
    private static final int MASTER_PORT = 5000;
    private static final int REDUCER_PORT = 6000;
    private static final List<Socket> workerSockets = new ArrayList<>();
    public static final Map<String, Integer> storeSeWorker = new HashMap<>();
    public static final Map<String, List<String>> uuidToResults = new HashMap<>();

    public static void main(String[] args) {
        if (args.length % 2 != 0) {
            System.out.println("TROPOS XRHSHS: java MasterServer <worker_ip1> <worker_port1> <worker_ip2> <worker_port2> ...");
            return;
        }

        try {
            for (int i = 0; i < args.length; i += 2) {
                String workerIP = args[i];
                int workerPort = Integer.parseInt(args[i + 1]);

                Socket socket = new Socket(workerIP, workerPort);
                workerSockets.add(socket);
                System.out.println("SYNDETHIKE ME TON Worker sto IP: " + workerIP + " PORT: " + workerPort);
            }

            // start async listener for reducer replies
            new ReducerReplyListener(REDUCER_PORT, uuidToResults).start();

            ServerSocket masterSocket = new ServerSocket(MASTER_PORT);
            System.out.println("O MASTER SERVER AKOUEI STO PORT " + MASTER_PORT);
            while (true) {
                Socket clientSocket = masterSocket.accept();
                new ClientHandler(clientSocket, workerSockets, uuidToResults).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {
    private final Socket clientSocket;
    private final List<Socket> workerSockets;
    private final Map<String, List<String>> uuidToResults;

    public ClientHandler(Socket clientSocket, List<Socket> workerSockets, Map<String, List<String>> uuidToResults) {
        this.clientSocket = clientSocket;
        this.workerSockets = workerSockets;
        this.uuidToResults = uuidToResults;
    }

    public void run() {
        try (
            BufferedReader inClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter outClient = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String mhnyma;

            while ((mhnyma = inClient.readLine()) != null) {
                if (!mhnyma.startsWith("ROLE:")) {
                    outClient.println("LATHOS MORFI ENTOLIS");
                    continue;
                }

                String[] split = mhnyma.split(" ", 2);
                if (split.length < 2) {
                    outClient.println("LATHOS ORISMATA ENTOLIS");
                    continue;
                }

                String roleCommand = split[1];
                String[] parts = roleCommand.split(" ");
                String command = parts[0];

                if (command.equals("SEARCH")) {
                    handleSearch(mhnyma, outClient);
                } else {
                    String onomastore = parts[1];
                    int index;

                    if (MasterServer.storeSeWorker.containsKey(onomastore)) {
                        index = MasterServer.storeSeWorker.get(onomastore);
                    } else {
                        int hash = Math.abs(onomastore.hashCode());
                        index = hash % workerSockets.size();
                        if (command.equals("PROSTHIKI_MAGAZIOU") || command.equals("PROSTHIKI_MAGAZIOU_MESW_JSON")) {
                            MasterServer.storeSeWorker.put(onomastore, index);
                        }
                    }

                    Socket workerSocket = workerSockets.get(index);
                    PrintWriter outWorker = new PrintWriter(workerSocket.getOutputStream(), true);
                    BufferedReader inWorker = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()));

                    outWorker.println(mhnyma);
                    String line;
                    while ((line = inWorker.readLine()) != null) {
                        outClient.println(line);
                        if (line.equals("TELOS_APANTISIS")) break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleSearch(String mhnyma, PrintWriter outClient) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String[] parts = mhnyma.split(" ", 8);

        if (parts.length < 8) {
            outClient.println("LATHOS ORISMATA ENTOLIS SEARCH.");
            return;
        }

        String apant = parts[2];
        String lat = parts[3];
        String lon = parts[4];
        String category = parts[5];
        String minStars = parts[6];
        String price = parts[7];

        String newMessage = "SEARCH " + uuid + " " + apant + " " + lat + " " + lon + " " + category + " " + minStars + " " + price;

        System.out.println("STELNETAI SEARCH STON WORKER: " + newMessage);

        for (Socket workerSocket : workerSockets) {
            PrintWriter outWorker = new PrintWriter(workerSocket.getOutputStream(), true);
            outWorker.println(newMessage);
        }

        synchronized (uuidToResults) {
            try {
                uuidToResults.wait(10000); // max 10 sec anamonh
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        List<String> result = uuidToResults.remove(uuid);
        if (result == null) {
            outClient.println("DEN ELAVE APANTHSH APO REDUCER");
            outClient.println("TELOS_APANTISIS");
        } else {
            for (String line : result) {
                outClient.println(line);
            }
            outClient.println("TELOS_APANTISIS");
        }
    }
}

class ReducerReplyListener extends Thread {
    private final int port;
    private final Map<String, List<String>> uuidToResults;

    public ReducerReplyListener(int port, Map<String, List<String>> uuidToResults) {
        this.port = port;
        this.uuidToResults = uuidToResults;
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(">> LISTENING FOR REDUCER REPLIES ON PORT " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                        // diavazoume prwta to UUID pou mas stelnei o Reducer
                        String uuid = in.readLine();
                        if (uuid == null || uuid.isEmpty()) {
                            System.out.println("LATHOS: DEN ELHFTHKE UUID APO REDUCER.");
                            return;
                        }

                        List<String> response = new ArrayList<>();
                        String line;
                        while ((line = in.readLine()) != null) {
                            if (line.equals("TELOS_APANTISIS")) break;
                            response.add(line);
                        }

                        // apothikevoume ta apotelesmata sto map me vasi to UUID
                        synchronized (uuidToResults) {
                            uuidToResults.put(uuid, response);
                            uuidToResults.notifyAll();
                        }

                        System.out.println("ELHFTHE APANTISI GIA UUID: " + uuid + " -> " + response.size() + " katastimata");

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}