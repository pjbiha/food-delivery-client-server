import java.io.*;
import java.net.*;
import java.util.*;

public class WorkerServer {
    private static String workerIP;
    private static int workerPort;
    public static final Map<String, Store> stores = new HashMap<>();
    private static final Object lock = new Object();

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("TROPOS XRHSHS: java WorkerServer <ip> <port>");
            return;
        }

        workerIP = args[0];
        try {
            workerPort = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("LATHOS PORT, PREPEI NA EINAI ARITHMOS.");
            return;
        }

        ServerSocket serverSocket = new ServerSocket(workerPort, 0, InetAddress.getByName(workerIP));
        System.out.println("O WORKER AKOUEI STO IP " + workerIP + " STO PORT " + workerPort);

        while (true) {
            Socket masterSocket = serverSocket.accept();
            new WorkerHandler(masterSocket).start();
        }
    }

    public static void addStore(Store store) {
        synchronized (lock) {
            stores.put(store.getName(), store);
            System.out.println("PROSTETHIKE TO MAGAZI: " + store.getName());
        }
    }

    public static Store getStore(String name) {
        synchronized (lock) {
            return stores.get(name);
        }
    }

    public static void removeStore(String name) {
        synchronized (lock) {
            stores.remove(name);
            System.out.println("AFAIRETHIKE TO MAGAZI: " + name);
        }
    }

    public static boolean storeExists(String name) {
        synchronized (lock) {
            return stores.containsKey(name);
        }
    }
}

class WorkerHandler extends Thread {
    private final Socket socket;

