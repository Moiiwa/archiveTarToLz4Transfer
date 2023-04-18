package org.example;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorOutputStream;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.InputMismatchException;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{

    private static Integer BUFFER_SIZE = 4096;

    public static void main( String[] args ) throws IOException {
        if (args.length == 0){
            throw new IllegalArgumentException();
        }
        File decompressionDir = new File("./decompressed");
        decompressionDir.mkdir();

        for (String arg: args){
            File file = new File(arg);
            if (!file.isDirectory()){
                String[] fileNameParts = file.getName().split("\\.");
                if (fileNameParts[fileNameParts.length-1].equals("gz")){
                    FileInputStream inputStream = new FileInputStream(file);
                    extractTarGZ(inputStream, file);
                }
            }
        }
        decompressionDir.delete();

    }

    public static void extractTarGZ(InputStream in, File root) {
        String destName = root.getName().replaceAll("\\.tar","").replaceAll("\\.gz","");
        try {
            GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(in);

            try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
                while (( tarIn.getNextEntry()) != null) {
                    /** If the entry is a directory, create the directory. **/
                        int count;
                    BlockLZ4CompressorOutputStream fos = new BlockLZ4CompressorOutputStream(new FileOutputStream(destName+".lz4", true));

                    byte data[] = new byte[BUFFER_SIZE];
                        try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE)) {
                            while ((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
                                dest.write(data, 0, count);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                }

                System.out.println("Untar completed successfully!");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
