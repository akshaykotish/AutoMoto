package com.example.automoto;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Communication {

    String message = "Hello";
    public static String Output = "NA";

    public static String Start()
    {
        Communication communication = new Communication();
        communication.Run_Client();
        return communication.Output;
    }

    public static String Start(String msg)
    {
        Communication communication = new Communication();
        communication.message = msg;
        communication.Run_Client();
        return communication.Output;
    }

    public void Run_Client()
    {
        Thread client = new Thread(new Client());
        client.start();
    }

    class Server implements Runnable{

        @Override
        public void run() {
            Output = "Recieveing";
            try{
                ServerSocket server = new ServerSocket(80);
                Socket socket = server.accept();
                DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                String line ="";
                line = input.readUTF();

                Output = line;
                //Toast.makeText(this, "Data:- " +line, Toast.LENGTH_SHORT).show();

                socket.close();
                input.close();

            }
            catch (Exception ex)
            {
                Output = ex.toString();
                //Toast.makeText(this, "Error :- " + ex.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    class Client implements Runnable{

        @Override
        public void run() {
            Output = "Sending";
            try {
                Socket socket = new Socket("192.168.1.184", 80);

                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                output.writeChars(message);


                Output = "Recieveing....";

                InputStreamReader input = new InputStreamReader(socket.getInputStream());


                String line ="";
                int a = 0;

                a = input.read();
                while(a != 13)
                {
                    char c = (char)a;
                    line = line + c + "";
                    a = input.read();
                }
                Output = line;


                Thread.sleep(1000);
                output.close();
                socket.close();
            }
            catch (Exception ex)
            {
                Output = ex.toString();
                //Toast.makeText(this, "Error :- " + ex.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }


}
