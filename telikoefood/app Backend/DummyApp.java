import java.io.*;
import java.net.*;

public class DummyApp {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5005)) {
            System.out.println("DummyApp listening on port 5005...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connection received.");

                new Thread(() -> handleClient(socket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try {
            BufferedReader netIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter netOut = new PrintWriter(socket.getOutputStream(), true);

            ProcessBuilder pb = new ProcessBuilder("java", "Client");
            Process clientProcess = pb.start();

            PrintWriter out = new PrintWriter(clientProcess.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientProcess.getInputStream()));

            netOut.println("CUSTOMER MENU");
            out.println("CUSTOMER");

            while (true) {
                netOut.println("\nEpilexe leitourgia:");
                netOut.println("1. Anazhthsh Katasthmatos");
                netOut.println("2. Lista Proiontwn");
                netOut.println("3. Agora Proiontos");
                netOut.println("4. Bathmologhse Katasthma");
                netOut.println("5. Exodos");

                String epilogi = netIn.readLine();
                if (epilogi == null) break;

                String mhnyma = "";

                if (epilogi.equals("1")) {
                    netOut.println("Theleis anazhthsh me apostash <5km (1) h custom filtra (2)?");
                    String flag = netIn.readLine().trim();

                    double geopla = 0.0;
                    double geomhk = 0.0;
                    if (flag.equals("1")) {
                        netOut.println("Dwse geografiko platos:");
                        geopla = Double.parseDouble(netIn.readLine().trim());

                        netOut.println("Dwse geografiko mhkos:");
                        geomhk = Double.parseDouble(netIn.readLine().trim());
                    }

                    String category = "0", stars = "0", timh = "0";
                    if (flag.equals("2")) {
                        netOut.println("Dwse kathgoria faghtou (0 gia kanena filtro):");
                        category = netIn.readLine().trim();

                        netOut.println("Dwse elaxista asteria (1-5, 0 gia ola):");
                        stars = netIn.readLine().trim();

                        netOut.println("Dwse kathgoria timwn ($,$$,$$$ h 0 gia ola):");
                        timh = netIn.readLine().trim();
                    }

                    mhnyma = String.format("SEARCH %s %.6f %.6f %s %s %s", flag, geopla, geomhk, category, stars, timh);

                } else if (epilogi.equals("2")) {
                    netOut.println("Dwse onoma store:");
                    String storeName = netIn.readLine();
                    mhnyma = "LIST_PROIONTA " + storeName;

                } else if (epilogi.equals("3")) {
                    netOut.println("Dwse onoma store:");
                    String storeName = netIn.readLine();
                    netOut.println("Dwse onoma proiontos:");
                    String productName = netIn.readLine();
                    netOut.println("Dwse posotita:");
                    String quantity = netIn.readLine().trim();
                    mhnyma = "AGORA_PROIONTOS " + storeName + " " + productName + " " + quantity;

                } else if (epilogi.equals("4")) {
                    netOut.println("Dwse onoma store:");
                    String storeName = netIn.readLine();
                    netOut.println("Dwse asteria (1-5):");
                    String stars = netIn.readLine().trim();
                    mhnyma = "RATE_MAGAZI " + storeName + " " + stars;

                } else if (epilogi.equals("5")) {
                    out.println("EXIT_CLIENT");
                    netOut.println("Exodos DummyApp.");
                    break;

                } else {
                    netOut.println("Lathos epilogi.");
                    continue;
                }

                out.println(mhnyma);

                String response;
                while ((response = in.readLine()) != null) {
                    if (response.equals("TELOS_APANTISIS")) break;
                    netOut.println(response);
                }
            }

            netIn.close();
            netOut.close();
            in.close();
            out.close();
            socket.close();
            clientProcess.destroy();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}