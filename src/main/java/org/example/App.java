package org.example;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FrameOutputStream;
import net.jpountz.xxhash.XXHashFactory;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Locale;

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
        long startTime = System.nanoTime();

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
        long endTime = System.nanoTime();
        long durationInSeconds = (endTime - startTime) / 1000000000;
        System.out.println(durationInSeconds);
    }

    public static void extractTarGZ(InputStream in, File root) {
        String destName = root.getName().replaceAll("\\.gz","");
        try {
            GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(in);
            LZ4Factory lz4Factory = LZ4Factory.fastestJavaInstance();
            LZ4FrameOutputStream.FLG.Bits[] bits = new LZ4FrameOutputStream.FLG.Bits[]{LZ4FrameOutputStream.FLG.Bits.BLOCK_INDEPENDENCE};
            LZ4FrameOutputStream frameOutputStream = new LZ4FrameOutputStream(new FileOutputStream(destName+".lz4", true), LZ4FrameOutputStream.BLOCKSIZE.SIZE_4MB,
                    -1L, lz4Factory.fastCompressor(), XXHashFactory.safeInstance().hash32(), bits);

            IOUtils.copy(gzipIn, frameOutputStream);
            System.out.println("Untar completed successfully!");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
