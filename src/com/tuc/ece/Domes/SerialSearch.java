package com.tuc.ece.Domes;

import java.io.*;
import java.util.concurrent.ThreadLocalRandom;

public class SerialSearch {

	private int numOfKeys;
	private int numofPages;
	private int pageSize;
	private int randomKey;

	public void SerialFileSearch(String file,int size,int keys) throws IOException {

		boolean found = false;                        //Setting up the file properties
		this.pageSize=size;         
		this.numOfKeys=keys;
		this.numofPages= this.numOfKeys/pageSize;
		int SizeInBytes=pageSize*4;
		
		BinarySearch binS = new BinarySearch();                           //Instance of BinarySearch Class so i can use its method
		RandomAccessFile fileToSearch = new RandomAccessFile(file, "rw"); 


		byte[] buffer = new byte[SizeInBytes];
		int[] intBuffer = new int[buffer.length / 4];  // This buffer contains the keys of the file.Every 4 bytes are converted
		                                              //to an integer.Size is devided by 4 because sizeof(integer)=4 bytes
		ByteArrayInputStream bArrayStream = new ByteArrayInputStream(buffer);
		DataInputStream dStream = new DataInputStream(bArrayStream);

		System.out.println("This procedure may take some time to complete...");
		int AverageDiskAccesses =0;
		
		for (int numOfSearches = 0; numOfSearches < 10000; numOfSearches++) { //loop to search for 10 thousand random keys in file
            
			int AccessSum=0;
			randomKey = ThreadLocalRandom.current().nextInt(1, numOfKeys);

			for (int pageCounter = 0; pageCounter < numofPages; pageCounter++) { //loop for accessing every page of file
				fileToSearch.seek(SizeInBytes * pageCounter);
				bArrayStream.reset();
				fileToSearch.read(buffer, 0, SizeInBytes);
				AccessSum+=1;			
				for (int i = 0; i < pageSize; i++) {                                  // Here i create an array with  integers,as much as
					intBuffer[i] = dStream.readInt();                               //  a page contains
				}
				found = binS.BinaryArraySearch(intBuffer, randomKey, intBuffer.length);
				if (found == true) {
					break;
				}
			}
			AverageDiskAccesses+=AccessSum;
		}
		AverageDiskAccesses/=10000;
		System.out.println("Average disk accesses : "+AverageDiskAccesses);
		fileToSearch.close();
		dStream.close();
		bArrayStream.close();
	}

}
