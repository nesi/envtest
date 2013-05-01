package nz.org.nesi.envtester;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.lang.StringUtils;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;

public class EnvDetails {
	
	public static void main(String[] args) {
		
		EnvDetails d = new EnvDetails();
		
		System.out.println(d.createReport());
	}
	
	public static Enumeration<NetworkInterface> lookupNetworkInterfaces() {
		
		Enumeration<NetworkInterface> e = null;
		try {
			e = NetworkInterface.getNetworkInterfaces();
			return e;
		} catch (SocketException e1) {
			return null;
		}
	}
	
	private List<NetworkInterface> interfaces;
	
	public EnvDetails() {
		try {
			interfaces = EnumerationUtils.toList(lookupNetworkInterfaces());
		} catch (Exception e) {
			e.printStackTrace();
			interfaces = Lists.newArrayList();
		}
		
	}
	
	public List<NetworkInterface> getNetworkInterfaces() {
		return interfaces;
	}
	
	private Map<String, String> createProperties() {
		
		Map<String, String> map = Maps.newLinkedHashMap();

		map.put("user.name", System.getProperty("user.name"));
		map.put("user.home", System.getProperty("user.home"));
		
		for ( NetworkInterface nwi : getNetworkInterfaces() ) {
			String name = nwi.getDisplayName();
			try {
				byte[] mac = nwi.getHardwareAddress();
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < mac.length; i++) {
					sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));		
				}
				map.put("interface."+name, sb.toString());
			} catch (Exception e) {
				map.put("interface."+name, "n/a");
			}
			List<InetAddress> inetAddr = EnumerationUtils.toList(nwi.getInetAddresses());
			for ( InetAddress a : inetAddr ) {
				String ina = a.getHostAddress();
				String hn = a.getHostName();
				if ( StringUtils.equals(ina, hn)) {
					map.put("interface."+name+".inetAddr", ina);
				} else {
					map.put("interface."+name+".inetAddr", ina+" ("+hn+")");
				}
			}
			try {
				Integer mtu = nwi.getMTU();
				map.put("interface."+name+".mtu", mtu.toString());
			} catch (Exception e) {
				map.put("interface."+name+".mtu", "n/a");
			}
		}
		
		List<String> props = Lists.newArrayList();
		props.add("os.name");
		props.add("os.arch");
		props.add("os.version");
		props.add("java.version");
		props.add("java.home");

		for ( String p : props ) {
			map.put(p, System.getProperty(p));
		}
		
		return map;
	}
	
	public String createReport() {
		
		StringBuffer b = new StringBuffer();
		Map<String, String> p = createProperties();
		for ( String key : p.keySet() ) {
			b.append(key+" = "+p.get(key)+"\n");
		}
		return b.toString();
	}
	
	public String toString() {
		return createReport();
	}

}
