package com.mycompany.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

public class Server extends Thread{
    private static int port = 8080;
    private static Vector<PrintStream> clients;
    private Socket conexao;
    private String nomeCliente;
    private static List<String> names = new ArrayList<String>();

    public Server(Socket socket) {
        this.conexao = socket;
    }

    public boolean armazena(String newName){
        for (int i=0; i < names.size(); i++){
            if(names.get(i).equals(newName))
            return true;
        }

       names.add(newName);
       return false;
    }

    public void remove(String oldName) {
       for (int i=0; i< names.size(); i++){
            if(names.get(i).equals(oldName))
            names.remove(oldName);
       }
    }

    public static void main(String[] args) {
        clients = new Vector<PrintStream>();

        try {
            ServerSocket server = new ServerSocket(port);
            System.out.println("ServidorSocket rodando na porta "+ port);

            while (true) {
                Socket conexao = server.accept();

                Thread t = new Server(conexao);
                t.start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }
	
    @Override
    public void run(){    
        
        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(this.conexao.getInputStream()));
            PrintStream saida = new PrintStream(this.conexao.getOutputStream());

            this.nomeCliente = entrada.readLine();

            if (this.nomeCliente == null) {
                return;
            }

            if (armazena(this.nomeCliente)){
                saida.println("ER001");
                System.out.println("Este nome ja existe! Conecte novamente com outro Nome.");
                
                while(armazena(this.nomeCliente)){
                    this.nomeCliente = entrada.readLine();
                }
                
                System.out.println(this.nomeCliente + " : Conectado ao Servidor!");
                saida.println("ST001");
            } else {
                System.out.println(this.nomeCliente + " : Conectado ao Servidor!");
                saida.println("ST001");
            }

            clients.add(saida);

            String msg = entrada.readLine();
            while (msg != null && !(msg.trim().equals(""))) {
                sendToAll(saida, msg);
                msg = entrada.readLine();
            }

            System.out.println(this.nomeCliente + " saiu do bate-papo!");
            sendToAll(saida, "saiu do bate-papo!");
            remove(this.nomeCliente);
            clients.remove(saida);
            this.conexao.close();
        } catch (IOException e) {
            System.out.println("Falha na Conexao... .. ."+" IOException: " + e);
        }
    }

    public void sendToAll(PrintStream saida, String msg) throws IOException {
        Enumeration<PrintStream> e = clients.elements();
        
        while (e.hasMoreElements()) {
            PrintStream chat = (PrintStream) e.nextElement();
            chat.println("["+this.nomeCliente+"]: "+msg);
        }
      }
}
