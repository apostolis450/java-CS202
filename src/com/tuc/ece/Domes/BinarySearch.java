package com.tuc.ece.Domes;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

 
public class BinarySearch {
	private int numberOfkeysInFile; //I use this variable so the program works for every sorted file
	private int NumberOfSearches=10000;
	private int SizeInBytes;
	//#######################################################################################################
    //this method search and array for a key according to binary search method
	public boolean BinaryArraySearch(int[] array, int key, int arraySize) {
		int lowerBound = 1;
		int upperBound = arraySize;
		boolean found = false;
		int arrayMid;
		while (upperBound >= lowerBound) {
			if (array[upperBound-1] < key){
				return false;
			}
			arrayMid = lowerBound + (upperBound - lowerBound) / 2;
			if (array[arrayMid-1] < key) {
				lowerBound = arrayMid + 1;
				continue;
			}
			else if (array[arrayMid-1] > key) {
				upperBound = arrayMid - 1;
				continue;
			}
			else if (array[arrayMid-1] == key) {
				return found = true;
			}
		}
		return found;
	}
	//#######################################################################################################
	//this method search a file for a key according to binary search method
	public void BinaryFileSearch(String file,int pageSize) throws IOException{
		SizeInBytes=pageSize*4; //integer allocates 4 bytes of memory
		
		RandomAccessFile fileToSearch = new RandomAccessFile(file, "rw");
		byte[] buffer = new byte[SizeInBytes];
		int[] intBuffer = new int[buffer.length / 4];
		ByteArrayInputStream bArrayStream = new ByteArrayInputStream(buffer);
		DataInputStream dStream = new DataInputStream(bArrayStream);
		
		
		int lowerBound;
		int upperBound;
		boolean found;
		int fileMid;
		int randomKey;
		int AccessSum;
		float AverageDiskAccesses =0;
		numberOfkeysInFile=(int) (fileToSearch.length()/4);
		for (int numOfSearches = 0; numOfSearches < NumberOfSearches; numOfSearches++) { //loop to search for 10 thousand random keys in file

			lowerBound = 1;
			upperBound = numberOfkeysInFile/pageSize;
			AccessSum=0;
		    randomKey = ThreadLocalRandom.current().nextInt(1, numberOfkeysInFile);
			found=false;
			while(!found){
				fileMid=lowerBound + (upperBound - lowerBound)/2;
				fileToSearch.seek(SizeInBytes*(fileMid-1));                  //pointer moves to first byte of middle page
				bArrayStream.reset();
				fileToSearch.read(buffer, 0, SizeInBytes);
				AccessSum+=1;			
				for (int i = 0; i < pageSize; i++) {                  //Here i create an array with  integers,as much as
					intBuffer[i] = dStream.readInt();           // a page contains
				}
				if(intBuffer[0]>randomKey){                                 //if the first value of the middle page is greater than the key,then the
					upperBound=fileMid-1;                                   //key is at the left half of the file
					continue;
				} 
				else if(intBuffer[(intBuffer.length-1)]<randomKey){         //if the lst value of the middle page is less than the key,then the
					lowerBound=fileMid+1;                                   //key is at the right half of the file
					continue;
				}
				else{
					found=this.BinaryArraySearch(intBuffer, randomKey, intBuffer.length);
					if(found==true){
						break;
					}
				}
			}
			AverageDiskAccesses+=AccessSum;
		}
		AverageDiskAccesses = AverageDiskAccesses/(NumberOfSearches);
		System.out.printf("Average number of disk accesses: %.2f\n",AverageDiskAccesses);
		fileToSearch.close();
		dStream.close();
		bArrayStream.close();
	}
	//#######################################################################################################
	//this method search is done using binary search method and key grouping
	public void KeyGroupingBinarySearch(String file,int pageSize) throws IOException{
		SizeInBytes=pageSize*4;
		
		//Streams and buffers setup to read from the file
		RandomAccessFile fileToSearch = new RandomAccessFile(file, "rw");
		byte[] buffer = new byte[SizeInBytes];
		int[] intBuffer = new int[buffer.length / 4];
		int[] randomKeys = new int[NumberOfSearches];
		ByteArrayInputStream bArrayStream = new ByteArrayInputStream(buffer);
		DataInputStream dStream = new DataInputStream(bArrayStream);
		//variables declaration
		numberOfkeysInFile=(int) (fileToSearch.length()/4);
		int fileMid;
		int randomKey;
		int tested=0;
		boolean found;
		int AccessSum=0;
		int AverageAccesses=0;
		boolean next;
		//Storing all random keys in an array and then sorting it
		for(int i=0;i<NumberOfSearches;i++){                                              
			randomKey = ThreadLocalRandom.current().nextInt(1, numberOfkeysInFile);
			randomKeys[i]=randomKey;
		}
		Arrays.sort(randomKeys); //sort the array
		//this loop tests every key if it exists in file.
		while(tested<NumberOfSearches){
			int lowerBound = 1;
			int upperBound = numberOfkeysInFile/128;
			int key=randomKeys[tested];
			AccessSum=0;
			found=false;
			while(!found){
				fileMid=lowerBound + (upperBound - lowerBound)/2;
				fileToSearch.seek(512*(fileMid-1));                  //pointer moves to first byte of middle page
				bArrayStream.reset();
				fileToSearch.read(buffer, 0, 512);
				AccessSum+=1;			
				for (int i = 0; i < pageSize; i++) {                  //Here i create an array with integers,as much as
					intBuffer[i] = dStream.readInt();           // a page contains
				}
				if(intBuffer[0]>key){                                 //if the first value of the middle page is greater than the key,then the
					upperBound=fileMid-1;                                   //key is at the left half of the file
					continue;
				} 
				else if(intBuffer[(intBuffer.length-1)]<key){         //if the lst value of the middle page is less than the key,then the
					lowerBound=fileMid+1;                                   //key is at the right half of the file
					continue;
				}
				else{
					found=this.BinaryArraySearch(intBuffer, key, intBuffer.length);
					if(found==true){
						next=true;             //This loop is true while the next keys exist in the page which is read at main memory this moment
						while(next){
							next=PageSearch(intBuffer,randomKeys,randomKeys.length,tested);
							tested++;
						}
						break;
					}
				}
				
			  }
			AverageAccesses+=AccessSum;
		   }
		AverageAccesses = AverageAccesses/NumberOfSearches;
		System.out.println("Average number of disk accesses: "+AverageAccesses);
		fileToSearch.close();
		dStream.close();
		bArrayStream.close();
		}
	//#######################################################################################################
	//This method checks if the next random number exists in the same page which is already read at main memory
	private boolean PageSearch(int[] intBuffer, int[] rand, int keyArrayLength, int tested) {
		if(tested < keyArrayLength-1){	 //This condition prevent an OutOfBoundsIndexException
		   if(intBuffer[intBuffer.length-1]>=rand[tested+1] && intBuffer[0]<=rand[tested+1]){
				//System.out.println("Next key exists at same page");
				return true;
			}
			else
				return false;	
		}
		else
			return false;
	}

	
}
	

