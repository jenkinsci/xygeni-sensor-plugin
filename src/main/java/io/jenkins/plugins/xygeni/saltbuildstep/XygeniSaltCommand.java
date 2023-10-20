package io.jenkins.plugins.xygeni.saltbuildstep;

import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Subject;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class XygeniSaltCommand {
    public static void run(
            Run<?, ?> build,
            Launcher launcher,
            TaskListener listener,
            String key,
            String keyPass,
            List<Subject> subjects) {

        PrintStream print_stream = null;
        try {

            ArgumentListBuilder args = new ArgumentListBuilder();
            args.add("salt", "at", "slsa", "--never-fail"); // provenance slsa attestation command
            args.add("--pipeline", build.getFullDisplayName());


            subjects.forEach(subject -> {
                if (subject.isValue()) {
                    args.add("-n", subject.getName());
                    args.add("-v", subject.getValue());
                } else if (subject.isFile()) {
                    args.add("-n", subject.getName());
                    args.add("-f", subject.getFile());
                } else {
                    args.add("-n", subject.getName());
                    args.add("-i", subject.getImage());
                }
            });

            args.add("-k", key);
            args.add("--key-password=" + keyPass);

            // TODO for testing
            args.add("--dry-run");
            args.add("-o", "xygeni-salt-result.json");

            Launcher.ProcStarter ps = launcher.launch();
            ps.cmds(args);
            ps.stdin(null);
            ps.stderr(listener.getLogger());

            String outFileName = "out";
            File outFile = new File(build.getRootDir(), outFileName);
            print_stream = new PrintStream(outFile, StandardCharsets.UTF_8);
            ps.stdout(print_stream);
            ps.quiet(true);
            boolean[] masks = new boolean[ps.cmds().size()];
            // masks[passwordIndex] = true; // Mask out password
            ps.masks(masks);

            ps.masks(masks);
            listener.getLogger().println("Salt Attestation in progress...");
            listener.getLogger().println("" + args.toString());
            ps.join(); // RUN !

        } catch (IOException | InterruptedException e) {
            listener.getLogger().println("Error running Xygeni Salt:" + e.getMessage());
        } finally {
            if (print_stream != null) {
                print_stream.close();
            }
        }
    }
}