    public WorkerHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String message;
            while ((message = in.readLine()) != null) {
                String[] parts = message.split(" ", 3);
                String command = parts[0];

                if (command.equals("SEARCH")) {
                    String uuid = parts[1];
                    String filters = parts[2];
                    handleSearch(uuid, filters);
                } else {
                    String response = handleCommand(message);
                    out.println(response);
                    out.println("TELOS_APANTISIS");
                }
            }
            System.out.println("TO CONNECTION EKLEISE APO TON MASTER.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleSearch(String uuid, String filters) {
        Map<String, String> result = MapSearch(filters.split(" "));

        if (!result.isEmpty()) {
            try (Socket reducerSocket = new Socket("127.0.0.1", 7000);
                 PrintWriter outReducer = new PrintWriter(reducerSocket.getOutputStream(), true)) {

                for (Map.Entry<String, String> entry : result.entrySet()) {
                    String output = uuid + "," + entry.getKey() + "," + entry.getValue();
                    System.out.println("STELNW STO REDUCER: " + output); 
                    outReducer.println(output);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, String> MapSearch(String[] parts) {
    System.out.println("PARAMETROI SEARCH:");
    for (int i = 0; i < parts.length; i++) {
        System.out.println("parts[" + i + "]: " + parts[i]);
    }

    String apant = parts[0];
    double lat = Double.parseDouble(parts[1]);
    double lon = Double.parseDouble(parts[2]);
    String category = parts[3];
    int minStars = Integer.parseInt(parts[4]);
    String price = parts[5];

    Map<String, String> result = new LinkedHashMap<>();

    synchronized (WorkerServer.class) {
        for (Store store : WorkerServer.stores.values()) {
            if (!search(apant, store, lat, lon, category, minStars, price)) continue;
            String metadata = String.format("%s,%.6f,%.6f,%d,%s,%d",
                    store.getCategory(),
                    store.getLatitude(),
                    store.getLongitude(),
                    store.getStars(),
                    store.getAveragePriceCategoryOfAvailableProducts(),
                    store.getNoOfVotes());
            result.put(store.getName(), metadata);
        }
    }
    return result;
}


    private boolean search(String apant, Store store, double lat, double lon, String cat, int stars, String price) {
        double dist = haversine(lat, lon, store.getLatitude(), store.getLongitude());
        if ("1".equals(apant)) return dist <= 5.0;
        if ("2".equals(apant)) {
            if (!"0".equals(cat) && !store.getCategory().equalsIgnoreCase(cat)) return false;
            if (stars > 0 && store.getStars() < stars) return false;
            if (!"0".equals(price) && !store.getAveragePriceCategoryOfAvailableProducts().equals(price)) return false;
        }
        return true;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
//diaxeirisi entolwn apo ton client
    private String handleCommand(String message) {
        try {
            String[] tokens = message.split(" ", 3);
            if (!tokens[0].startsWith("ROLE:")) return "LEIPEI H SYMPLHRWSH ROLOU!";

            String role = tokens[0].substring(5);
            String commandLine = tokens[1] + " " + (tokens.length > 2 ? tokens[2] : "");
            String[] parts = commandLine.split(" ");
            String command = parts[0];

            Set<String> managerOnly = Set.of("PROSTHIKI_MAGAZIOU", "PROSTHIKI_PROIONTOS", "AFAIRESH_MAGAZIOU", "PROSTHIKI_MAGAZIOU_MESW_JSON","AFAIRESH_PROIONTOS","PROION_SALES");
            Set<String> customerOnly = Set.of("AGORA_PROIONTOS", "LIST_PROIONTA", "RATE_MAGAZI");

            if (managerOnly.contains(command) && !role.equals("MANAGER"))
                return "MH EKSOUSIODOTHMENH PROSVASH: MONO O MANAGER MPOREI NA TREKSEI THN ENTOLH " + command;
            if (customerOnly.contains(command) && !role.equals("CUSTOMER"))
                return "MH EKSOUSIODOTHMENH PROSVASH: MONO O CUSTOMER MPOREI NA TREKSEI THN ENTOLH " + command;
            //entoli gia na prosthesei katastima
            if (command.equals("PROSTHIKI_MAGAZIOU")) {
                String storeName = parts[1];
                String category = parts[2];
                double latitude = Double.parseDouble(parts[3]);
                double longitude = Double.parseDouble(parts[4]);
                int stars = Integer.parseInt(parts[5]);
                int noOfVotes = Integer.parseInt(parts[6]);

                Store store = new Store(storeName, category, latitude, longitude, stars, noOfVotes);
                WorkerServer.addStore(store);
                return "PROSTHETIKE TO MAGAZI ME ONOMA: " + storeName;
            }
            //entoli gia prosthiki proiontos
            if (command.equals("PROSTHIKI_PROIONTOS")) {
                String storeName = parts[1];
                String productName = parts[2];
                double price = Double.parseDouble(parts[3]);
                int amount = Integer.parseInt(parts[4]);

                Store store = WorkerServer.getStore(storeName);
                if (store != null) {
                    store.addProduct(productName, price, amount);
                    return "PROSTETHIKE TO PROION: " + productName;
                } else {
                    return "DEN VRETHIKE TO MAGAZI!";
                }
            }
            //entoli gia anazhthsh proiontwn se lista
            if (command.equals("LIST_PROIONTA")) {
                String storeName = parts[1];
                Store store = WorkerServer.getStore(storeName);
                return store.listProducts().toString();
            }
            //entoli gia agora proiontos
            if (command.equals("AGORA_PROIONTOS")) {
                String storeName = parts[1];
                String productName = parts[2];
                int quantity = Integer.parseInt(parts[3]);

                Store store = WorkerServer.getStore(storeName);
                if (store != null) {
                    boolean success = store.buyProduct(productName, quantity);
                    if (success) {
                        return "H AGORA PETYXE!";
                    } else {
                        return "DEN VRETHIKE ARKETO APOTHEMA.";
                            }
                } else {
                    return "DEN VRETHIKE TO MAGAZI!";
                }
            }
            //entoli gia afairesh katastimatos
            if (command.equals("AFAIRESH_MAGAZIOU")) {
                String storeName = parts[1];
                if (WorkerServer.storeExists(storeName)) {
                    WorkerServer.removeStore(storeName);
                    return "TO MAGAZI AFAIRETHIKE.";
                } else {
                    return "TO MAGAZI DEN VRETHIKE!";
                }
            }
            //entoli gia na emfanisei tis pwlhseis enos proiontos
            if (command.equals("PROION_SALES")) {
                String storeName = parts[1];
                Store store = WorkerServer.getStore(storeName);
                return store != null ? store.getSalesReport() : "TO MAGAZI DEN VRETHIKE!";
            }
            //entoli gia na prosthesei katastima mesw json
            if (command.equals("PROSTHIKI_MAGAZIOU_MESW_JSON")) {
                String jsonData = message.substring("PROSTHIKI_MAGAZIOU_MESW_JSON ".length()).replace("\\\"", "\"").replace("\\\\", "\\");
                Store store = StoreFactory.fromJson(jsonData);
                if (store != null) {
                    WorkerServer.addStore(store);
                    return "FORTWSE APO TO JSON TO MAGAZI ME ONOMA: " + store.getName();
                } else {
                    return "LATHOS JSON FORMAT!";
                }
            }
            //entoli gia na vathmologhsei to katastima
            if (command.equals("RATE_MAGAZI")) {
                String storeName = parts[1];
                int starsGiven = Integer.parseInt(parts[2]);
                Store store = WorkerServer.getStore(storeName);
                if (store != null) {
                    return store.addRating(starsGiven);
                } else {
                    return "TO MAGAZI DEN VRETHIKE!";
                }

            }
            //entoli gia na afairesei proionta apo th lista enos katastimatos
            if (command.equals("AFAIRESH_PROIONTOS")) {
                String storeName = parts[1];
                String productName = parts[2];
                Store store = WorkerServer.getStore(storeName);
                if (store != null) {
                    boolean removed = store.removeProduct(productName);
                    if (removed) {
                        return "Product removed.";
                    } else {
                        return "Product not found.";
                    }

                } else {
                    return "TO MAGAZI DEN VRETHIKE!";
                }
            }

            return "AGNWSTO COMMAND !";

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR KATA THN DIARKEIA ANAGNWSHS TOU COMMAND.";
        }
    }
}