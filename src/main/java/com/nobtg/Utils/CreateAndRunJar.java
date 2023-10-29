package com.nobtg.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class CreateAndRunJar {
    public static void start() {
        try {
            File tempJarFile = File.createTempFile("tempJar", ".jar");
            try (JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tempJarFile))) {
                addClassToJar(Main.class, jarOutputStream);
                addClassToJar(GradientText.class, jarOutputStream);
                Manifest manifest = new Manifest();
                manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
                manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, Main.class.getName());
                jarOutputStream.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
                manifest.write(new BufferedOutputStream(jarOutputStream));
                jarOutputStream.closeEntry();
            }
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", tempJarFile.getAbsolutePath());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            Runtime.getRuntime().addShutdownHook(new Thread(process::destroyForcibly));
            new Thread(() -> {
                int exitCode;
                try {
                    exitCode = process.waitFor();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Exit Code: " + exitCode);
                try {
                    printProcessOutput(process);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                tempJarFile.delete();
            }).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addClassToJar(@NotNull Class<?> clazz, @NotNull JarOutputStream jarOutputStream) throws IOException {
        String className = clazz.getName().replace('.', '/') + ".class";
        try (InputStream inputStream = clazz.getClassLoader().getResourceAsStream(className)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            jarOutputStream.putNextEntry(new JarEntry(className));
            if (inputStream != null) {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    jarOutputStream.write(buffer, 0, bytesRead);
                }
            }
            jarOutputStream.closeEntry();
        }
    }

    private static void printProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}