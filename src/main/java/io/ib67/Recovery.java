package io.ib67;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Recovery {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Args should be jar file name.");
            return;
        }
        File temp=new File("temp");
        temp.mkdir();
        unzipJar("temp",args[0]);
        for (File file : FileUtils.listFiles(temp, null, true)) {
            if(file.getName().endsWith("class")){
                System.out.println("Loading..."+file.getName());
                String newName=new ClassReader(new FileInputStream(file)).getClassName();
                System.out.println("Mapping: "+file.getName()+" to "+newName+".class");
                file.renameTo(new File(file.getParent()+"/"+newName+".class"));
            }else{
                System.out.println("Skip..."+file.getName());
            }
        }
        FileOutputStream fos = new FileOutputStream("output.jar");
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        System.out.println("Compressing....output.jar");
        zipFile(temp, ".", zipOut);
        zipOut.close();
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
    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }
}

