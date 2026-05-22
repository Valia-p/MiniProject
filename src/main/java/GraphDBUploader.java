import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class GraphDBUploader {
    private static final String DEFAULT_GRAPHDB_BASE_URL = "http://localhost:7200";
    private static final String DEFAULT_REPOSITORY_NAME = "MiniProjekt";
    private static final String DEFAULT_GENERATED_TTL_PATH = "output/generated.ttl";
    private static final String DATA_BASE_IRI = "http://example.org/resource/";
    private static final String ONTOLOGY_BASE_IRI = "http://example.org/ontology/";

    public static void main(String[] args) {
        String graphdbBaseUrl = args.length > 0 ? args[0] : DEFAULT_GRAPHDB_BASE_URL;
        String repositoryName = args.length > 1 ? args[1] : DEFAULT_REPOSITORY_NAME;
        String generatedTtlPath = args.length > 2 ? args[2] : DEFAULT_GENERATED_TTL_PATH;
        boolean clearBeforeLoad = args.length > 3 && Boolean.parseBoolean(args[3]);

        new GraphDBUploader().run(
                graphdbBaseUrl,
                repositoryName,
                generatedTtlPath,
                clearBeforeLoad
        );
    }

    public void run(
            String graphdbBaseUrl,
            String repositoryName,
            String generatedTtlPath,
            boolean clearBeforeLoad
    ) {
        HTTPRepository repository = new HTTPRepository(graphdbBaseUrl, repositoryName);

        System.out.println("GraphDB base URL  : " + graphdbBaseUrl);
        System.out.println("Repository name   : " + repositoryName);
        System.out.println("Generated TTL     : " + generatedTtlPath);
        System.out.println("Clear before load : " + clearBeforeLoad);
        System.out.println("Connecting to GraphDB...");

        try (RepositoryConnection connection = repository.getConnection()) {
            System.out.println("Connected.");

            if (clearBeforeLoad) {
                connection.clear();
                System.out.println("Repository cleared.");
            } else {
                System.out.println("Keeping existing repository data.");
            }

            uploadOntologyIfSupported(connection);
            System.out.println("Uploading RDF file...");
            uploadTurtle(connection, Path.of(generatedTtlPath));
            System.out.println("Upload completed successfully.");
            printTripleCount(connection);
        } catch (Exception e) {
            throw new RuntimeException("GraphDB upload failed: " + e.getMessage(), e);
        } finally {
            repository.shutDown();
        }
    }

    private void uploadTurtle(RepositoryConnection connection, Path ttlFile) throws IOException {
        if (!Files.exists(ttlFile)) {
            throw new IOException("TTL file not found: " + ttlFile);
        }
        try (FileInputStream fis = new FileInputStream(ttlFile.toFile())) {
            connection.add(fis, DATA_BASE_IRI, RDFFormat.TURTLE);
        }
        System.out.println("Uploaded RDF: " + ttlFile);
    }

    private void uploadOntologyIfSupported(RepositoryConnection connection) throws IOException {
        Path ontologyTtl = firstExistingPath("ontology/ontology.ttl", "ontology.ttl");
        Path ontologyRdf = firstExistingPath("ontology/ontology.rdf", "ontology.rdf");
        Path ontologyOwl = firstExistingPath("ontology/ontology.owl", "ontology.owl");

        if (ontologyTtl != null) {
            try (FileInputStream fis = new FileInputStream(ontologyTtl.toFile())) {
                connection.add(fis, ONTOLOGY_BASE_IRI, RDFFormat.TURTLE);
            }
            System.out.println("Uploaded ontology: " + ontologyTtl);
            return;
        }

        if (ontologyRdf != null) {
            try (FileInputStream fis = new FileInputStream(ontologyRdf.toFile())) {
                connection.add(fis, ONTOLOGY_BASE_IRI, RDFFormat.RDFXML);
            }
            System.out.println("Uploaded ontology: " + ontologyRdf);
            return;
        }

        if (ontologyOwl != null) {
            if (isOwlFunctionalSyntax(ontologyOwl)) {
                System.out.println(
                        "Skipped ontology.owl (OWL Functional Syntax). Convert it to TTL/RDFXML if you want automatic upload."
                );
                return;
            }
            try (FileInputStream fis = new FileInputStream(ontologyOwl.toFile())) {
                connection.add(fis, ONTOLOGY_BASE_IRI, RDFFormat.RDFXML);
            }
            System.out.println("Uploaded ontology: " + ontologyOwl);
            return;
        }

        System.out.println("No ontology file found for auto-upload.");
    }

    private boolean isOwlFunctionalSyntax(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            return trimmed.startsWith("Prefix(") || trimmed.startsWith("Ontology(");
        }
        return false;
    }

    private Path firstExistingPath(String... candidates) {
        return Arrays.stream(candidates)
                .map(Path::of)
                .filter(Files::exists)
                .findFirst()
                .orElse(null);
    }

    private void printTripleCount(RepositoryConnection connection) {
        String sparql = "SELECT (COUNT(*) AS ?triples) WHERE { ?s ?p ?o }";
        try (TupleQueryResult result = connection.prepareTupleQuery(sparql).evaluate()) {
            if (result.hasNext()) {
                String count = result.next().getValue("triples").stringValue();
                System.out.println("Triple count in repository: " + count);
            }
        }
    }
}
