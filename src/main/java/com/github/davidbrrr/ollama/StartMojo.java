package com.github.davidbrrr.ollama;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Mojo(name = "start")
public class StartMojo extends AbstractMojo {

  @Parameter(property = "ollama.image", defaultValue = "ollama/ollama")
  private String image;

  @Parameter(property = "ollama.containerName", defaultValue = "ollama-maven")
  private String containerName;

  @Parameter(property = "ollama.port", defaultValue = "11434")
  private int port;

  @Parameter(property = "ollama.model", defaultValue = "gemma3:latest")
  private String model;

  @Parameter(property = "ollama.preloadModel", defaultValue = "true")
  private Boolean preloadModel;

  @Parameter(property = "ollama.cleanOnStartup", defaultValue = "false")
  private Boolean cleanOnStartup;

  public void execute() throws MojoExecutionException {
    try {
      getLog().info("Pulling Ollama image...");
      new ProcessBuilder("docker", "pull", image).inheritIO().start().waitFor();

      try {
        if (!containerExists()) {
          createAndStartContainer();
        } else if (cleanOnStartup) {
          stopAndRemoveContainer();
          createAndStartContainer();
        } else if (!containerIsRunning()) {
          getLog().info("Container exists but is stopped. Starting container...");
          new ProcessBuilder("docker", "start", containerName).inheritIO().start().waitFor();
        } else {
          getLog().info("Container is already running.");
        }
      } catch (IOException | InterruptedException e) {
        throw new MojoExecutionException("Failed to manage Docker container.", e);
      }

      if(preloadModel) {
        getLog().info("Preloading model: " + model);
        new ProcessBuilder("docker", "exec", containerName, "ollama", "pull", model)
            .inheritIO().start().waitFor();
      }

    } catch (IOException | InterruptedException e) {
      throw new MojoExecutionException("Failed to start Ollama container.", e);
    }
  }

  private boolean containerExists() throws IOException, InterruptedException {
    Process process = new ProcessBuilder("docker", "ps", "-a", "--filter", "name=" + containerName, "--format", "{{.ID}}").start();
    String output = readProcessOutput(process);
    int exitCode = process.waitFor();
    return exitCode == 0 && !output.isEmpty();
  }

  private boolean containerIsRunning() throws IOException, InterruptedException {
    Process process = new ProcessBuilder("docker", "ps", "--filter", "name=" + containerName, "--format", "{{.ID}}").start();
    String output = readProcessOutput(process);
    int exitCode = process.waitFor();
    return exitCode == 0 && !output.isEmpty();
  }

  private void createAndStartContainer() throws IOException, InterruptedException {
    getLog().info("No existing container found. Creating and starting new container...");
    new ProcessBuilder("docker", "run", "-d", "-p", port + ":11434", "--name", containerName, image)
        .inheritIO().start().waitFor();
  }

  private void stopAndRemoveContainer() throws IOException, InterruptedException {
    getLog().info("Stopping and removing existing Ollama container: " + containerName);

    new ProcessBuilder("docker", "stop", containerName)
        .inheritIO().start().waitFor();

    new ProcessBuilder("docker", "rm", containerName)
        .inheritIO().start().waitFor();
  }

  private String readProcessOutput(Process process) throws IOException {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line = reader.readLine();
      return line == null ? "" : line.trim();
    }
  }
}
