package geoip;

import helpers.GH;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;


import java.util.Collections;
import java.util.zip.GZIPInputStream;

import logger.LoggerFactory;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.osgi.framework.Bundle;


import com.maxmind.geoip.Country;
import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

public final class GEOIP {


	
	
	
	private final boolean country =  GEOPref.getBoolean(GEOPref.COUNTRY_ONLY);
	private final String gzPath = country ? "db/GeoIP.dat.gz" : "db/GeoLiteCity.dat.gz" ; 
	private final String filePath = country ? "db/GeoIP.dat":"db/GeoLiteCity.dat";
	
	

	private static final Logger logger = LoggerFactory.make(Level.DEBUG);
	
	private final LookupService ls ;
	
	private static class GEOIPHolder {
		private static final GEOIP singleton = new GEOIP();
	}
	
	public static GEOIP get() {
		return GEOIPHolder.singleton;
	}
	
	private GEOIP() {
		LookupService ls = null;
		try {
			if (!getDBFile().isFile() || GEOPref.isVersionChanged()) {
				copyToWorkspace();
			}
			ls = new LookupService(getDBFile(),
					LookupService.GEOIP_MEMORY_CACHE );
		} catch(IOException ioe) {
			logger.warn(ioe, ioe);
		}
		this.ls = ls;
	}
	
	

	
	/**
	 * 
	 * @return true if only country information is available
	 * meaning:
	 * true: getLocation() /may not be called
	 * false .. all may be called..
	 */
	public boolean isCountryOnly() {
		return country;
	}
	
	/**
	 * @param ip - an IP that should be resolved
	 * @return the location to the IP
	 */
	public Location getLocation(String ip) {
		try {
			return ls.getLocation(ip);
		} catch(RuntimeException e) {
			logger.debug(e,e);
		}
		return null;
	}
	
	/**
	 * @param ip - an IP that should be resolved
	 * @return the location to the IP
	 */
	public Location getLocation(InetAddress ip) {
		try {
			return ls.getLocation(ip);
		} catch(RuntimeException e) {
			logger.debug(e+"  ip: "+ip,e);
		}
		return null;
	}
	
	
	public Country getCountry(InetAddress ip) {
		if (country) {
			try {
				return ls.getCountry(ip);
			} catch(RuntimeException e) {
				logger.debug(e,e);
			}
		} else {
			Location loc = getLocation(ip);
			if (loc != null) {
				return new Country(loc.countryCode,loc.countryName);
			}
		}
		return null;
	}
	
	public String getCountryCode(InetAddress ip) {
		if (country) {
			Country c = getCountry(ip);
			return c == null ? null : c.getCode();
		} else {
			Location loc = getLocation(ip);
			return loc == null? null: loc.countryCode;
		}
	}
	

	
	
	private File getDBFile() {
		return new File(new File(Platform.getInstanceLocation().getURL().getFile()),filePath);
	}
	
	private void copyToWorkspace() {
		logger.info(Lang.GEOIPDBUnpacking);
		Bundle bundle = Platform.getBundle(GEOPref.PLUGIN_ID);
		Path path = new Path(gzPath); 
		URL url = FileLocator.find(bundle, path, Collections.EMPTY_MAP);
	
		FileOutputStream fos = null;
		GZIPInputStream gis = null;
		try {
			InputStream is = url.openStream();
			gis = new GZIPInputStream(is);
			fos = new FileOutputStream(getDBFile());
			GH.copy(gis, fos);
			GEOPref.setVersion();
			
		} catch(IOException ioe) {
			logger.warn(ioe, ioe);
		} finally {
			GH.close(fos,gis);
		}
		
	}
	
	
}
