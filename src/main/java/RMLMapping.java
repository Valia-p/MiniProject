import be.ugent.rml.Executor;
import be.ugent.rml.Utils;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.QuadStoreFactory;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.NamedNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class RMLMapping {

    public static void main(String[] args) {
        String mappingFile = args.length > 0 ? args[0] : "mappings/mapping.ttl";
        String outputFile = args.length > 1 ? args[1] : "output/generated.ttl";
        new RMLMapping().start(mappingFile, outputFile);
    }

    public void start(String mappingFilePath, String outputTtlPath) {
        File mappingFile = new File(mappingFilePath);
        if (!mappingFile.exists()) {
            throw new RuntimeException("Mapping file not found: " + mappingFilePath);
        }

        try {
            String baseIri = Utils.getBaseDirectiveTurtle(mappingFile.getAbsolutePath());
            if (baseIri == null || baseIri.isBlank()) {
                baseIri = "http://example.org/resource/";
            }

            QuadStore mappingStore;
            try (InputStream mappingStream = new FileInputStream(mappingFile)) {
                mappingStore = QuadStoreFactory.read(mappingStream);
            }

            String projectRoot = Path.of("").toAbsolutePath().normalize().toString();
            RecordsFactory recordsFactory = new RecordsFactory(projectRoot);
            QuadStore outputStore = new RDF4JStore();

            Executor executor = new Executor(
                    mappingStore,
                    recordsFactory,
                    outputStore,
                    baseIri,
                    null
            );

            QuadStore result = executor.execute(null)
                    .get(new NamedNode("rmlmapper://default.store"));

            Path outputPath = Path.of(outputTtlPath);
            Path parent = outputPath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                result.write(writer, "turtle");
            }

            System.out.println("RML mapping completed. Output written to: " + outputPath);
        } catch (Exception e) {
            throw new RuntimeException("RML mapping failed: " + e.getMessage(), e);
        }
    }
}
