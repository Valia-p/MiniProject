public class Pipeline {

    private static final String DEFAULT_GRAPHDB_BASE_URL = "http://localhost:7200";
    private static final String DEFAULT_REPOSITORY_NAME = "MiniProject";
    private static final String DEFAULT_MAPPING_PATH = "mappings/mapping.ttl";
    private static final String DEFAULT_GENERATED_TTL_PATH = "output/generated.ttl";
    private static final String DEFAULT_QUERIES_PATH = "queries/sparql_queries.txt";

    public static void main(String[] args) {
        String graphdbBaseUrl = args.length > 0 ? args[0] : DEFAULT_GRAPHDB_BASE_URL;
        String repositoryName = args.length > 1 ? args[1] : DEFAULT_REPOSITORY_NAME;
        String generatedTtlPath = args.length > 2 ? args[2] : DEFAULT_GENERATED_TTL_PATH;
        boolean clearBeforeLoad = args.length > 3 && Boolean.parseBoolean(args[3]);
        String mappingPath = args.length > 4 ? args[4] : DEFAULT_MAPPING_PATH;
        String queriesPath = args.length > 5 ? args[5] : DEFAULT_QUERIES_PATH;

        System.out.println("=== Pipeline configuration ===");
        System.out.println("GraphDB base URL : " + graphdbBaseUrl);
        System.out.println("Repository name  : " + repositoryName);
        System.out.println("Mapping file     : " + mappingPath);
        System.out.println("Generated TTL    : " + generatedTtlPath);
        System.out.println("Clear repository : " + clearBeforeLoad);
        System.out.println("Queries file     : " + queriesPath);
        System.out.println("==============================");

        new RMLMapping().start(mappingPath, generatedTtlPath);
        new GraphDBUploader().run(graphdbBaseUrl, repositoryName, generatedTtlPath, clearBeforeLoad);
        new QueryRunner().run(graphdbBaseUrl, repositoryName, queriesPath);
    }
}
