# MiniProject - Semantic Web Soundtracks

Το project μετατρέπει ένα dataset με τραγούδια και ταινίες σε RDF γράφο, τον φορτώνει σε GraphDB και παρέχει έτοιμα SPARQL queries για εξερεύνηση των δεδομένων.

Ο φάκελος του project είναι ο φάκελος που περιέχει τα αρχεία `pom.xml`, `README.md`, `ontology.owl`, `mappings/`, `output/`, `queries/`, `scripts/` και `src/`. Όλες οι εντολές παρακάτω θεωρούν ότι το terminal είναι ανοιχτό μέσα σε αυτόν τον φάκελο.

Δεν χρειάζεται να κατέβει ξεχωριστό dataset ή RMLMapper jar για τη βασική εκτέλεση. Το project περιέχει ήδη τα βασικά παραγόμενα artifacts:

- `data/processed/soundtracks_clean.json`
- `output/generated.ttl`
- `queries/sparql_queries.txt`

## Στόχος Εργασίας

Στόχος είναι να μοντελοποιηθούν πληροφορίες για soundtracks ταινιών ως σημασιολογικός γράφος. Το project ξεκινά από ένα Excel dataset, δημιουργεί καθαρή JSON δομή, χρησιμοποιεί RML mapping για παραγωγή RDF triples και τελικά φορτώνει το παραγόμενο Turtle αρχείο σε GraphDB.

Η λογική ροή του project είναι:

```text
raw Excel dataset
  -> Python preprocessing
  -> cleaned JSON
  -> RML mapping
  -> generated Turtle RDF
  -> GraphDB repository
  -> SPARQL queries
```

Για την απλή εκτέλεση/αξιολόγηση δεν χρειάζεται να ξαναπαραχθούν όλα τα ενδιάμεσα αρχεία, επειδή το `output/generated.ttl` υπάρχει ήδη.

## Τεχνολογίες

- Python 3 για καθαρισμό και μετατροπή του αρχικού dataset.
- RML/R2RML mapping για περιγραφή της μετατροπής από JSON σε RDF.
- RDF/Turtle ως τελική μορφή RDF εξόδου.
- Java 17 με RDF4J για φόρτωση του RDF αρχείου στο GraphDB.
- Maven για build και εκτέλεση του Java uploader.
- GraphDB για αποθήκευση RDF γράφου και εκτέλεση SPARQL queries.

## Δομή Project

```text
MiniProject/
├── common-mapping.json
├── ontology.owl
├── pom.xml
├── README.md
├── data/
│   ├── raw/
│   │   └── final-cleaned-songs-csv.xls
│   └── processed/
│       ├── columns_profile.json
│       ├── soundtracks_clean.json
│       └── soundtracks_structure.json
├── mappings/
│   └── mapping.ttl
├── output/
│   └── generated.ttl
├── queries/
│   └── sparql_queries.txt
├── scripts/
│   └── convert_dataset_to_json.py
├── src/
│   └── main/java/GraphDBUploader.java
└── target/
    └── ...
```

## Κύρια Αρχεία

| Αρχείο | Περιγραφή |
| --- | --- |
| `data/raw/final-cleaned-songs-csv.xls` | Το αρχικό Excel dataset με τραγούδια, ταινίες, contributors, genres, γλώσσες, χώρες παραγωγής και metadata ταινιών. |
| `scripts/convert_dataset_to_json.py` | Python preprocessing script. Καθαρίζει στήλες, χειρίζεται null values, σπάει multi-value πεδία σε λίστες και δημιουργεί slugs για IRIs. |
| `data/processed/soundtracks_clean.json` | Καθαρισμένο JSON dataset. Είναι η κύρια είσοδος για το RML mapping. |
| `data/processed/columns_profile.json` | Metadata για το preprocessing, όπως source file, αριθμός γραμμών, mapping στηλών και multi-value columns. |
| `mappings/mapping.ttl` | RML mapping που περιγράφει πώς το JSON μετατρέπεται σε RDF resources και σχέσεις. |
| `output/generated.ttl` | Έτοιμο RDF/Turtle αρχείο που φορτώνεται στο GraphDB. |
| `ontology.owl` | OWL ontology του project σε OWL Functional Syntax. Περιέχει classes/properties για films, songs, people roles, genres, countries, languages και companies. |
| `src/main/java/GraphDBUploader.java` | Java πρόγραμμα που συνδέεται στο GraphDB και ανεβάζει το `output/generated.ttl`. |
| `queries/sparql_queries.txt` | Έτοιμα SPARQL queries για έλεγχο και παρουσίαση του RDF γράφου. |
| `pom.xml` | Maven configuration για Java 17 και RDF4J dependencies. |

