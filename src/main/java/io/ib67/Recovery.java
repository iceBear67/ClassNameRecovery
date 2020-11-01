package io.ib67;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;

import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Recovery {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Args should be jar file name.");
            return;
        }
        File temp=new File("temp");
        File out=new File("out");
        temp.mkdir();
        out.mkdir();
        unzipJar("temp",args[0]);
        for (File file : FileUtils.listFiles(temp, null, true)) {
            if(file.getName().endsWith("class")){
                System.out.println("Loading..."+file.getName());
                String newName=new ClassReader(new FileInputStream(file)).getClassName();
                System.out.println("Mapping: "+file.getName()+" to "+newName+".class");
                File destin=new File(out+"/"+newName+".class");
                destin.getParentFile().mkdirs();
                Files.move(file.toPath(),destin.toPath());
            }else{
                System.out.println("Skip..."+file.getName());
            }
        }
        FileOutputStream fos = new FileOutputStream("output.jar");
        JarOutputStream jarOut=new JarOutputStream(fos);
        System.out.println("Compressing....output.jar");
        zipFile(out, ".", jarOut);
        jarOut.close();
        fos.close();
        FileUtils.deleteDirectory(temp);

    }
    public static void unzipJar(String destinationDir, String jarPath) throws IOException {
        File file = new File(jarPath);
        JarFile jar = new JarFile(file);

        // fist get all directories,
        // then make those directory on the destination Path
        for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
            JarEntry entry = (JarEntry) enums.nextElement();

            String fileName = destinationDir + File.separator + entry.getName();
            File f = new File(fileName);

            if (fileName.endsWith("/")) {
                f.mkdirs();
            }

        }

        //now create all files
        for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
            JarEntry entry = (JarEntry) enums.nextElement();

            String fileName = destinationDir + File.separator + entry.getName();
            File f = new File(fileName);

            if (!fileName.endsWith("/")) {
                InputStream is = jar.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(f);

                // write contents of 'is' to 'fos'
                while (is.available() > 0) {
                    fos.write(is.read());
                }

                fos.close();
                is.close();
            }
        }
    }
    private static void zipFile(File fileToZip, String fileName, JarOutputStream jarOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                jarOut.putNextEntry(new ZipEntry(fileName));
                jarOut.closeEntry();
            } else {
                jarOut.putNextEntry(new ZipEntry(fileName + "/"));
                jarOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), jarOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        JarEntry zipEntry = new JarEntry(fileName);
        jarOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            jarOut.write(bytes, 0, length);
        }
        fis.close();
    }
}

