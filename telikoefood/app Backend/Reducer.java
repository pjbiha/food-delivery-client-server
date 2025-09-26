import java.io.*;
import java.net.*;
import java.util.*;

public class Reducer {
    private static final int REDUCER_PORT = 7000;
    public static final Map<String, List<String>> resultsMap = new HashMap<>();
    public static final Map<String, Integer> receivedCounts = new HashMap<>();
    public static final Map<String, Timer> uuidTimers = new HashMap<>();
    public static final Object lock = new Object();
    public static int numWorkers;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("TROPOS XRHSHS: java Reducer <numWorkers>");
            return;
        }

        numWorkers = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(REDUCER_PORT)) {
            System.out.println("O REDUCER AKOUEI STO PORT " + REDUCER_PORT);
            while (true) {
                Socket workerSocket = serverSocket.accept();
                new ReducerHandler(workerSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //prosthetei apotelesmata apo tous workers
    // kai elegxei an exoun stalei ola ta apotelesmata
    public static void addResult(String uuid, String data) {
        synchronized (lock) {
            System.out.println("APOTHIKEUW GIA TO UUID " + uuid + ": " + data);
            resultsMap.computeIfAbsent(uuid, k -> new ArrayList<>()).add(data);

            int count = receivedCounts.getOrDefault(uuid, 0) + 1;
            receivedCounts.put(uuid, count);
            System.out.println("LAMVANO " + count + " APO " + numWorkers + " WORKERS GIA TO UUID: " + uuid);

            if (!uuidTimers.containsKey(uuid)) {
                Timer timer = new Timer();
                uuidTimers.put(uuid, timer);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("1 DEYTERO PERASE. KALEITAI sendResultsToMaster GIA TO UUID: " + uuid);
                        sendResultsToMaster(uuid);
                    }
                }, 1000);
            }

            if (count == numWorkers) {
                System.out.println("OLI OI WORKERS EXOUN STALEI GIA TO UUID: " + uuid);
                Timer t = uuidTimers.remove(uuid);
                if (t != null) t.cancel();
            }
        }
    }
    // pairnei ta apotelesmata gia ena sigkekrimeno uuid
    // kai ta epistrepsei ston master
    public static List<String> getResults(String uuid) {
        synchronized (lock) {
            uuidTimers.remove(uuid); // cleanup
            List<String> results = resultsMap.remove(uuid);
            receivedCounts.remove(uuid);
            if (results == null) {
                System.out.println("DEN VRETHIKE KANENA APOTELESMA GIA TO UUID: " + uuid);
            } else {
                System.out.println("APOSTELW APOTELESMATA GIA TO UUID " + uuid + ": " + results);
            }
            return results;
        }
    }
    // stelnei ta apotelesmata ston master    
    public static void sendResultsToMaster(String uuid) {
        List<String> results = Reducer.getResults(uuid);

        try (Socket masterSocket = new Socket("127.0.0.1", 6000);
             PrintWriter out = new PrintWriter(masterSocket.getOutputStream(), true)) {

            System.out.println("SENDRESULTSTOMASTER KALEITAI GIA UUID: " + uuid);
            out.println(uuid);

            if (results != null && !results.isEmpty()) {
                System.out.println("APOSTELOUME: " + results.size() + " GRAMMES");
                for (String line : results) {
                    out.println(line);
                }
            } else {
                System.out.println("KENA H NULL RESULTS");
            }

            out.println("TELOS_APANTISIS");
            System.out.println("STELNW 'TELOS_APANTISIS' STON MASTER.");

        } catch (IOException e) {
            System.out.println("LATHOS STO SEND TO MASTER");
            e.printStackTrace();
        }
    }
}

class ReducerHandler extends Thread {
    private final Socket socket;

    public ReducerHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (BufferedReader inWorker = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter outWorker = new PrintWriter(socket.getOutputStream(), true)) {

            String line;
            System.out.println("PERIMENW NA LAVW MINYMA APO WORKER H MASTER...");

            while ((line = inWorker.readLine()) != null) {
                System.out.println("LAMVANO APO WORKER: " + line);

                if (line.length() == 36 && line.contains("-")) {
                    String uuid = line.trim();
                    System.out.println("LAMVANO AITHMA MASTER GIA UUID: " + uuid);
                    checkForCompletion(uuid);
                    continue;
                }

                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    String uuid = parts[0];
                    String data = parts[1];

                    Reducer.addResult(uuid, data);
                }
            }

            System.out.println("TELIOSA ME TON WORKER. ELEGXOS GIA MHDENIKES EGGRAFES...");
            checkForCompletionOnWorkerExit();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkForCompletion(String uuid) {
        synchronized (Reducer.lock) {
            int count = Reducer.receivedCounts.getOrDefault(uuid, 0);
            System.out.println("CHECKING COMPLETION FOR UUID: " + uuid + " - " + count + " / " + Reducer.numWorkers);

            if (count >= Reducer.numWorkers) {
                Reducer.sendResultsToMaster(uuid);
            } else {
                System.out.println("AKOMA PERIMENW EGGRAFES GIA TO UUID: " + uuid);
            }
        }
    }

    private void checkForCompletionOnWorkerExit() {
        synchronized (Reducer.lock) {
            for (String uuid : new ArrayList<>(Reducer.receivedCounts.keySet())) {
                int count = Reducer.receivedCounts.get(uuid);
                System.out.println("ELEGXOS GIA UUID: " + uuid + " ME COUNT: " + count + " / " + Reducer.numWorkers);

                if (count >= Reducer.numWorkers) {
                    System.out.println("KALEITAI sendResultsToMaster GIA UUID: " + uuid);
                    Reducer.sendResultsToMaster(uuid);
                }
            }
        }
    }

    
}
