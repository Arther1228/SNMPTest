package com.test.snmp;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.snmp4j.log.ConsoleLogFactory;
import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;
import com.test.snmp.SnmpData;
/**
 * @filename SnmpGet.java
 * @author code by jianghuiwen
 * @mail jianghuiwen2012@163.com
 *
 * 下午2:21:53
 */
public class TestSnmpGet {

	public void testGet()
	{
		String ip = "127.0.0.1";
		String community = "public";
		String oidval = "1.3.6.1.2.1.1.6.0";
		SnmpData.snmpGet(ip, community, oidval);
	}
	public void testGetList()
	{
		String ip = "127.0.0.1";
		String community = "public";
		List<String> oidList=new ArrayList<String>();
		oidList.add("1.3.6.1.2.1.1.5.0");
		oidList.add("1.3.6.1.2.1.1.7.0");
		SnmpData.snmpGetList(ip, community, oidList);
	}
	public void testGetAsyList()
	{
		String ip = "127.0.0.1";
		String community = "public";
		List<String> oidList=new ArrayList<String>();
		oidList.add("1.3.6.1.2.1");
		oidList.add("1.3.6.1.2.12");
		SnmpData.snmpAsynGetList(ip, community, oidList);
		System.out.println("i am first!");
	}
	@Test
	public void testWalk()
	{
		String ip = "192.168.99.234";
		String community = "public";
		String targetOid = "1.3.6.1.4.1.37014.8000.2.4.1";
		SnmpData.snmpWalk(ip, community, targetOid);
	}
	public void testAsyWalk()
	{
		String ip = "127.0.0.1";
		String community = "public";
		// 异步采集数据
		SnmpData.snmpAsynWalk(ip, community, "1.3.6.1.2.1.25.4.2.1.2");
	}
	
	public void testSetPDU() throws Exception
	{
		String ip = "127.0.0.1";
		String community = "public";
		SnmpData.setPDU(ip, community, "1.3.6.1.2.1.1.6.0","jianghuiwen");
	}
	public void testVersion()
	{
		//System.out.println(org.snmp4j.version.VersionInfo.getVersion());
	}
}