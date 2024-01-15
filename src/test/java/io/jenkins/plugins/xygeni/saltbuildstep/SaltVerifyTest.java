package io.jenkins.plugins.xygeni.saltbuildstep;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Certs;
import io.jenkins.plugins.xygeni.saltbuildstep.model.Subject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

class SaltVerifyTest {

    @Test
    void runVerifyId(@TempDir File tempDir) throws IOException, InterruptedException {

        String expected = "salt at --never-fail verify --pipeline=MyPipeline --basedir=$WORKSPACE"
                + " -o target/** -k env:PUBKEY --id=at-id -n subject-name --digest=sha256:abc";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(baos);

        Certs certs = new Certs(null, null, null, null, null, true);
        List<Subject> subjects = new ArrayList<>();
        subjects.add(new Subject("subject-name", "", "", "", "sha256:abc"));
        runVerify("target/**", "env:PUBKEY", null, subjects, "at-id", null, tempDir, printStream);

        assertLogContains(expected, baos);
    }

    @Test
    void runVerifyAttestation(@TempDir File tempDir) throws IOException, InterruptedException {

        String expected = "salt at --never-fail verify --pipeline=MyPipeline --basedir=$WORKSPACE"
                + " -o target/** -k env:PUBKEY --attestation=at-file.json -n subject-name --digest=sha256:abc";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(baos);

        Certs certs = new Certs("env:KEY", "env:PASS", "env:PUBKEY", "x509", null, false);
        List<Subject> subjects = new ArrayList<>();
        subjects.add(new Subject("subject-name", "", "", "", "sha256:abc"));
        runVerify("target/**", "env:PUBKEY", null, subjects, null, "at-file.json", tempDir, printStream);

        assertLogContains(expected, baos);
    }

    private void assertLogContains(String expected, ByteArrayOutputStream baos) {
        String output = baos.toString();
        Assertions.assertTrue(
                output.contains(expected), "Expected: \n" + expected + "\nnot found in: \n" + baos.toString());
    }

    private void runVerify(
            String output,
            String publicKey,
            String certificate,
            List<Subject> subjects,
            String id,
            String attestation,
            File tempDir,
            PrintStream printStream)
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

        SaltVerifyRecorder p = new SaltVerifyRecorder(output, publicKey, certificate, id, attestation);
        p.setSubjects(subjects);
        p.perform(mockRun, mockWorkspace, mockEVars, new Launcher.DummyLauncher(mockTList), mockTList);
    }
}
