package com.github.pmvieira93.gateway;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Strings;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Slf4j
@UtilityClass
public class DockerComposeUtils {

    static final String COMPOSE_CMD = "docker-compose %s";

    public static boolean isDockerServiceRunning(final String serviceName) {
        boolean result = false;
        String options = "ps --format 'table {{.Name}},{{.Service}},{{.Status}}' | tail -1f";
        String composeCmd = String.format(COMPOSE_CMD, options);

        try {
            String output = runner(composeCmd);
            result = output.contains(serviceName);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public static boolean up() {
        return up(List.of());
    }

    public static boolean up(final String service) {
        return up(List.of(service));
    }

    public static boolean up(final List<String> services) {
        boolean result = false;
        List<String> servicesNotRunning = services.stream().filter(s -> !isDockerServiceRunning(s)).toList();
        String servicesToUp = Strings.join(servicesNotRunning).with(" ");
        String options = "up --build -d " + servicesToUp;
        String composeCmd = String.format(COMPOSE_CMD, options);
        try {
            runner(composeCmd);
            result = true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public static boolean down() {
        return down(List.of());
    }

    public static boolean down(final String service) {
        return down(List.of(service));
    }

    public static boolean down(final List<String> services) {
        boolean result = false;
        List<String> servicesRunning = services.stream().filter(DockerComposeUtils::isDockerServiceRunning).toList();
        String servicesToUp = Strings.join(servicesRunning).with(" ");
        String options = "down " + servicesToUp;
        String composeCmd = String.format(COMPOSE_CMD, options);
        try {
            runner(composeCmd);
            result = true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    private static String runner(final String cmd) throws IOException, InterruptedException {
        log.debug("Command: {}", cmd);
        Process process = Runtime.getRuntime().exec(new String[]{
                "bash", "-c", cmd
        });
        await().during(10, TimeUnit.SECONDS);
        String output = new String(process.getInputStream().readAllBytes());
        log.debug("Command output: {}", output);
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Command failed with exit code: " + exitCode);
        }
        return output;
    }
}
