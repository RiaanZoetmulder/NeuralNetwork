package Datastorage;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class dataStorage {
	
	// variable for storing all the objects
	final public List<SensorsActions> sensActList = new ArrayList<SensorsActions>();
	
	// Method for adding a sensors and actions object to list
	public void Add(SensorsActions sensAct){
		sensActList.add(sensAct);
	}
	
	// Method for writing all objects to the text file.
	public void toText(){
		
		try {
			PrintWriter file = new PrintWriter("TrainingData.txt", "UTF-8");
			for(SensorsActions element: sensActList){
				
				
				// Print the actions 
				for(int i = 0; i < element.getActions().length; i++){
					file.print(element.getActions()[i]);
					if(i < element.getActions().length)
						file.print(",");
				}
				//file.print("],[");
				
				// print the sensors
				for(int i = 0; i < element.getSensors().length; i++){
					file.print(element.getSensors()[i]);
					if(i < element.getSensors().length - 1)
						file.print(",");
				}
				//file.println("]]");
				file.println();
				
			
			}
			file.close();
		} catch (FileNotFoundException e) {
			System.out.println("Didn't work");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			System.out.println("Didn't work");
			e.printStackTrace();
		}
		
	}

}
