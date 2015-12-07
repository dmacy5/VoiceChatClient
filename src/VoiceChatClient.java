import java.io.*;
import javax.sound.sampled.*;
import java.net.Socket;
import java.util.Scanner;

public class VoiceChatClient implements Runnable{

    private static Scanner kb = new Scanner(System.in);
    private static String serverAddress;
    private static Socket socket;
    private static DataOutputStream dataOutputStream;
    private static DataInputStream dataInputStream;
    private static TargetDataLine microphone;
    private static SourceDataLine speakers;

    /**
     * Prompt for and return the address of the server.
     */
    private static String getServerAddress() {
        System.out.println("Enter IP Address of the Server: ");
        return kb.nextLine();
    }

    /**
     * Connects to the server then enters the processing loop. (Reading all of the input streams)
     */
    public void run() {
        try {
            while (true) {
                // write mic data to stream for immediate playback
                //int length = dataInputStream.available();
                byte[] data = new byte[1024];
                dataInputStream.readFully(data);
                speakers.write(data, 0, 1024);
            }
        } catch (IOException e) {
            System.out.println("Failed receiving input.");
        }
    }

    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        try {
            serverAddress = getServerAddress();
            socket = new Socket(serverAddress, 8888); //192.168.1.4
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());

            //Initializing all of the microphone data
            AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
            try {
                microphone = AudioSystem.getTargetDataLine(format);

                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                microphone = (TargetDataLine) AudioSystem.getLine(info);

                microphone.open(format);

                int numBytesRead;
                int CHUNK_SIZE = 1024;
                byte[] data = new byte[microphone.getBufferSize() / 5];

                microphone.start();

                DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
                speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);

                speakers.open(format);
                speakers.start();


                (new Thread(new VoiceChatClient())).start();

                System.out.println("Type any key to exit");
                while(true) //Possibly need a way to exit here
                {
                    numBytesRead = microphone.read(data, 0, CHUNK_SIZE);
                    dataOutputStream.write(data, 0, numBytesRead);
                    dataOutputStream.flush();
                    if(System.in.available() != 0) {
                        throw new IOException() ;
                    }
                }

            }
            catch(LineUnavailableException e){
                System.out.println("Problem accessing a line.");
            }

        } catch (IOException e) {
            try {
                microphone.close();
                speakers.close();
                System.out.println("Disconnected from server.");
            } catch(NullPointerException ex) {
                System.out.println("Couldn't access server.");
            }
            finally {
                System.out.println("Leaving client...");
            }
        }
    }
}
