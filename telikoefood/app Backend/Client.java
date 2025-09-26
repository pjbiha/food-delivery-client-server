import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 5000; //port pou sundeetai o client

        try (
            Socket socket = new Socket(serverAddress, port); //anoigei sundesh me master
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);//stelnei entoles se master
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //diabazw apanthseis apo master
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))//userinput
        ) {
            //diavazei ton rolo apo stdin manager h customer
            String rolos = userInput.readLine().trim().toUpperCase();

            String command;
            while ((command = userInput.readLine()) != null) { //perimenei gia entoles
                if (command.equalsIgnoreCase("EXIT_CLIENT")) {
                    break;
                }

                //stelnei entoli k rolo
                String fullMessage = "ROLE:" + rolos + " " + command;
                out.println(fullMessage);

                //diavazei kai epistrefei mono tin apantisi (kathari)
                String response;
                while ((response = in.readLine()) != null) {
                    System.out.println(response);
                    if (response.equals("TELOS_APANTISIS")) {
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
