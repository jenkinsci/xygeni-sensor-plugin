package io.jenkins.plugins.xygeni.saltbuildstep;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.xygeni.saltbuildstep.model.AttestationOptions;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Certs;
import io.jenkins.plugins.xygeni.saltbuildstep.model.OutputOptions;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

public class SaltAtCommitTest {

    @Test
    void runSaltCommitWithKeyless(@TempDir File tempDir) throws IOException, InterruptedException {

        String expected = "salt at --never-fail commit --pipeline=MyPipeline --basedir=$WORKSPACE"
                + " --no-upload --project=MyProject -o out.json --pretty-print --keyless";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(baos);

        Certs certs = new Certs(null, null, null, null, null, true);
        runCommit(certs, tempDir, printStream);

        assertLogContains(expected, baos);
    }

    @Test
    void runSaltCommitWithKey(@TempDir File tempDir) throws IOException, InterruptedException {

        String expected = "salt at --never-fail commit --pipeline=MyPipeline --basedir=$WORKSPACE"
                + " --no-upload --project=MyProject -o out.json --pretty-print -k env:KEY --key-password=env:PASS --public-key=env:PUBKEY --pki-format=x509";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(baos);

        Certs certs = new Certs("env:KEY", "env:PASS", "env:PUBKEY", "x509", null, false);
        runCommit(certs, tempDir, printStream);

        assertLogContains(expected, baos);
    }

    private void assertLogContains(String expected, ByteArrayOutputStream baos) {
        String output = baos.toString();
        Assertions.assertTrue(
                output.contains(expected), "Expected: \n" + expected + "\nnot found in: \n" + baos.toString());
    }

    private void runCommit(Certs certs, File tempDir, PrintStream printStream)
            throws IOException, InterruptedException {

        Run mockRun = Mockito.mock(Run.class);
        Mockito.when(mockRun.getResult()).thenReturn(Result.SUCCESS);
        Mockito.when(mockRun.getRootDir()).thenReturn(tempDir);
        Mockito.when(mockRun.getFullDisplayName()).thenReturn("MyPipeline");

        TaskListener mockTList = Mockito.mock(TaskListener.class);
        Mockito.when(mockTList.getLogger()).thenReturn(printStream);

        EnvVars mockEVars = Mockito.mock(EnvVars.class);
        Mockito.when(mockEVars.expand(Mockito.anyString())).thenReturn("");

        FilePath mockWorkspace = Mockito.mock(FilePath.class);
        Mockito.when(mockWorkspace.list(Mockito.anyString())).thenReturn(new FilePath[] {new FilePath(tempDir)});

        SaltAtCommitRecorder p = new SaltAtCommitRecorder(
                certs, new AttestationOptions(true, "MyProject", false), new OutputOptions("out.json", true, null));
        p.perform(mockRun, mockWorkspace, mockEVars, new Launcher.DummyLauncher(mockTList), mockTList);
    }
}
