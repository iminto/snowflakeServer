package iminto.github.io;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * 原始版socket调用方法
 */
public class OldServer {
    public static void main(String[] args) {
        Socket socket=null;
        try {
            socket = new Socket("127.0.0.1", 8888);
            InputStream inputStream = socket.getInputStream();
            PrintWriter out=new PrintWriter(socket.getOutputStream());
            out.print("11,22");
            out.flush();
            byte[] bytes=readBytes(inputStream);
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.put(bytes, 0, bytes.length);
            buffer.flip();//need flip
            System.out.println(buffer.getLong());
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    public static byte[] readBytes(InputStream in) throws IOException {
        BufferedInputStream bufin = new BufferedInputStream(in);
        int buffSize = 1024;
        ByteArrayOutputStream out = new ByteArrayOutputStream(buffSize);
        byte[] temp = new byte[buffSize];
        int size = 0;
        while ((size = bufin.read(temp)) != -1) {
            out.write(temp, 0, size);
        }
        bufin.close();
        byte[] content = out.toByteArray();
        return content;
    }
}
