package com.m2corp.djm2.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Consumer;

public class DemucsService {

    public File[] separate(File inputFile, Consumer<String> progressConsumer) throws Exception {

        File outputDir = new File("separated");

        ProcessBuilder processBuilder = new ProcessBuilder(
                "python",
                "-m", "demucs",
                "--out", outputDir.getAbsolutePath(),
                inputFile.getAbsolutePath()
        );


        processBuilder.redirectErrorStream(true);

        progressConsumer.accept("Starting Demucs separation for: " + inputFile.getName());
        progressConsumer.accept("This may take several minutes and download AI models on the first run...");

        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                progressConsumer.accept(line);
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Demucs process failed with exit code: " + exitCode);
        }

        progressConsumer.accept("Separation finished successfully!");

        File htdemucsDir = Paths.get(outputDir.getAbsolutePath(), "htdemucs", inputFile.getName().substring(0, inputFile.getName().lastIndexOf('.'))).toFile();

        if (!htdemucsDir.exists() || !htdemucsDir.isDirectory()) {
            throw new RuntimeException("Could not find Demucs output directory: " + htdemucsDir.getAbsolutePath());
        }

        progressConsumer.accept("Found separated files in: " + htdemucsDir.getAbsolutePath());

        return Arrays.stream(htdemucsDir.listFiles())
                .filter(file -> file.getName().endsWith(".wav"))
                .toArray(File[]::new);
    }
}