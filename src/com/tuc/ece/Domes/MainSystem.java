/* 
 * 
 * Student ID: Apostolos Zacharopoulos 2015030128
 * Project1:File Editing
 * Data&File Structures Subject
 * 
 */

package com.tuc.ece.Domes;

import java.io.*;
import java.util.Scanner;


public class MainSystem {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String fileName;
		MainSystem sys = new MainSystem();
		SerialSearch serial = new SerialSearch();
		BinarySearch bin = new BinarySearch();
		CacheMemoryBinarySearch cache;
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter the file name: ");
		fileName=sc.nextLine();
		System.out.println("How many keys you would like in the file?");
		int keys =sc.nextInt();
		System.out.println("Which is the size of the page?'\t(Number of keys per page)");
		int size=sc.nextInt();
		sys.FileSetup(fileName,keys,size);
		int option = 0;
		while (option != 5) {
			sys.Menu();
			option = sc.nextInt();
			switch (option) {
			case 1:
				serial.SerialFileSearch(fileName, size,keys);
				break;
			case 2:
				bin.BinaryFileSearch(fileName,size);
				break;
			case 3:
				bin.KeyGroupingBinarySearch(fileName,size);
				break;
			case 4:
				System.out.println("How many pages you want the queue to contain? \n");
				int queueSize = sc.nextInt();
				cache = new CacheMemoryBinarySearch(fileName, queueSize,size);
				break;
			case 5:
				System.out.println("Terminating...");
				System.exit(0);
				break;
		    default:
		    	System.out.println("Invalid choice,please try again!");
		    	break;
			}
		}
		sc.close(); 

	}

	public void Menu() {
		System.out.println("------------------------------------------------------");
		System.out.println("Please choose an option:");
		System.out.println("1) Serial search in file for a random key.");
		System.out.println("2) Binary search in file for a random key.");
		System.out.println("3) Binary search with question grouping.");
		System.out.println("4) Binary search with cache memory use.");
		System.out.println("5) Exit.");
		System.out.println("------------------------------------------------------");
	}

	//In file setup the file is created and being set up according to users input
	public void FileSetup(String filename,int numOfkeys,int pageSize) throws IOException {
		int numOfPages = (numOfkeys / pageSize);   // Number of pages in file.Every page
											            // contain 128 integers (for our project)
		int value = 1;                                 // We use casting because the number of keys is a long variable
		ByteArrayOutputStream bArrayStream = new ByteArrayOutputStream();
		DataOutputStream dStream = new DataOutputStream(bArrayStream);
		RandomAccessFile MyFile = new RandomAccessFile(filename, "rw");
		MyFile.seek(0);

		System.out.println("File setup procedure has started..");
		for (int i = 0; i < numOfPages; i++) {
			value = 1;
			for (int j = 0; j < pageSize; j++) {
				int key = value + i * pageSize;
				dStream.writeInt(key);
				value += 1;
			}
			byte[] buffer = bArrayStream.toByteArray();
			MyFile.write(buffer);
			bArrayStream.reset();
		}
		System.out.println("File setup completed!! \t File size = "+MyFile.length()+" bytes");
		dStream.close();
		MyFile.close();
	}

}
