package net.alloyggp.perf;

public class RunAllTestsFirstPass {
    public static void main(String[] args) throws Exception {
        MissingEntriesPerfTestRunner.main(new String[0]);
        MissingEntriesCorrectnessTestRunner.main(new String[0]);
    }
}
