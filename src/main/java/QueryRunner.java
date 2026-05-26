import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class QueryRunner {

    public static void main(String[] args) {
        String graphdbBaseUrl = args.length > 0 ? args[0] : "http://localhost:7200";
        String repositoryName = args.length > 1 ? args[1] : "MiniProject";
        String queriesPath = args.length > 2 ? args[2] : "queries/sparql_queries.txt";
        String resultsPath = args.length > 3 ? args[3] : "output/query_results.txt";

        new QueryRunner().run(graphdbBaseUrl, repositoryName, queriesPath, resultsPath);
    }

    public void run(String graphdbBaseUrl, String repositoryName, String queriesFilePath) {
        run(graphdbBaseUrl, repositoryName, queriesFilePath, "output/query_results.txt");
    }

    public void run(
            String graphdbBaseUrl,
            String repositoryName,
            String queriesFilePath,
            String resultsFilePath
    ) {
        List<String> queries;
        try {
            queries = loadQueries(Path.of(queriesFilePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read SPARQL queries file: " + queriesFilePath, e);
        }

        List<String> outputLines = new ArrayList<>();
        Path resultsFile = Path.of(resultsFilePath);

        if (queries.isEmpty()) {
            logLine("No executable SPARQL queries found in: " + queriesFilePath, outputLines);
            writeResults(resultsFile, outputLines);
            logLine("Query results written to: " + resultsFile, outputLines);
            return;
        }

        HTTPRepository repository = new HTTPRepository(graphdbBaseUrl, repositoryName);
        try (RepositoryConnection connection = repository.getConnection()) {
            logLine("Executing " + queries.size() + " SPARQL queries...", outputLines);
            for (int i = 0; i < queries.size(); i++) {
                String query = queries.get(i);
                logLine("", outputLines);
                logLine("---- Query " + (i + 1) + " ----", outputLines);
                executeAndPrint(connection, query, outputLines);
            }
        } finally {
            repository.shutDown();
        }

        writeResults(resultsFile, outputLines);
        System.out.println("Query results written to: " + resultsFile);
    }

    private void executeAndPrint(
            RepositoryConnection connection,
            String query,
            List<String> outputLines
    ) {
        TupleQuery tupleQuery = connection.prepareTupleQuery(query);
        try (TupleQueryResult result = tupleQuery.evaluate()) {
            List<String> bindingNames = result.getBindingNames();
            int rows = 0;

            while (result.hasNext()) {
                BindingSet row = result.next();
                rows++;

                StringBuilder sb = new StringBuilder();
                sb.append("Row ").append(rows).append(": ");
                for (int i = 0; i < bindingNames.size(); i++) {
                    String name = bindingNames.get(i);
                    String value = row.hasBinding(name) ? row.getValue(name).stringValue() : "";
                    sb.append(name).append("=").append(value);
                    if (i < bindingNames.size() - 1) {
                        sb.append(" | ");
                    }
                }
                logLine(sb.toString(), outputLines);
            }

            logLine("Rows returned: " + rows, outputLines);
        } catch (Exception e) {
            logLine("Query failed: " + e.getMessage(), outputLines);
        }
    }

    private void writeResults(Path outputFile, List<String> lines) {
        try {
            Path parent = outputFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(outputFile, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write query results file: " + outputFile, e);
        }
    }

    private void logLine(String message, List<String> outputLines) {
        System.out.println(message);
        outputLines.add(message);
    }

    private List<String> loadQueries(Path queriesFile) throws IOException {
        if (!Files.exists(queriesFile)) {
            throw new IOException("File not found: " + queriesFile);
        }

        String content = Files.readString(queriesFile, StandardCharsets.UTF_8);
        String[] blocks = content.split("(?m)^=+\\R");
        List<String> queries = new ArrayList<>();

        for (String block : blocks) {
            String[] lines = block.split("\\R");
            int start = firstQueryLineIndex(lines);
            if (start < 0) {
                continue;
            }

            StringBuilder query = new StringBuilder();
            for (int i = start; i < lines.length; i++) {
                query.append(lines[i]).append(System.lineSeparator());
            }

            String queryText = query.toString().trim();
            if (queryText.contains("SELECT") && queryText.contains("WHERE")) {
                queries.add(queryText);
            }
        }

        return queries;
    }

    private int firstQueryLineIndex(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (trimmed.startsWith("PREFIX ") || trimmed.startsWith("SELECT ")) {
                return i;
            }
        }
        return -1;
    }
}
