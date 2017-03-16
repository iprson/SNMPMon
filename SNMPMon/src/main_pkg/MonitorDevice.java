package main_pkg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

// класс, реализующий чтение характеристик устройства
public class MonitorDevice implements AutoCloseable
{
	private final static String SNMP_COMMUNITY = "public";
	private final static int    SNMP_RETRIES   = 3;
	private final static long   SNMP_TIMEOUT   = 1000L;
	private Snmp snmp = null;
	private TransportMapping transport = null;
	private int countInterfaces = 0;
	private String host;
	// начальный if-oid
	private final String sOid = "1.3.6.1.2.1.2.1.0";
	// массив содержащий пары "номер_интерфейса - характеристики_интерфейса"
	private Map<Integer, List<InterfaceElement>> mapInterfaces = null;
	// массив для характеристик конкретного интерфейса
	private List<InterfaceElement> interfaceInfo = null;
	
	// конструктор, принимающий адрес и порт устроства, с кторого надо считать информацию
	public MonitorDevice(String _host, String _port)
	{
		host = "udp:" + _host + "/" + _port;
	}
	
	public Map<Integer, List<InterfaceElement>> getMapInterfaces() 
	{
		return mapInterfaces;
	}
	
	// функция, читающая характеристики интерфейсов устройства
	public void ReadSNMP() throws IOException 
	{
		mapInterfaces = new TreeMap<Integer, List<InterfaceElement>>(new Comparator<Integer>() 
		{
			@Override
            public int compare(Integer k1, Integer k2) 
			{
				return k1.compareTo(k2);
			}
		});
		
		Target t = getTarget(host);
		String r = send(t, sOid, PDU.GET);
		
		// получение количества интерфейсов
		countInterfaces = Integer.parseInt(r.split("=")[1].trim());
		
		int idx;
		String nOid = sOid;
		
		// получение списка индексов интерфейсов
		for (int i = 1; i <= countInterfaces; i++) 
		{
			r = send(t, nOid, PDU.GETNEXT);
			
			nOid = r.split("=")[0].trim();
			idx = Integer.parseInt(r.split("=")[1].trim());
			
			interfaceInfo = new ArrayList<InterfaceElement>(CreateTable());
			mapInterfaces.put(idx, interfaceInfo);
		}
		
		nOid = ".1.3.6.1.2.1.2.2.1.";
		
		// чтение характеристик конкретного интерфейса
		List<InterfaceElement> buffLIst = null;
		for (Map.Entry<Integer, List<InterfaceElement>> entry : mapInterfaces.entrySet()) 
		{
			buffLIst = new ArrayList<InterfaceElement>(entry.getValue());
			for (InterfaceElement interfaceElement : buffLIst) 
			{
				r = send(t, nOid+interfaceElement.getKey()+"."+entry.getKey(), PDU.GET);
				interfaceElement.setValue(r.split("=")[1].trim());
			}	
	    }
	}
	
	// функция на запрос характеристики
	private String send(Target target, String oid, int pduType) throws IOException 
	{
		
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID(oid)));
		pdu.setType(pduType);
		ResponseEvent event = snmp.send(pdu, target, null);
		if (event != null) 
		{
			return event.getResponse().get(0).toString();
		}
		else 
		{
			return "Timeout exceeded";
		}
	}
	
	// функция получение адресата
	private Target getTarget(String address) 
	{
		Address targetAddress = GenericAddress.parse(address);
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(SNMP_COMMUNITY));
		target.setAddress(targetAddress);
		target.setRetries(SNMP_RETRIES);
		target.setTimeout(SNMP_TIMEOUT);
		target.setVersion(SnmpConstants.version1);
		return target;
	}
	
	// запуск слушателя SNMP
	public void start() throws IOException 
	{
		transport = new DefaultUdpTransportMapping();
		snmp = new Snmp(transport);
		transport.listen();
	}
	
	// останов слушателя SNMP
	private void stop() throws IOException 
	{
		try 
		{
			if (transport != null) 
			{
				transport.close();
				transport = null;
			}
		} 
		finally 
		{
			if (snmp != null) 
			{
				snmp.close();
				snmp = null;
			}
		}
	}

	// инициализация списка характеристик
	private List<InterfaceElement> CreateTable()
	{
		List<InterfaceElement> listElements = new ArrayList<InterfaceElement>();
		listElements.add(new InterfaceElement("2",  "ifDescr"));
		listElements.add(new InterfaceElement("3",  "ifType"));
		listElements.add(new InterfaceElement("4",  "ifMtu"));
		listElements.add(new InterfaceElement("5",  "ifSpeed"));
		listElements.add(new InterfaceElement("6",  "ifPhysAddress"));
		listElements.add(new InterfaceElement("7",  "ifAdminStatus"));
		listElements.add(new InterfaceElement("8",  "ifOperStatus"));
		listElements.add(new InterfaceElement("9",  "ifLastChange"));
		listElements.add(new InterfaceElement("10", "ifInOctets"));
		listElements.add(new InterfaceElement("11", "ifInUcastPkts"));
		listElements.add(new InterfaceElement("13", "ifInDiscards"));
		listElements.add(new InterfaceElement("14", "ifInErrors"));
		listElements.add(new InterfaceElement("15", "ifInUnknownProtos"));
		listElements.add(new InterfaceElement("16", "ifOutOctets"));
		listElements.add(new InterfaceElement("17", "ifOutUcastPkts"));
		listElements.add(new InterfaceElement("19", "ifOutDiscards"));
		listElements.add(new InterfaceElement("20", "ifOutErrors"));
		
		return listElements;
	}
	
	@Override
	public void close() throws Exception 
	{
		stop();
	}
}