## Πώς να Τρέξει το Project

### Βήμα 0: Άνοιγμα terminal στο project root

Άνοιξε terminal μέσα στον φάκελο `MiniProject`, δηλαδή στον φάκελο που περιέχει το `pom.xml`.

Αν βρίσκεσαι έναν φάκελο πιο πάνω, μπες στο project με:

```powershell
cd MiniProject
```

Όλες οι επόμενες εντολές χρησιμοποιούν relative paths και δεν εξαρτώνται από προσωπικό path υπολογιστή.

### Βήμα 1: Έλεγχος ότι υπάρχει το RDF αρχείο

Το project περιέχει ήδη το έτοιμο RDF αρχείο:

```text
output/generated.ttl
```

Αυτό είναι το αρχείο που θα φορτωθεί στο GraphDB. Για τη βασική εκτέλεση δεν χρειάζεται να τρέξει RML processor ούτε να κατέβει κάποιο extra RML jar.

### Βήμα 2: Εκκίνηση GraphDB

Άνοιξε GraphDB και βεβαιώσου ότι το Workbench είναι διαθέσιμο στο:

```text
http://localhost:7200
```

Δημιούργησε ή επίλεξε repository με ID:

```text
MiniProjekt
```

Το ίδιο ID χρησιμοποιείται ως default και από τον Java uploader.

### Βήμα 3: Φόρτωση του RDF στο GraphDB με Java

Τρέξε:

```powershell
mvn compile exec:java
```

Η παραπάνω εντολή χρησιμοποιεί τις default τιμές του `GraphDBUploader`:

```text
GraphDB URL: http://localhost:7200
Repository: MiniProjekt
TTL file: output/generated.ttl
clearBeforeLoad: false
```

Αν θέλεις να καθαριστεί πρώτα το repository και μετά να φορτωθεί από την αρχή το RDF αρχείο, τρέξε:

```powershell
mvn compile exec:java "-Dexec.args=http://localhost:7200 MiniProjekt output/generated.ttl true"
```

Το τελευταίο argument σημαίνει:

- `false`: κρατά τα υπάρχοντα triples και προσθέτει το RDF αρχείο.
- `true`: καθαρίζει πρώτα το repository και μετά φορτώνει το RDF αρχείο.

Αναμενόμενη έξοδος:

```text
GraphDB base URL  : http://localhost:7200
Repository name   : MiniProjekt
Generated TTL     : output/generated.ttl
Clear before load : false
Connecting to GraphDB...
Connected.
Keeping existing repository data.
Skipped ontology.owl (OWL Functional Syntax). Convert it to TTL/RDFXML if you want automatic upload.
Uploading RDF file...
Uploaded RDF: output/generated.ttl
Upload completed successfully.
Triple count in repository: ...
```

Αν το Maven δεν είναι διαθέσιμο στο περιβάλλον εκτέλεσης, μπορεί να γίνει import του ίδιου αρχείου απευθείας από το GraphDB Workbench:

```text
Import -> RDF -> Upload RDF files -> output/generated.ttl
```

Με αυτόν τον τρόπο φορτώνεται το ίδιο RDF graph, χωρίς να αλλάζουν τα queries.

### Βήμα 4: Εκτέλεση SPARQL queries

Αφού φορτωθούν τα δεδομένα:

1. Άνοιξε το GraphDB Workbench στο `http://localhost:7200`.
2. Επίλεξε το repository `MiniProjekt`.
3. Άνοιξε την καρτέλα SPARQL.
4. Άνοιξε το αρχείο `queries/sparql_queries.txt`.
5. Κάνε copy/paste ένα query στο GraphDB και πάτησε Run.

Τα διαθέσιμα queries είναι:

| Query | Σκοπός |
| --- | --- |
| Query 1 | Εμφανίζει ταινίες, τραγούδια και artists. |
| Query 2 | Εμφανίζει ταινίες ανά genre και χώρα παραγωγής. |
| Query 3 | Συνδέει directors με films και soundtracks. |
| Query 4 | Ελέγχει τη σχέση `ex:partOfFilm` ανάμεσα σε song και film. |
| Query 5 | Βρίσκει άτομα που εμφανίζονται με πολλαπλούς ρόλους, π.χ. artist, composer, director, actor. |

