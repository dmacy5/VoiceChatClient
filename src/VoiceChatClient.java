import java.io.*;
import javax.sound.sampled.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


    public class VoiceChatClient implements Runnable{

        public static BufferedReader in;
        public static PrintWriter out;
        public static Scanner kb = new Scanner(System.in);
        public static String serverAddress;
        public static Socket socket;
        public static BufferedReader keyRead;
        public static String screenName;
        public static DataOutputStream dataOutputStream;
        public static DataInputStream dataInputStream;
        public static TargetDataLine microphone;
        public static SourceDataLine speakers;

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
                    int len = dataInputStream.readInt();
                    byte[] data = new byte[len];
                    dataInputStream.readFully(data);
                    speakers.write(data, 0, 1024);
                }
                //speakers.drain();
                //speakers.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }




        /**
         * Runs the client as an application with a closeable frame.
         */
        public static void main(String[] args) throws Exception {
            try {
                serverAddress = getServerAddress();
                socket = new Socket(serverAddress, 9045);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                keyRead = new BufferedReader(new InputStreamReader(System.in));
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
                    while(true) //Possibly need a way to exit here
                    {
                        numBytesRead = microphone.read(data, 0, CHUNK_SIZE);
                        dataOutputStream.write(data, 0, numBytesRead);
                        dataOutputStream.flush();
                    }
                        //microphone.close();

                }
                catch(LineUnavailableException e){
                    System.out.println(e);
                }



            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
