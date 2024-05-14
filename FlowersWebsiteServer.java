import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FlowersWebsiteServer {
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    ServerSocket serverSocket;

    List<String> screens = List.of(
            "/index.html",
            "/roses.html",
            "/daisies.html"
    );
    List<String> images = List.of(
            "/images/index_1.jpg",
            "/images/index_2.jpg",
            "/images/index_3.jpg",
            "/images/index_4.jpg",
            "/images/roses_1.jpg",
            "/images/roses_2.jpg",
            "/images/roses_3.jpg",
            "/images/roses_4.jpg",
            "/images/daisies_1.jpg",
            "/images/daisies_2.jpg",
            "/images/daisies_3.jpg",
            "/images/daisies_4.jpg"
    );

    public void listenConnections(){
        try {
            serverSocket = new ServerSocket(8084);

            while (true){
                socket = serverSocket.accept();
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream());

                String line = "";
                while((line = in.readLine()).length() > 0){
                    String path = line.split(" ")[1];

                    if (screens.contains(path)) 
                        sendHtml("." + path);
                    else if (images.contains(path))
                        sendImage("." + path);
                    else
                        sendFilesPage();
                }

                System.out.println("\n");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendHtml(String path){
        try {
            StringBuilder contentBuilder = new StringBuilder();
            BufferedReader in = new BufferedReader(new FileReader(path));
            String str;

            while ((str = in.readLine()) != null)
                contentBuilder.append(str);

            in.close();

            String content = contentBuilder.toString();

            String response = getResponse("text/html", content.getBytes().length, content);

            out.println(response);
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendImage(String path) {
        try {
            FileInputStream inFile  = new FileInputStream(path);
            DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());

            Path filePath = Paths.get(path);
            byte[] content = Files.readAllBytes(filePath);

            inFile.read(content);

            String response = getResponse("image/jpg", content.length, "");

            outToClient.writeBytes(response);
            outToClient.write(content, 0, (int)content.length);
            outToClient.flush();
            inFile.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendFilesPage() {
        try {
            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append("<html><head><title>Arquivos</title></head><body>");
            contentBuilder.append("<h1>Arquivos:</h1><ul>");

            File[] files = new File("./").listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (fileName.endsWith(".html"))
                        contentBuilder.append("<li><a href=\"").append(fileName).append("\">").append(fileName).append("</a></li>");
                    else
                        contentBuilder.append("<li>").append(fileName).append("</li>");
                }
            }
            contentBuilder.append("</ul></body></html>");
            String content = contentBuilder.toString();

            String response = getResponse("text/html", content.getBytes().length, content);

            out.println(response);
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String getResponse(String contentType, long contentLength, String content){
        
        return "HTTP/1.1 200 OK\n" +
               "Content-Type: " + contentType + "\n" +
               "Server: SISDIST Server 1.0\n" +
               "Connection: close\n" +
               "Content-Length: " + contentLength + "\n" +
               "\n" +
               content;
    }

    public static void main(String[] args) throws IOException {
        FlowersWebsiteServer server = new FlowersWebsiteServer();
        server.listenConnections();
    }
}
