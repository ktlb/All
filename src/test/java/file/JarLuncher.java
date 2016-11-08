package file;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * one jar -- spirit boot
 * @author  gongxu
 * @version  [版本号, 2016年1月8日]
 */
public class JarLuncher
{
    //协议
    public static final String PROTOCOL = "houtuiwoyaokaiszhuangbile:";
    
    public static void main(String[] args)
        throws Exception
    {
        // 嗯哼 
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        //可以兼容的.. 但是不搞了. 暂时不考虑war
        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory()
        {
            @Override
            public URLStreamHandler createURLStreamHandler(String protocol)
            {
                if (PROTOCOL.startsWith(protocol))
                {
                    return new URLStreamHandler()
                    {
                        protected URLConnection openConnection(URL u)
                            throws IOException
                        {
                            return new URLConnection(u)
                            {
                                public void connect()
                                    throws IOException
                                {
                                }
                                
                                public InputStream getInputStream()
                                    throws IOException
                                {
                                    String file = URLDecoder.decode(this.url.getFile(), "UTF-8");
                                    InputStream result = classLoader.getResourceAsStream(file);
                                    if (result == null)
                                    {
                                        throw new MalformedURLException("Could not open InputStream for URL '"
                                            + this.url + "'");
                                    }
                                    return result;
                                }
                            };
                        }
                        
                        protected void parseURL(URL url, String spec, int start, int limit)
                        {
                            String file;
                            if (spec.startsWith(PROTOCOL))
                            {
                                file = spec.substring(PROTOCOL.length());
                            }
                            else
                            {
                                if (url.getFile().equals("./"))
                                {
                                    file = spec;
                                }
                                else
                                {
                                    if (url.getFile().endsWith("/"))
                                    {
                                        file = url.getFile() + spec;
                                    }
                                    else
                                    {
                                        file = spec;
                                    }
                                }
                            }
                            setURL(url, PROTOCOL, "", -1, null, null, file, null, null);
                        }
                    };
                }
                return null;
            }
        });
        
        //恩. 获取info
        if (!JarLuncher.class.getProtectionDomain().getCodeSource().getLocation().toString().endsWith("jar"))
        {
            System.err.println("装逼模式开启失败,进入乞讨模式");
            System.exit(0);
        }
        
        JarFile jarFile =
            new JarFile(URLDecoder.decode(JarLuncher.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getFile()
                .toString(), "utf-8"));
        Set<Entry<Object, Object>> ens = jarFile.getManifest().getMainAttributes().entrySet();
        String mainClass = "";
        List<URL> cps = new ArrayList<URL>();
        cps.add(JarLuncher.class.getProtectionDomain().getCodeSource().getLocation());
        for (Entry<Object, Object> entry : ens)
        {
            if ("Class-Path".equals(entry.getKey().toString()))
            {
                String[] libs = entry.getValue().toString().split(" ");
                for (String string : libs)
                {
                    if (string.endsWith(".jar"))
                    {
                        cps.add(new URL(PROTOCOL + string));
                    }
                }
            }
            else if ("busMain".equals(entry.getKey().toString()))
            {
                mainClass = entry.getValue().toString();
            }
        }
        jarFile.close();
        ClassLoader jceClassLoader = new URLClassLoader(cps.toArray(new URL[0]), null)
        {
            @Override
            public Class<?> loadClass(String name)
                throws ClassNotFoundException
            {
                return super.loadClass(name);
            }
            
            @Override
            protected Class<?> findClass(String name)
                throws ClassNotFoundException
            {
                return super.findClass(name);
            }
        };
        try
        {
            Class.forName("org.apache.catalina.webresources.TomcatURLStreamHandlerFactory", true, jceClassLoader)
                .getMethod("disable")
                .invoke(Class.forName("org.apache.catalina.webresources.TomcatURLStreamHandlerFactory",
                    true,
                    jceClassLoader));
        }
        catch (Exception e)
        {
            
        }
        
        Class<?> c = Class.forName(mainClass, true, jceClassLoader);
        
        Method main = c.getMethod("main", new Class[]{args.getClass()});
        c.getMethod("setLOADER", new Class[]{ClassLoader.class, String.class})
            .invoke(null,
                jceClassLoader,
                URLDecoder.decode(JarLuncher.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getFile()
                    .toString(), "UTF-8"));
        main.invoke(null, new Object[]{args});
    }
}
