import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author wuhulala<br>
 * @date 2019/12/23<br>
 * @since v1.0<br>
 */
public class SocketTest {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress("192.168.1.3" , 9001));
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream  = socket.getOutputStream();
            outputStream.write("QUERY TIME ORDER".getBytes());
            outputStream.write("QUERY TIME ORDER".getBytes());
            outputStream.write("QUERY TIME ORDER".getBytes());
            outputStream.write("QUERY TIME ORDER".getBytes());
            outputStream.write("QUERY TIME ORDER".getBytes());
            outputStream.flush();
            InputStreamReader reader = new InputStreamReader(inputStream) ;
            char [] temChar  = new char[40];
            StringBuffer buffer = new StringBuffer( );

            while (reader.read(temChar) != -1){
                buffer.append(temChar);
                System.out.println(buffer.toString() +"\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
