package model;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class transferfile extends Thread {
	static Socket ClientSoc, dataSoc;
	static ServerSocket DataSoc;
	DataInputStream dinput;
	DataOutputStream doutput;

	transferfile(Socket soc, ServerSocket datasoc) {
		try {
			ClientSoc = soc;
			DataSoc = datasoc;
			dinput = new DataInputStream(ClientSoc.getInputStream());
			doutput = new DataOutputStream(ClientSoc.getOutputStream());
			System.out.println("FTP Client Connected ...");
			String use = dinput.readUTF();
			if (use.compareTo("test") == 0) {
				String pass = dinput.readUTF();
				if (pass.compareTo("test") == 0) {
					doutput.writeUTF("Success");
					System.out.println("User logged in successfully");

				} else {
					doutput.writeUTF("Failure");
				}

			} else {
				doutput.writeUTF("Failure");
			}
			start();

		} catch (Exception ex) {
		}
	}

	void SendFile() throws Exception {
		DataInputStream datain;
		DataOutputStream dataout;
		dataSoc = DataSoc.accept();
		datain = new DataInputStream(dataSoc.getInputStream());
		dataout = new DataOutputStream(dataSoc.getOutputStream());

		String filename = dinput.readUTF();
		File f = new File(filename);
		if (!f.exists()) {
			doutput.writeUTF("File Not Found");
			return;
		} else {
			String path = f.getAbsolutePath();
			String temppath = "/tmp/";
			File tmpfile = new File("/tmp/" + filename);
			Path target = Paths.get(temppath + f.getName());
			Path source = Paths.get(path);
			
			try {

				Files.copy(source, target, REPLACE_EXISTING);

			} catch (IOException e) {

				e.printStackTrace();
			}

			doutput.writeUTF("READY");
			FileInputStream fin = new FileInputStream(tmpfile);
			doutput.writeDouble(f.length());
			int ch;
			do {
				ch = fin.read();
				dataout.writeUTF(String.valueOf(ch));
			} while (ch != -1);
			fin.close();
			doutput.writeUTF("File Receive Successfully");
		}
	}

	void ReceiveFile() throws Exception {
		DataInputStream datain;
		DataOutputStream dataout;
		dataSoc = DataSoc.accept();
		datain = new DataInputStream(dataSoc.getInputStream());
		dataout = new DataOutputStream(dataSoc.getOutputStream());

		String filename = dinput.readUTF();
		if (filename.compareTo("File not found") == 0) {
			return;
		}
		File f = new File(filename);
		String option;

		if (f.exists()) {
			doutput.writeUTF("File Already Exists");
			option = dinput.readUTF();
		} else {
			doutput.writeUTF("SendFile");
			option = "Y";
		}

		if (option.compareTo("Y") == 0) {

			String path = f.getAbsolutePath();
			String temppath = "/tmp/";

			File tmpfile = new File("/tmp/" + filename);

			FileOutputStream fout = new FileOutputStream(tmpfile);
			int ch;
			String temp;
			do {
				temp = datain.readUTF();
				ch = Integer.parseInt(temp);
				if (ch != -1) {
					fout.write(ch);
				}
			} while (ch != -1);
			fout.close();
			Path source = Paths.get(temppath + f.getName());
			Path target = Paths.get(path);

			try {

				Files.move(source, target, REPLACE_EXISTING);

			} catch (IOException e) {

				e.printStackTrace();
			}

			File delfile = new File(temppath, filename);
			delfile.delete();
			doutput.writeUTF("File Send Successfully");
		} else {
			return;
		}

	}

	void Pwd() throws Exception {
		DataInputStream datain;
		DataOutputStream dataout;
		dataSoc = DataSoc.accept();
		datain = new DataInputStream(dataSoc.getInputStream());
		dataout = new DataOutputStream(dataSoc.getOutputStream());

		File file = new File(".");
		String dir = file.getAbsolutePath();
		System.out.println(dir);
		dataout.writeUTF(dir);
		
	}

	void getFiles() throws Exception {
		DataInputStream datain;
		DataOutputStream dataout;
		dataSoc = DataSoc.accept();
		datain = new DataInputStream(dataSoc.getInputStream());
		dataout = new DataOutputStream(dataSoc.getOutputStream());
		String dir = System.getProperty("user.dir");
		File folder = new File(dir);
		File[] listofFiles = folder.listFiles();

		int count = 0;
		for (int i = 0; i < listofFiles.length; i++) {
			if (listofFiles[i].isFile()) {
				count++;
			}
		}
		doutput.writeInt(count);

		for (File file : listofFiles) {
			if (file.isFile()) {
				
				doutput.writeUTF(file.getName());

			}
		}

	}

	public void run() {

		while (true) {
			try {
				String Command = dinput.readUTF();

				if (Command.compareTo("GET") == 0) {
					System.out.println("\tGET Command Received ...");
					SendFile();
					continue;
				} else if (Command.compareTo("SEND") == 0) {
					System.out.println("\tSEND Command Receiced ...");
					ReceiveFile();
					continue;
				} else if (Command.compareTo("DISCONNECT") == 0) {
					System.out.println("\tDisconnect Command Received ...");
					doutput.flush();
					ClientSoc.close();
					// System.exit(1);
				} else if (Command.compareTo("PWD") == 0) {
					System.out.println("\tPWD Command Received ...");
					Pwd();
					continue;
				} else if (Command.compareTo("getFiles") == 0) {
					System.out.println("\tgetFiles Command Received ...");
					getFiles();
					continue;
				}

			} catch (Exception ex) {
			}
		}
	}

}
