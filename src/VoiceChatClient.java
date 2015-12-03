import java.io.ByteArrayOutputStream;
import javax.sound.sampled.*;

/**
 * Created by dmacy on 12/3/2015.
 */

public class VoiceChatClient {


    public static void main(String[] args){
        AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
        TargetDataLine microphone;
        SourceDataLine speakers;
        try {
            microphone = AudioSystem.getTargetDataLine(format);

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);

            microphone.open(format);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int numBytesRead;
            int CHUNK_SIZE = 1024;
            byte[] data = new byte[microphone.getBufferSize() / 5];

            microphone.start();

            int bytesRead = 0;
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
            speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);

            speakers.open(format);
            speakers.start();
        }
        catch(LineUnavailableException e){
            System.out.println(e);
        }
    }
}
