package me.gacl.snmp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

public class Snmp4jFirstDemo {

	private Snmp snmp = null;
	private int version ;

	public Snmp4jFirstDemo(int version) {
		try {
			this.version = version;
			TransportMapping transport = new DefaultUdpTransportMapping();
			snmp = new Snmp(transport);
			if (version == SnmpConstants.version3) {
				// 设置安全模式
				USM usm = new USM(SecurityProtocols.getInstance(),new OctetString(MPv3.createLocalEngineID()), 0);
				SecurityModels.getInstance().addSecurityModel(usm);
			}
			// 开始监听消息
			transport.listen();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		ArrayList<ApTerminalMac> hmapTerminalMacList = hanmingWalk();
		System.out.println("print result:");
		for(int i =0;i< hmapTerminalMacList.size();i++){
			ApTerminalMac apTerminalMac = new ApTerminalMac();
			apTerminalMac = hmapTerminalMacList.get(i);
			String apmac = apTerminalMac.getApmac();

			ArrayList<String> termimalMacList = apTerminalMac.getTerminalmac();
			System.out.println("ap mac :" + apmac +" 有  "+ termimalMacList.size()+" 在线终端:");
			for(String termimal: termimalMacList){
				System.out.println("终端 mac:" + termimal);
			}
		}

		//				ArrayList<ApTerminalMac> h3capTerminalMacList = h3cWalk();
		//				System.out.println("print result:");
		//				for(int i =0;i< h3capTerminalMacList.size();i++){
		//					ApTerminalMac apTerminalMac = new ApTerminalMac();
		//					apTerminalMac = h3capTerminalMacList.get(i);
		//					String apmac = apTerminalMac.getApmac();
		//		
		//					ArrayList<String> termimalMacList = apTerminalMac.getTerminalmac();
		//					System.out.println("ap mac :" + apmac +" 有  "+ termimalMacList.size()+" 在线终端:");
		//					for(String termimal: termimalMacList){
		//						System.out.println("终端 mac:" + termimal);
		//					}
		//		
		//				}

		//						ArrayList<ApTerminalMac> ruijieapTerminalMacList = ruijieWalk();
		//						System.out.println("print result:");
		//						for(int i =0;i< ruijieapTerminalMacList.size();i++){
		//							ApTerminalMac apTerminalMac = new ApTerminalMac();
		//							apTerminalMac = ruijieapTerminalMacList.get(i);
		//							String apmac = apTerminalMac.getApmac();
		//							
		//							ArrayList<String> termimalMacList = apTerminalMac.getTerminalmac();
		//							System.out.println("ap mac :" + apmac +" 有 "+ termimalMacList.size()+" 在线终端:");
		//							for(String termimal: termimalMacList){
		//								System.out.println("终端 mac:" + termimal);
		//							}
		//						}
	}
	/**
	 * 汉明 根据snmp协议,通过ac来获取在线终端  
	 */
	public static ArrayList<ApTerminalMac> hanmingWalk() {
		System.out.println("hanming  start !");

		try {
			Snmp snmp = new Snmp(new DefaultUdpTransportMapping()); // 构造一个UDP
			snmp.listen(); // 开始监听snmp消息

			CommunityTarget target = new CommunityTarget();
			target.setCommunity(new OctetString("public"));// snmpv2的团体名
			target.setVersion(SnmpConstants.version2c); // snmp版本
			target.setAddress(new UdpAddress("192.168.99.234/161"));
			target.setTimeout(60000); // 时延
			target.setRetries(1); // 重传

			TableUtils utils = new TableUtils(snmp, new DefaultPDUFactory(PDU.GETBULK));// GETNEXT or GETBULK
			utils.setMaxNumRowsPerPDU(10);  
			//终端 mac oid
			OID[] columnOids1 = new OID[] {new OID("1.3.6.1.4.1.37014.8000.2.4.1.1.1")};
			long l1prex = System.currentTimeMillis();
			System.out.println("----------l1-prex:" + l1prex);
			//终端 mac list
			List<TableEvent> l1 = utils.getTable(target, columnOids1,null,null);
			long l1after = System.currentTimeMillis();
			System.out.println("----------l1-after:" + l1after);

			System.out.println("l1 cost:" + (l1after - l1prex));

			//终端 ap oid
			OID[] columnOids2 = new OID[] {new OID("1.3.6.1.4.1.37014.8000.2.4.1.1.10")};
			long l2prex = System.currentTimeMillis();
			System.out.println("----------l2-prex:" + l2prex);
			//apmac list
			List<TableEvent> l2 = utils.getTable(target, columnOids2,null,null);
			long l2after = System.currentTimeMillis();
			System.out.println("----------l2-after:" + l2after);

			System.out.println("l2 cost:" + (l2after - l2prex));

			if(l1.size() <= 0 && l2.size()<= 0){
				System.out.println("没有获取到数据!");
				return null;
			}

			String oid ="",va = "";
			//最后的结果
			ArrayList<ApTerminalMac> apTerminalMacList = new ArrayList<ApTerminalMac>();
			//终端mac HashMap
			HashMap terminalMacmap = new HashMap();
			//apmac HashMap
			HashMap apMacmap = new HashMap();

			//唯一性的key List
			ArrayList<String> apMacUniqueList = new ArrayList<String>();

			//终端 mac
			for(int i =0 ;i< l1.size(); i++){
				oid = l1.get(i).getColumns()[0].getOid().toString();
				va = l1.get(i).getColumns()[0].getVariable().toString();
				String oidnew = oid.substring(("1.3.6.1.4.1.37014.8000.2.4.1.1.1.").length(),oid.length());
				//System.out.println("oidnew: " + oidnew +","+ "va: " + va);
				terminalMacmap.put(oidnew, va);
			}
			System.out.println("终端 size: "+ terminalMacmap.size());

			//ap mac
			for(int i =0 ;i< l2.size(); i++){
				oid = l2.get(i).getColumns()[0].getOid().toString();
				va = l2.get(i).getColumns()[0].getVariable().toString();
				String oidnew = oid.substring(("1.3.6.1.4.1.37014.8000.2.4.1.1.10.").length(),oid.length());
				//System.out.println("oidnew: " + oidnew +","+ "va: " + va);
				apMacmap.put(oidnew, va);
				//new ap mac
				if(!apMacUniqueList.contains(va)){
					//System.out.println("new va:" + va);
					apMacUniqueList.add(va);
				}
			}
			System.out.println("ap size(仅指有终端接入的ap): "+ apMacUniqueList.size());
			System.out.println("---------------------------");

			//result
			String apmac = "";
			for(int i=0; i<apMacUniqueList.size();i++){
				apmac = apMacUniqueList.get(i);
				//System.out.println("当前的apmac：" + apmac);

				ApTerminalMac apTerminalMac = new ApTerminalMac();
				apTerminalMac.setApmac(apmac);
				ArrayList<String> terminalmacTemp = new ArrayList<String>();

				//遍历apMacmap获取终端的oid
				Iterator iter_apMacmap = apMacmap.entrySet().iterator();
				while(iter_apMacmap.hasNext()) {
					Map.Entry entry_apMacmap = (Map.Entry)iter_apMacmap.next();
					if(StringUtils.equalsIgnoreCase(entry_apMacmap.getValue().toString(),apmac)){
						//根据当前的apmac来获取终端mac
						if(terminalMacmap.containsKey(entry_apMacmap.getKey())){
							//System.out.println("当前的apmac对应的oid: "+ entry_apMacmap.getKey().toString() + ",  对应的终端mac:" + terminalMacmap.get(entry_apMacmap.getKey()));
							terminalmacTemp.add(terminalMacmap.get(entry_apMacmap.getKey()).toString());
						}
					}
				}
				apTerminalMac.setTerminalmac(terminalmacTemp);
				//System.out.println("apMac: "+ apTerminalMac.getApmac()+ ", apMac有终端数："+apTerminalMac.getTerminalmac().size());
				//递归添加查询结果集
				apTerminalMacList.add(apTerminalMac);
			}
			System.out.println("hanming  end !");
			System.out.println("---------------------------");

			return apTerminalMacList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 华三 根据snmp协议,通过ac来获取在线终端  
	 */
	public static ArrayList<ApTerminalMac> h3cWalk() {
		System.out.println("h3c  start !");
		try {
			Snmp snmp = new Snmp(new DefaultUdpTransportMapping()); // 构造一个UDP
			snmp.listen(); // 开始监听snmp消息

			CommunityTarget target = new CommunityTarget();
			target.setCommunity(new OctetString("h3c"));// snmpv2的团体名
			target.setVersion(SnmpConstants.version2c); // snmp版本
			target.setAddress(new UdpAddress("192.168.110.2/161"));
			target.setTimeout(60000); // 时延
			target.setRetries(1); // 重传

			TableUtils utils = new TableUtils(snmp, new DefaultPDUFactory(PDU.GETBULK));// GETNEXT or GETBULK
			utils.setMaxNumRowsPerPDU(10);  
			//ap mac oid
			OID[] columnOids1 = new OID[] {new OID("1.3.6.1.4.1.2011.10.2.75.2.1.1.1.3")};
			//ap mac list
			List<TableEvent> l1 = utils.getTable(target, columnOids1,null,null);

			//终端 mac oid
			OID[] columnOids2 = new OID[] {new OID("1.3.6.1.4.1.2011.10.2.75.3.1.2.1.1")};
			//终端 mac list
			List<TableEvent> l2 = utils.getTable(target, columnOids2,null,null);

			if(l1.size() <= 0 && l2.size()<= 0){
				System.out.println("没有获取到数据!");
				return null;
			}

			String oid ="",va = "";
			//最后的结果
			ArrayList<ApTerminalMac> apTerminalMacList = new ArrayList<ApTerminalMac>();
			//终端mac HashMap
			HashMap terminalMacmap = new HashMap();
			//apmac HashMap
			HashMap apMacmap = new HashMap();

			//get apMac
			for(int i =0 ;i< l1.size(); i++){
				oid = l1.get(i).getColumns()[0].getOid().toString();
				va = l1.get(i).getColumns()[0].getVariable().toString();
				//System.out.println("oid: " + oid +","+ "va: " + va);
				//process oid  ASCII码转换为10进制
				String oidnew = convertAPIndex(oid);
				//在线apMac:全部ap mac 和对应的序列号
				apMacmap.put(oidnew, va);
				//System.out.println("oidnew: " + oidnew +","+ "va: " + va);
			}
			System.out.println("在线ap size:" + apMacmap.size());

			//get 终端 Mac
			for(int i =0 ;i< l2.size(); i++){
				oid = l2.get(i).getColumns()[0].getOid().toString();
				va = l2.get(i).getColumns()[0].getVariable().toString();
				//System.out.println("oid: " + oid +","+ "va: " + va);
				String mac = convertOidToMac(oid);
				//终端apMac  获取所有的终端序列号和ap序列号
				terminalMacmap.put(mac, va);
				//	System.out.println("mac: " + mac +","+ "va: " + va);
			}
			System.out.println("终端 size:" + terminalMacmap.size());

			//result
			String apIndex = "";
			String apmac ="";
			//遍历在线apMac
			Iterator iter_apMac = apMacmap.entrySet().iterator();
			while(iter_apMac.hasNext()) {
				Map.Entry entry_apMac= (Map.Entry)iter_apMac.next();
				apIndex =  entry_apMac.getKey().toString();
				apmac =  entry_apMac.getValue().toString();
				//System.out.println("当前的apmac：" + apmac + ",当前的apIndex：" + apIndex);
				ApTerminalMac apTerminalMac = new ApTerminalMac();
				apTerminalMac.setApmac(apmac);
				ArrayList<String> terminalmacTemp = new ArrayList<String>();

				//遍历终端apMac
				Iterator iter_terminalMacmap = terminalMacmap.entrySet().iterator();
				while(iter_terminalMacmap.hasNext()) {
					Map.Entry entry_terminalMacmap = (Map.Entry)iter_terminalMacmap.next();
					//System.out.println("entry_terminalMacmap.getValue().toString():" + entry_terminalMacmap.getValue().toString() + ",  apIndex:"+ apIndex);
					if(StringUtils.equalsIgnoreCase(entry_terminalMacmap.getValue().toString(),apIndex)){
						terminalmacTemp.add(entry_terminalMacmap.getKey().toString());
						//System.out.println("终端 mac:" + entry_terminalMacmap.getKey().toString());
					}
				}
				apTerminalMac.setTerminalmac(terminalmacTemp);
				//System.out.println("apMac: "+ apTerminalMac.getApmac()+ ", apMac有终端数："+apTerminalMac.getTerminalmac().size());
				//递归添加查询结果集
				apTerminalMacList.add(apTerminalMac);
			}
			System.out.println("h3c  end !");
			System.out.println("-----------------------");
			return apTerminalMacList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 锐捷 根据snmp协议,通过ac来获取在线终端  
	 */
	public static ArrayList<ApTerminalMac> ruijieWalk() {
		System.out.println("ruijie  start !");
		try {
			Snmp snmp = new Snmp(new DefaultUdpTransportMapping()); // 构造一个UDP
			snmp.listen(); // 开始监听snmp消息

			CommunityTarget target = new CommunityTarget();
			target.setCommunity(new OctetString("ruijie"));// snmpv2的团体名
			target.setVersion(SnmpConstants.version2c); // snmp版本
			target.setAddress(new UdpAddress("10.0.0.14/161"));
			target.setTimeout(60000); // 时延
			target.setRetries(1); // 重传

			TableUtils utils = new TableUtils(snmp, new DefaultPDUFactory(PDU.GETBULK));// GETNEXT or GETBULK
			utils.setMaxNumRowsPerPDU(10);  
			//终端 mac oid
			OID[] columnOids2 = new OID[] {new OID("1.3.6.1.4.1.4881.1.1.10.2.56.5.1.1.1.1")};
			//终端 mac list
			List<TableEvent> l2 = utils.getTable(target, columnOids2,null,null);

			//终端 mac & ap mac oid
			OID[] columnOids3 = new OID[] {new OID("1.3.6.1.4.1.4881.1.1.10.2.56.5.1.1.1.2")};
			//终端 mac list
			List<TableEvent> l3 = utils.getTable(target, columnOids3,null,null);
			if(l2.size()<= 0 && l3.size()<= 0){
				System.out.println("没有获取到数据!");
				return null;
			}
			String oid ="",va = "";
			//最后的结果
			ArrayList<ApTerminalMac> apTerminalMacList = new ArrayList<ApTerminalMac>();

			ArrayList<String> apMacHaveterminalList = new ArrayList<String>();
			//终端mac HashMap
			HashMap terminalMacmap = new HashMap();
			//终端 mac & ap mac HashMap
			HashMap apterminalMacMap = new HashMap();

			//get terminalMac
			for(int i =0 ;i< l2.size(); i++){
				oid = l2.get(i).getColumns()[0].getOid().toString();
				va = l2.get(i).getColumns()[0].getVariable().toString();
				String oidnew = oid.substring(("1.3.6.1.4.1.4881.1.1.10.2.56.5.1.1.1.1.").length(),oid.length());
				//System.out.println("oidnew: " + oidnew +","+ "va: " + va);
				//在线apMac
				terminalMacmap.put(oidnew, va);
			}
			System.out.println("终端 size:" + terminalMacmap.size());

			//get 终端 mac & ap mac
			for(int i =0 ;i< l3.size(); i++){
				oid = l3.get(i).getColumns()[0].getOid().toString();
				va = l3.get(i).getColumns()[0].getVariable().toString();
				String oidnew = oid.substring(("1.3.6.1.4.1.4881.1.1.10.2.56.5.1.1.1.2.").length(),oid.length());
				//System.out.println("oidnew: " + oidnew +","+ "va: " + va);
				//在线apMac
				apterminalMacMap.put(oidnew, va);
				if(!apMacHaveterminalList.contains(va)){
					apMacHaveterminalList.add(va);
				}
			}
			System.out.println("有无线接入的ap size:" + apMacHaveterminalList.size());
			System.out.println("---------------------------");

			//result
			String apmac ="";
			for(int i =0 ;i<apMacHaveterminalList.size();i++){
				apmac = apMacHaveterminalList.get(i);
				//System.out.println("当前的 apmac:" + apmac );

				ApTerminalMac apTerminalMac = new ApTerminalMac();
				apTerminalMac.setApmac(apmac);
				ArrayList<String> terminalmacTemp = new ArrayList<String>();

				//遍历apMacmap获取终端的oid
				Iterator iter_apterminalMacMap = apterminalMacMap.entrySet().iterator();
				while(iter_apterminalMacMap.hasNext()) {
					Map.Entry entry_apterminalMacMap = (Map.Entry)iter_apterminalMacMap.next();
					if(StringUtils.equalsIgnoreCase(entry_apterminalMacMap.getValue().toString(),apmac)){
						//根据当前的apmac来获取终端mac
						if(terminalMacmap.containsKey(entry_apterminalMacMap.getKey())){
							//System.out.println("当前的apmac对应的oid: "+ entry_apMacmap.getKey().toString() + ",  对应的终端mac:" + terminalMacmap.get(entry_apMacmap.getKey()));
							terminalmacTemp.add(terminalMacmap.get(entry_apterminalMacMap.getKey()).toString());
						}
					}
				}
				apTerminalMac.setTerminalmac(terminalmacTemp);
				//System.out.println("apMac: "+ apTerminalMac.getApmac()+ ", apMac有终端数："+apTerminalMac.getTerminalmac().size());
				//递归添加查询结果集
				apTerminalMacList.add(apTerminalMac);
			}
			System.out.println("ruijie  end !");
			System.out.println("-----------------------------");
			return apTerminalMacList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * 根据oid获取ap的序列号
	 */
	public static String convertAPIndex(String oid){
		oid = oid.substring(("1.3.6.1.4.1.2011.10.2.75.2.1.1.1.3.20").length() + 1);
		String oidFinal = "";
		String[] strArray = null;  
		strArray = oid.split("\\."); //拆分字符为"." ,然后把结果交给数组strArray
		String temp ="";char a ;int index =0;
		for(int i=0;i<strArray.length;i++){
			temp = strArray[i];
			index = Integer.parseInt(strArray[i]);
			a = (char) index;
			oidFinal += a ;
		}
		return oidFinal;
	}

	/**
	 * 根据oid获取ap的mac地址
	 */
	public static String convertOidToMac(String oid){
		oid = oid.substring(("1.3.6.1.4.1.2011.10.2.75.3.1.2.1.1").length() + 1);
		String mac ="";
		String[] strArray = null;  
		strArray = oid.split("\\."); //拆分字符为"." ,然后把结果交给数组strArray
		String temp ="";String a ;int index =0;
		for(int i=0;i<strArray.length;i++){
			temp = strArray[i];
			index = Integer.parseInt(strArray[i]);
			a = Integer.toHexString(index);
			mac += a + ":";
		}
		//去除最后一个":"
		mac = mac.substring(0,mac.length()-1);

		//plus 0
		if(mac.length() < ("18:dc:56:85:53:60").length()){
			String mac_process ="";
			String[] strA = mac.split(":");
			for(int t=0;t< strA.length; t++){
				if(strA[t].length()<2){
					strA[t] = "0" +strA[t];
					mac_process = mac_process + strA[t] + ":";
				}else{
					mac_process = mac_process + strA[t] + ":";
				}

			}
			//delete last ":"
			mac_process = mac_process.substring(0,mac_process.length()-1);
			mac = mac_process;
		}
		return mac;
	}

}
