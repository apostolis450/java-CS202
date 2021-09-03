package com.tuc.ece.Domes;

import java.io.*;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

public class CacheMemoryBinarySearch {

	LinkedList<int[]> queue = new LinkedList<int[]>();
	RandomAccessFile fileToSearch;
	BinarySearch binary;
	private int NumberOfKeysInFile;
	private int NumberOfPages;
	private int randomKey;
	private int pageSize;
	private int pageSizeInBytes;
	private int NumberOfSearches=10000;
	private int[] filePageKeyFound;
	int[] intBuffer;
	//********************************************************************************************
	//Class constructor
	public CacheMemoryBinarySearch(String file,int queueSize,int pageSize) throws IOException {
		fileToSearch = new RandomAccessFile(file, "rw");
		NumberOfKeysInFile=(int)fileToSearch.length()/4;
		this.pageSize=pageSize;
		NumberOfPages=NumberOfKeysInFile/pageSize;
		QueueSetup(queueSize);
		Search();
	}
	//********************************************************************************************
	//This method initialize the queue with the specified number of pages
	public void QueueSetup(int queueSize) throws IOException{
		pageSizeInBytes=this.pageSize*4;       //integer allocates 4 bytes in memory
		//****************************************************
		byte[] buffer = new byte[pageSizeInBytes];
		//int[] intBuffer;
		ByteArrayInputStream bArrayStream = new ByteArrayInputStream(buffer);
		DataInputStream dStream = new DataInputStream(bArrayStream);
		
		//*****************************************************
		for(int pageCounter=0;pageCounter<queueSize;pageCounter++){ 
			fileToSearch.seek(pageSizeInBytes*pageCounter);
			bArrayStream.reset();
			fileToSearch.read(buffer, 0, pageSizeInBytes);
			intBuffer = new int[buffer.length / 4];
			for (int i = 0; i < this.pageSize; i++) {                                  //Here i create an array with 128 integers,as much as
				intBuffer[i] = dStream.readInt();                            // a page contains
			}
			queue.add(intBuffer);
		}
	}
	//********************************************************************************************
	public void Search() throws IOException {
		boolean found;
		boolean exist;
		int AccessSum;
		int AverageAccesses=0;
		for (int i = 0; i < NumberOfSearches; i++) {        
			randomKey = ThreadLocalRandom.current().nextInt(1, NumberOfKeysInFile); 
			found = QueueSearch(queue.size(), randomKey);
			if (found == true) {
				// number exists in queue
				continue;
			} else { //binary search at file to find the key 
				int lowerBound = 1;
				int upperBound = NumberOfKeysInFile/pageSize;
				int fileMid;
				byte[] buffer = new byte[pageSizeInBytes];
				//int[] intBuffer;
				ByteArrayInputStream bArrayStream = new ByteArrayInputStream(buffer);
				DataInputStream dStream = new DataInputStream(bArrayStream);
				AccessSum=0;
				while(!found){
					fileMid=lowerBound + (upperBound - lowerBound)/2;
					fileToSearch.seek(pageSizeInBytes*(fileMid-1));                  //pointer moves to first byte of middle page
					bArrayStream.reset();
					fileToSearch.read(buffer, 0, pageSizeInBytes);
					AccessSum+=1;			
					for (int i1 = 0; i1 < pageSize; i1++) {                  //Here i create an array with integers,as much as
						intBuffer[i1] = dStream.readInt();                  // a page contains
					}
					
					if(intBuffer[0]>randomKey){                                 //if the first value of the middle page is greater than the key,then the
						upperBound=fileMid-1;                                   //key is at the left half of the file
						continue;
					} 
					else if(intBuffer[(intBuffer.length-1)]<randomKey){         //if the last value of the middle page is less than the key,then the
						lowerBound=fileMid+1;                                   //key is at the right half of the file
						continue;
					}
					else{
						found=binary.BinaryArraySearch(intBuffer, randomKey, intBuffer.length);
						if(found==true){
							filePageKeyFound=new int[intBuffer.length]; //Found the page that contains the key and save to add it in queue
							System.arraycopy(intBuffer, 0, filePageKeyFound, 0, filePageKeyFound.length);
							break;
						}
					}
				}
				AverageAccesses+=AccessSum;
				bArrayStream.close();
				dStream.close();
				exist = queue.contains(filePageKeyFound);
				if (exist == false){ 
					queue.addFirst(filePageKeyFound);
					queue.removeLast();
				}
			    else
					break;
			}

		}
		AverageAccesses/=NumberOfSearches;
		System.out.println("Average number of disk accesses: "+AverageAccesses);
	}
	
	
	
	
	
	
	
	//this method search for key in every page which exist in queue
	//********************************************************************************************
	public boolean QueueSearch(int qSize, int key) {
		int[] elementArray;
		boolean found;
		binary = new BinarySearch();
		for (int i = 0; i < queue.size(); i++) {
			elementArray = queue.get(i);
			found = binary.BinaryArraySearch(elementArray, key, elementArray.length);
			if (found == true)
				return true;
		}
		return false;
	}
	
	
	
		
}

