package model;


import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;
import static java.nio.file.StandardCopyOption.*;


public class FTPServer {
	static int PortNo;
	//public ServerSocket datasoc;
	 public static void main(String args[]) throws Exception
	 
	    {
		 	
		 	//System.out.println(args[0]);
		 	if (args.length == 1){
		 		//System.out.println(args[0]);
		 		PortNo=Integer.parseInt(args[0]);	
		 	}
		 	else {
		 		Scanner sc = new Scanner(System.in);
		 		while(PortNo<1024) {
		 		System.out.println("Enter number port (number port >1024): ");
		 		PortNo=sc.nextInt();}
		 	}
	        ServerSocket soc=new ServerSocket(PortNo);
	        ServerSocket datasoc=new ServerSocket(PortNo-1);
	        System.out.println("FTP Server Started on Port Number "+ PortNo);
	        while(true)
	        {
	            System.out.println("Waiting for Connection ...");
	            //System.out.println(123);
	            transferfile t=new transferfile(soc.accept(),datasoc);
            
	        }
	    }
	}



