package com.xxicon.code.reactor.core.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class RTSI {
    private static final String FILE_URL_PREFIX = "file:";
    private static final String JAR_URL_PREFIX = "jar:file:";
    private static final String JAR_URL_SEPARATOR = "!/";

    private static String resolveBasePackage(String pkg) {
        return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(pkg));
    }

    public static Set<Class> findClass(String pkg, Class toSubclass) {
        return findClass(pkg, toSubclass, false);
    }

    public static Set<Class> findClass(String pkg, Class toSubclass, boolean searchSubDir) {
        try {
            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
            Set<Class> candidates = new HashSet<Class>();
            String pkgPattern = searchSubDir ? "/**/*.class" : "/*.class";
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    resolveBasePackage(pkg) + pkgPattern;
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    Class c = Class.forName(metadataReader.getClassMetadata().getClassName());
                    if (canUseClassAs(toSubclass, c)) {
                        candidates.add(Class.forName(metadataReader.getClassMetadata().getClassName()));
                    }
                }
            }
            return candidates;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Set<Class> findClass(List<String> pkgList, Class toSubclass) {
        return findClass(pkgList, toSubclass, false);
    }

    public static Set<Class> findClass(List<String> pkgList, Class toSubclass, boolean searchSubDir) {
        Set<Class> clazzSet = new HashSet<Class>();
        for (String pkg : pkgList) {
            Set<Class> current = findClass(pkg, toSubclass, searchSubDir);
            if (null != current) {
                clazzSet.addAll(current);
            }
        }
        return clazzSet;
    }

    public static Resource[] findResources(String pkg) {
        return findResources(pkg, "");
    }

    public static Resource[] findResources(String pkg, boolean findSubDir) {
        return findResources(pkg, "", findSubDir);
    }

    public static Resource[] findResources(String pkg, String fileExt) {
        return findResources(pkg, fileExt, true);
    }

    public static Resource[] findResources(String pkg, String fileExt, boolean findSubDir) {
        try {
            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            String pkgPattern = findSubDir ? "/**/*" : "/*";
            if (fileExt != null && fileExt.trim().length() > 0) {
                pkgPattern += fileExt.trim();
            }
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    resolveBasePackage(pkg) + pkgPattern;
            return resourcePatternResolver.getResources(packageSearchPath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static URL getURL(String classpathPart, String pkgName) {
        String urlStr;
        URL result;
        File classpathFile;
        File file;
        JarFile jarfile;
        Enumeration enm;
        String pkgNameTmp;

        result = null;
        urlStr = null;

        try {
            classpathFile = new File(classpathPart);
            // directory or jar?
            if (classpathFile.isDirectory()) {
                // does the package exist in this directory?
                file = new File(classpathPart + pkgName);
                if (file.exists()) {
                    urlStr = FILE_URL_PREFIX + classpathPart + pkgName;
                }
            } else {
                // is package actually included in jar?
                jarfile = new JarFile(classpathPart);
                enm = jarfile.entries();
                pkgNameTmp = pkgName.substring(1); // remove the leading "/"
                while (enm.hasMoreElements()) {
                    if (enm.nextElement().toString().startsWith(pkgNameTmp)) {
                        urlStr = JAR_URL_PREFIX + classpathPart + JAR_URL_SEPARATOR + pkgNameTmp;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // ignore
            e.printStackTrace();
        }

        // try to generate URL from url string
        if (urlStr != null) {
            try {
                result = new URL(urlStr);
            } catch (Exception e) {
                System.err.println("Trying to create URL from '" + urlStr + "' generates this exception:\n" + e);
                result = null;
            }
        }
        return result;
    }

    public static List<URL> getPkgResource(String pkgName) {
        List<URL> value = getPkgResourceCp(pkgName);
        if (value.size() > 0) {
            return value;
        }
        return getPkgResourceCl(pkgName);
    }

    /**
     * 非web环境能加载成功,web环境加载失败
     *
     * @param pkgName
     * @return
     */
    public static List<URL> getPkgResourceCp(String pkgName) {
        List<URL> list = new ArrayList<URL>();
        if (!pkgName.startsWith("/")) {
            pkgName = "/" + pkgName;
        }
        pkgName = pkgName.replace('.', '/');
        StringTokenizer tok = new StringTokenizer(System
                .getProperty("java.class.path"), System
                .getProperty("path.separator"));
        while (tok.hasMoreTokens()) {
            String part = tok.nextToken();
            URL url = getURL(part, pkgName);
            if (null == url) {
                continue;
            }
            list.add(url);
        }
        return list;
    }

    /**
     * web环境加载成功
     * 非web环境,此方法有问题,classLoader.getResources(path)有时无法获得资源
     * 原因未知
     *
     * @param pkgName
     * @return
     */
    public static List<URL> getPkgResourceCl(String pkgName) {
        String path = pkgName.replace('.', '/');
        List<URL> list = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                list.add(resource);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return list;
    }

    public static boolean canUseClassAs(Class<?> toSubclass, Class<?> cl) {
        return toSubclass.isAssignableFrom(cl)
                && ((cl.getModifiers() & Modifier.ABSTRACT) == 0);
    }

    public static Set<Class> fromFileSystem(File directory, String pkgName, Class toSubclass) {
        return fromFileSystem(directory, pkgName, toSubclass, false);
    }

    public static Set<Class> fromFileSystem(File directory, String pkgName,
                                            Class toSubclass, boolean searchSubDir) {
        Set<Class> clazzSet = new HashSet<>();
        // Get the list of the files contained in the package
        File[] files = directory.listFiles();
        for (File file : files) {
            String fileName = file.getName();
            if (searchSubDir && file.isDirectory()) {
                clazzSet.addAll(fromFileSystem(file, pkgName + "." + fileName, toSubclass, searchSubDir));
                continue;
            }
            if (!fileName.endsWith(".class")) {
                continue;
            }
            // removes the .class extension
            String classname = fileName.substring(0, fileName.length() - 6);

            try {
                Class clazz = Class.forName(pkgName + "." + classname);
                if (canUseClassAs(toSubclass, clazz)) {
                    clazzSet.add(clazz);
                }
            } catch (ClassNotFoundException ex) {
                System.err.println(ex);
            }
        }
        return clazzSet;
    }

    public static Set<Class> fromJar(URL url, String pkgName, Class toSubclass)
            throws Exception {
        return fromJar(url, pkgName, toSubclass, false);
    }


    public static Set<Class> fromJar(URL url, String pkgName, Class toSubclass, boolean searchSubDir)
            throws Exception {
        // It does not work with the filesystem: we must
        // be in the case of a package contained in a jar file.
        Set<Class> clazzSet = new HashSet<>();

        //******************************************
        //JarURLConnection conn = (JarURLConnection) url.openConnection();
        //String starts = conn.getEntryName();
        //JarFile jFile = conn.getJarFile(); //此语句异常,原因未知
        //******************************************

        String urlStr = url.toString();
        int JAR_URL_SEPARATOR_INDEX = urlStr.indexOf(JAR_URL_SEPARATOR);
        String starts = urlStr.substring(JAR_URL_SEPARATOR_INDEX + JAR_URL_SEPARATOR.length());
        JarFile jFile = new JarFile(new File(urlStr.substring(JAR_URL_PREFIX.length(), JAR_URL_SEPARATOR_INDEX)));

        Enumeration e = jFile.entries();
        while (e.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) e.nextElement();
            String entryName = entry.getName();
            if (entryName.startsWith(starts)
                    && entryName.endsWith(".class")) {
                //只有在不需要便利子目录的时候才判断
                if (!searchSubDir && !(entryName.lastIndexOf('/') <= starts.length())) {
                    continue;
                }
                String classname = entryName.substring(0, entryName.length() - 6);
                if (classname.startsWith("/"))
                    classname = classname.substring(1);
                classname = classname.replace('/', '.');
                if (!classname.startsWith(pkgName)) {
                    continue;
                }
                try {
                    Class clazz = Class.forName(classname);
                    if (canUseClassAs(toSubclass, clazz)) {
                        clazzSet.add(clazz);
                    }
                } catch (ClassNotFoundException ex) {
                    System.err.println(ex);
                }
            }
        }
        return clazzSet;
    }

}
