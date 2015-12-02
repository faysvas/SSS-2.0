/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.stork.ss;

/**
 *
 * @author Fay
 */
import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;

public class Monitoring 
{
 public Monitoring(){
        
    }
 
    public void monitoringLog( String data )
    {	
    	try{
    		java.util.Date date= new java.util.Date();
	Timestamp timestamp= new Timestamp(date.getTime());
    		
    		File file =new File("webapps/iss-asign3/monitoring.log");
    		
    		//if file doesnt exists, then create it
    		if(!file.exists()){
    			file.createNewFile();
    		}
    		
    		//true = append file
    		//FileWriter fileWritter = new FileWriter(file.getName(),true);
    	        //BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
    	        //bufferWritter.write(timestamp.toString()+" "+data+"\r\n" );
    	        //bufferWritter.close();
                data=timestamp.toString().substring(0, 19)+" "+data+"\r\n";
                 Files.write(Paths.get("webapps/iss-asign3/monitoring.log"), data.getBytes(), StandardOpenOption.APPEND);
    	    
	      
	        
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }
}