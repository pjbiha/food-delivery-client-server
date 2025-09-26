import java.io.*;
import java.util.Scanner;

public class ConsoleManagerApp {
    public static void main(String[] args) {
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "Client"); //dhmiourgei programma, trexei to client.java anti gia terminal
            Process clientProcess = pb.start();


            PrintWriter out = new PrintWriter(clientProcess.getOutputStream(), true);//stelnei entoles ston client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientProcess.getInputStream()));//diabazei ti apantaei o client
            Scanner scanner = new Scanner(System.in);

            //stelnei ton rolo
            out.println("MANAGER");

            while (true) { //emfanizontai oi diathesimes entoles gia managereo
                System.out.println("\nEpilexe leitourgia:");
                System.out.println("1. Prosthese Katasthma");
                System.out.println("2. Prosthese Proion");
                System.out.println("3. Afairese Katasthma");
                System.out.println("4. Afairese Proion");
                System.out.println("5. Emfanise Poliseis Proiontos");
                System.out.println("6. Exodos");

                String epilogi = scanner.nextLine().trim();
                String mhnyma = "";

                if (epilogi.equals("1")) { //mhnymata analoga entolh pou dothike
                    System.out.print("Dwse Onoma Katasthmatos: ");//sto onoma anti gia keno xrhsimopoioume "_" 
                    String onomaKatasthmatos = scanner.nextLine();

                    System.out.print("Dwse Kathgoria: ");
                    String kathgoria = scanner.nextLine().trim();

                    System.out.print("Dwse Gewgrafiko Platos: ");
                    double gewgrafikoPlatos = Double.parseDouble(scanner.nextLine().trim());

                    System.out.print("Dwse Gewgrafiko Mhkos: ");
                    double gewgrafikoMhkos = Double.parseDouble(scanner.nextLine().trim());

                    System.out.print("Dwse Asteria: ");
                    int asteria = Integer.parseInt(scanner.nextLine().trim());

                    System.out.print("Dwse Arithmo Psifwn: ");
                    int arithmosPsifwn = Integer.parseInt(scanner.nextLine().trim());

                    mhnyma = "PROSTHIKI_MAGAZIOU " + onomaKatasthmatos + " " + kathgoria + " " + gewgrafikoPlatos + " " + gewgrafikoMhkos + " " + asteria + " " + arithmosPsifwn;
                } else if (epilogi.equals("2")) {
                    System.out.print("Dwse Onoma Katasthmatos: ");
                    String onomaKatasthmatos = scanner.nextLine();

                    System.out.print("Dwse Onoma Proiontos: ");
                    String onomaProiontos = scanner.nextLine();

                    System.out.print("Dwse Timh: ");
                    double timh = Double.parseDouble(scanner.nextLine().trim());

                    System.out.print("Dwse Posothta: ");
                    int posothta = Integer.parseInt(scanner.nextLine().trim());

                    mhnyma = "PROSTHIKI_PROIONTOS " + onomaKatasthmatos + " " + onomaProiontos + " " + timh + " " + posothta;
                } else if (epilogi.equals("3")) {
                    System.out.print("Dwse Onoma Katasthmatos gia afairesh: ");
                    mhnyma = "AFAIRESH_MAGAZIOU " + scanner.nextLine();
                } else if (epilogi.equals("4")) {
                    System.out.print("Dwse Onoma Katasthmatos: ");
                    String onomaKatasthmatos = scanner.nextLine();

                    System.out.print("Dwse Onoma Proiontos gia Afairesh: ");
                    String onomaProiontos = scanner.nextLine();
                    mhnyma = "AFAIRESH_PROIONTOS " + onomaKatasthmatos + " " + onomaProiontos;
                } else if (epilogi.equals("5")) {
                    System.out.print("Dwse Onoma Katasthmatos gia probolh pwlhsewn: ");
                    mhnyma = "PROION_SALES " + scanner.nextLine();
                } else if (epilogi.equals("6")) {
                    out.println("EXIT_CLIENT");
                    System.out.println("Exodos ConsoleManagerApp.");
                    break;
                } else {
                    System.out.println("Lathos epilogi.");
                    continue;
                }

                //stelnei tin entoli ston Client
                out.println(mhnyma);

                //diavazei kai ektipwnei apantisi
                String response;
                while ((response = in.readLine()) != null) { //stelnei apanthseis mexri na pei TELOS_APANTISIS
                    if (response.equals("TELOS_APANTISIS")) break;
                    System.out.println(response);
                }
            }

            scanner.close();
            out.close();
            in.close();
            clientProcess.destroy();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}