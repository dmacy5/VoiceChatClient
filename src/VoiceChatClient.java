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

        public static Scanner kb = new Scanner(System.in);
        public static String serverAddress;
        public static Socket socket;
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
                    //int length = dataInputStream.available();
                    byte[] data = new byte[1024];
                    dataInputStream.readFully(data);
                    speakers.write(data, 0, 1024);
                }
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
                socket = new Socket(serverAddress, 9999); //192.168.1.4
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
                            microphone.close();
                            speakers.drain();
                            speakers.close();
                            System.exit(0);
                        }
                    }

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
