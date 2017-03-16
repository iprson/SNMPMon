package main_pkg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SNMPMon {

	public static void main(String[] args) throws Exception 
	{
		
		try (MonitorDevice md = new MonitorDevice(args[0], args[1]))
		//try (MonitorDevice md = new MonitorDevice("127.0.0.1", "175"))
		{
			// ?????? ?????????
			md.start();
			// ?????? ?????????????
			md.ReadSNMP();
			
			try (
					FileWriter out = new FileWriter(new File("Interfaces.txt"), true);
				) 
			{
				// ?????? ? ????
				List<InterfaceElement> buffList = null;
				for (Map.Entry<Integer, List<InterfaceElement>> entry : md.getMapInterfaces().entrySet()) 
				{
					System.out.print("Interfaces " + entry.getKey() + ": ");
					out.write("Interfaces " + entry.getKey() + ": ");
					buffList = new ArrayList<InterfaceElement>(entry.getValue());
					for (InterfaceElement interfaceElement : buffList) 
					{
						System.out.print("\t" + interfaceElement.getNameValue());
						out.write("\t" + interfaceElement.getNameValue());
					}
					System.out.println();
					out.write("\n");
			    } 
			} 
			catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		} 
		catch (IOException e) 
		{
			throw new RuntimeException(e);
		}
	}

}