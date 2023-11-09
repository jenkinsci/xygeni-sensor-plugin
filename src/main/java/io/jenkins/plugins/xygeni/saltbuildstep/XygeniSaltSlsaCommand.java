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

public class XygeniSaltSlsaCommand {
    public static void run(
            Run<?, ?> build,
            Launcher launcher,
            TaskListener listener,
            String key,
            String keyPassword,
            String publicKey,
            String pkiFormat,
            String certificate,
            List<Subject> subjects,
            boolean testingMode,
            String output) {

        PrintStream print_stream = null;
        try {

            ArgumentListBuilder args = new ArgumentListBuilder();
            args.add("salt", "at", "--never-fail", "slsa"); // provenance slsa attestation command
            args.add("--pipeline", build.getFullDisplayName());

            args.add("-k", key);
            args.add("--key-password=" + keyPassword);
            args.add("--public-key=" + publicKey);
            args.add("--pki-format=" + pkiFormat);
            if (!certificate.isEmpty()) {
                args.add("--certificate=" + certificate);
            }

            subjects.forEach(subject -> {
                if (subject.isValue()) {
                    args.add("-n", subject.getName());
                    args.add("-v", subject.getValue());
                } else if (subject.isFile()) {
                    args.add("-n", subject.getName());
                    args.add("-f", subject.getFile());
                } else if (subject.isDigest()) {
                    args.add("-n", subject.getName());
                    args.add("--digest=" + subject.getFile());
                } else {
                    args.add("-n", subject.getName());
                    args.add("-i", subject.getImage());
                }
            });

            if (testingMode) {
                args.add("--dry-run");
                args.add("--pretty-print");
            }
            if (!output.isEmpty()) {
                args.add("-o", output);
            }

            Launcher.ProcStarter ps = launcher.launch();
            ps.cmds(args);
            ps.stdin(null);
            ps.stderr(listener.getLogger());

            String outFileName = "out";
            File outFile = new File(build.getRootDir(), outFileName);
            print_stream = new PrintStream(outFile, StandardCharsets.UTF_8);
            ps.stdout(print_stream);
            ps.quiet(true);

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
