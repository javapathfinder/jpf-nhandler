/* 
 * Copyright (C) 2021  Franck van Breugel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You can find a copy of the GNU General Public License at
 * <http://www.gnu.org/licenses/>.
 */

package jpf;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.gradle.api.GradleException;

/**
 * Utility class to find the full path of the jar file of jpf-core.
 * It also issues a warning if the key "extensions" in the site.properties
 * file has more than one value.
 * 
 * @author Franck van Breugel
 */
public class Jar {
	/**
	 * Returns the full path of the jar file of jpf-core.
	 * 
	 * @return the full path of the jar file of jpf-core
	 * @throws GradleException if something goes wrong
	 */
	public static String getFullPath() {
        File devHome = new File(System.getProperty("user.dir")).getParentFile();
        File dotJpf = new File(devHome, ".jpf");

        if (!dotJpf.exists() || !dotJpf.isDirectory()) {
            File home = new File(System.getProperty("user.home"));
            dotJpf = new File(home, ".jpf");
        }

        if (!dotJpf.exists()) {
            throw new GradleException(".jpf directory cannot be found in " + dotJpf.getAbsolutePath());
        }
        if (!dotJpf.isDirectory()) {
            throw new GradleException(".jpf is not a directory");
        }

		// find site.properties file	
		File siteProperties = new File(dotJpf, "site.properties");
		if (!siteProperties.exists()) {
			throw new GradleException("site.properties file cannot be found");
		}
		if (!siteProperties.isFile()) {
			throw new GradleException("site.properties is not a file");
		}

		try {
			// load site.properties file
			PropertiesConfiguration configuration = new PropertiesConfiguration();
			configuration.load(siteProperties);
                        // add system properties
			configuration.append(new SystemConfiguration());

			// find jpf-core directory
			if (!configuration.containsKey("jpf-core")) {
				throw new GradleException("site.properties file does not contain jpf-core");
			}
			String path = configuration.getString("jpf-core");
			File jpfCore = new File(path);			
			if (!jpfCore.exists()) {
				throw new GradleException("jpf-core directory cannot be found");
			}
			if (!jpfCore.isDirectory()) {
				throw new GradleException("jpf-core is not a directory");
			}

			// check extensions
			if (configuration.containsKey("extensions")) {
				String[] value = configuration.getStringArray("extensions");
				if (value.length > 1) {
					System.out.println("WARNING: in the site.properties file, the property \"extensions\" has");
					System.out.println("more than one value.  However, it suffices to use");
					System.out.println("extensions = ${jpf-core}");
					System.out.println("(see https://github.com/javapathfinder/jpf-core/wiki/Creating-site-properties-file).");
				} else if (value.length == 1) {
					if (!value[0].equals(path)) {
						System.out.println("WARNING: in the site.properties file, the property \"extensions\" does");
						System.out.println("not have the value ${jpf-core}");
						System.out.println("(see https://github.com/javapathfinder/jpf-core/wiki/Creating-site-properties-file).");
					}
				}
			}

			// find build directory
			File build = new File(jpfCore, "build");
			if (!build.exists()) {
				throw new GradleException("build directory cannot be found");
			}
			if (!build.isDirectory()) {
				throw new GradleException("build is not a directory");
			}

			// find jpf.jar		
			File jpfJar = new File(build, "jpf.jar");
			if (!jpfJar.exists()) {
				throw new GradleException("jpf.jar file cannot be found");
			}
			if (!jpfJar.isFile()) {
				throw new GradleException("jpf.jar is not a file");
			}

			return jpfJar.toString();
		} catch (ConfigurationException e) {
			throw new GradleException("site.properties file cannot be read"); 
		}
	}
}
