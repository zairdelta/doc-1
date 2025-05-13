package com.woow.it;

import javax.net.ssl.SSLSocketFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SSLSocketTest {
    public static void main(String[] args) throws Exception {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try (Socket socket = factory.createSocket("0rbjt9.stackhero-network.com", 63338)) {
            OutputStream out = socket.getOutputStream();
            out.write("test".getBytes());
            out.flush();

            InputStream in = socket.getInputStream();
            int b = in.read();
            System.out.println("Read byte: " + b);
        }
    }
}
