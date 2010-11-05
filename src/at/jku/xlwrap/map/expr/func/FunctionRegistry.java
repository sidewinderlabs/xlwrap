/**
 * Copyright 2009 Andreas Langegger, andreas@langegger.at, Austria
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.jku.xlwrap.map.expr.func;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dorgon
 *
 */
public class FunctionRegistry {
	private static final Logger log = LoggerFactory.getLogger(FunctionRegistry.class);
	
	public static final String FUNCTION_PREFIX = "E_Func";
	
	private static Map<String, Class<? extends XLExprFunction>> functions = new Hashtable<String, Class<? extends XLExprFunction>>();
	
	static {
		// register standard function package with all sub packages
		registerPackage("at.jku.xlwrap.map.expr.func");
	}
	
	/**
	 * @param image
	 * @return
	 */
	public static boolean hasFunction(String image) {
		return functions.containsKey(image);
	}

	/**
	 * Register a function package (will search for all classes in sub packages with FUNCTION_PREFIX as prefix)
	 * 
	 * @param pkg name
	 */
	public static void registerPackage(String pkg) {
		try {
			log.info("Registering function package '" + pkg + "' ...");
			Enumeration<URL> list = FunctionRegistry.class.getClassLoader().getResources(pkg.replace('.', '/'));
			while (list.hasMoreElements()) {
				URL url = list.nextElement();
				if (url.getProtocol().equals("jar"))
					registerJarPackage(url, pkg);
				else if (url.getProtocol().equals("file"))
					registerFilePackage(new File(url.getFile()), pkg);
				else if (url.getProtocol().equals("http"))
					log.error("Cannot automatically register function package '" + pkg + "' via remote code base from " + url);
				else
					log.warn("Failed to automatically register function package '" + pkg + "' from " + url + ". Please check your class path.");
			}
		} catch (IOException e) {
			log.error("Failed to register function package '" + pkg + "'.", e);
		}
	}

    /**
     * Recursively register sub packages from file system
     *
     * @param directory   base directory
     * @param packageName package name for classes found inside the base directory
     */
    @SuppressWarnings("unchecked")
	private static void registerFilePackage(File directory, String packageName) {
        if (!directory.exists())
        	return;
        
        File[] files = directory.listFiles();
        for (File file : files) {
        	String name = file.getName();
            if (file.isDirectory()) {
                if (!name.contains("."))
                	registerFilePackage(file, packageName + "." + name);
            } else if (name.endsWith(".class") && name.startsWith(FunctionRegistry.FUNCTION_PREFIX) && !name.contains("$")) {
            	String cl = null;
                try {
                	cl = packageName + '.' + name.substring(0, name.length() - 6);
					register((Class<? extends XLExprFunction>) Class.forName(cl));
				} catch (ClassNotFoundException e) {
					log.error("Failed to register function package '" + packageName + "'.", e);
				} catch (ClassCastException e) {
					log.error("Failed to register function implementation '" + cl + "' (it does not extend " + XLExprFunction.class.getName() + "').", e);
				}
            }
        }
    }
    
    /**
     * Recursively register sub packages from JAR
     *
     * @param url
     * @param packageName package name for classes found inside the base directory
     * @throws IOException 
     */
    @SuppressWarnings("unchecked")
	private static void registerJarPackage(URL url, String packageName) throws IOException {
    	String file = url.getFile();
    	file = file.substring(5, file.indexOf("!"));
    	JarFile jar = new JarFile(file);    	

    	String pkg = packageName.replace('.', '/');    	
    	Enumeration<JarEntry> entries = jar.entries();
    	while (entries.hasMoreElements()) {
        	JarEntry entry = entries.nextElement();
        	
        	String fullName = entry.getName();
        	String name = "";
        	String thisPkg = "";
        	Matcher m = Pattern.compile("^(.*)\\/(.*)$").matcher(fullName);
        	if (m.find()) {
        		thisPkg = m.group(1);
        		name = m.group(2);
        	}
        	
        	if (fullName.startsWith(pkg) && name.endsWith(".class") && name.startsWith(FunctionRegistry.FUNCTION_PREFIX) && !name.contains("$")) {
            	String cl = null;
                try {
                	cl = thisPkg.replace('/', '.') + '.' + name.substring(0, name.length() - 6);
					register((Class<? extends XLExprFunction>) Class.forName(cl));
				} catch (ClassNotFoundException e) {
					log.error("Failed to register function package '" + packageName + "'.", e);
				} catch (ClassCastException e) {
					log.error("Failed to register function implementation '" + cl + "' (it does not extend " + XLExprFunction.class.getName() + "').", e);
				}
            }
        }
    }

    /**
     * @param impl
     * @return
     */
    public static XLExprFunction createInstance(Class<? extends XLExprFunction> impl) {
    	return createInstance(getFunctionName(impl));
    }
    
    /**
	 * @param funcName
	 * @return instance of the function
	 * @throws UnsupportedOperationException 
	 */
	public static XLExprFunction createInstance(String funcName) throws UnsupportedOperationException {
		Class<? extends XLExprFunction> impl = functions.get(funcName);
		if (impl == null)
			throw new UnsupportedOperationException("Unknown function: '" + funcName + "'.");
		try {
			return impl.newInstance();
		} catch (InstantiationException e) {
			throw new UnsupportedOperationException("Function '" + funcName + "' found, but instanciating the implementation failed, please check if it has a default constructor.", e);
		} catch (IllegalAccessException e) {
			throw new UnsupportedOperationException("Function '" + funcName + "' found, however, its usage is restricted.", e);
		}
	}
	
	/**
	 * register a function, name must have prefix "XLFunc" + function name, e.g. XLFuncURLENCODE
	 *  
	 * @param impl the implementing class
	 */
	public static void register(Class<? extends XLExprFunction> impl) {
		if (!impl.getSimpleName().startsWith(FUNCTION_PREFIX))
			throw new IllegalArgumentException("Class name of a function implementation must have prefix '" + FUNCTION_PREFIX  +"': " + impl.getSimpleName() + ".");
		String name = getFunctionName(impl);
		functions.put(name, impl);
		log.debug("registered function '" + name + "': " + impl.getCanonicalName());
	}
	
	/** 
	 * returns the name of a function based on its implementing class name
	 * 
	 * @param impl
	 * @return
	 */
	public static String getFunctionName(Class<? extends XLExprFunction> impl) {
		return impl.getSimpleName().substring(6);
	}

}