## Optional: Αναπαραγωγή Ενδιάμεσων Αρχείων

Τα παρακάτω βήματα δεν χρειάζονται για τη βασική εκτέλεση, επειδή τα παραγόμενα αρχεία υπάρχουν ήδη στο project. Χρησιμοποιούνται μόνο αν αλλάξει το raw dataset ή αν χρειάζεται να αναπαραχθεί το pipeline από την αρχή.

### 1. Raw Excel σε cleaned JSON

```powershell
python scripts\convert_dataset_to_json.py
```

Αναμενόμενη έξοδος:

```text
Input: data\raw\final-cleaned-songs-csv.xls
Rows: 5288
Multi-value columns: 20
Output: data\processed\soundtracks_clean.json
Profile: data\processed\columns_profile.json
```

Το script παράγει:

```text
data/processed/soundtracks_clean.json
data/processed/columns_profile.json
```

### 2. Cleaned JSON σε RDF/Turtle

Το mapping βρίσκεται στο:

```text
mappings/mapping.ttl
```

και η είσοδός του είναι:

```text
data/processed/soundtracks_clean.json
```

Το τελικό RDF αρχείο που χρησιμοποιεί το project είναι ήδη διαθέσιμο στο:

```text
output/generated.ttl
```

Γι' αυτό το README δεν απαιτεί κατέβασμα εξωτερικού RMLMapper jar για τη βασική εκτέλεση. Αν γίνει αλλαγή στο dataset ή στο mapping, το Turtle μπορεί να αναπαραχθεί με διαθέσιμο RML processor του περιβάλλοντος.

## Σημειώσεις για το Ontology

Το `ontology.owl` είναι σε OWL Functional Syntax. Ο Java uploader το αναγνωρίζει και το παραλείπει αυτόματα, επειδή το RDF4J auto-upload περιμένει Turtle ή RDF/XML μορφή για ontology files.

Αυτό δεν εμποδίζει τη φόρτωση του `output/generated.ttl`. Αν χρειάζεται reasoning με την ontology μέσα στο GraphDB, το ontology μπορεί να φορτωθεί χειροκίνητα ή να εξαχθεί σε Turtle/RDFXML ως:

```text
ontology.ttl
```

ή:

```text
ontology.rdf
```

## Troubleshooting

### `Connection refused: localhost:7200`

Το GraphDB δεν τρέχει ή δεν είναι διαθέσιμο στο `http://localhost:7200`. Άνοιξε πρώτα το GraphDB Workbench και μετά ξανατρέξε τον Java uploader.

### `Repository not found`

Δεν υπάρχει repository με ID `MiniProjekt`. Δημιούργησέ το από το GraphDB Workbench ή δώσε διαφορετικό repository ID στο δεύτερο argument του uploader.

### `TTL file not found`

Το `output/generated.ttl` δεν υπάρχει ή το command εκτελείται από λάθος φάκελο. Βεβαιώσου ότι βρίσκεσαι στο project root, δηλαδή στον φάκελο που περιέχει το `pom.xml`.

### `Skipped ontology.owl`

Δεν είναι σφάλμα. Σημαίνει ότι το ontology είναι σε OWL Functional Syntax και δεν φορτώνεται αυτόματα σε αυτή τη μορφή από τον uploader.

### `mvn` is not recognized

Το Maven δεν είναι διαθέσιμο στο περιβάλλον εκτέλεσης. Σε αυτή την περίπτωση μπορεί να γίνει import του `output/generated.ttl` απευθείας από το GraphDB Workbench, χωρίς να αλλάξει κάτι στα δεδομένα ή στα queries.

## Σειρά Παρουσίασης

Για παρουσίαση ή αναφορά, η προτεινόμενη σειρά είναι:

1. Περιγραφή dataset και στόχου.
2. Περιγραφή preprocessing.
3. Περιγραφή ontology.
4. Περιγραφή RML mapping.
5. Παραγωγή RDF/Turtle.
6. Φόρτωση σε GraphDB με Java.
7. Εκτέλεση SPARQL queries.
8. Παρουσίαση αποτελεσμάτων.
