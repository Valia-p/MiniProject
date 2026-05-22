# MiniProject - Semantic Web Soundtracks

Το project μετατρέπει ένα dataset με τραγούδια και ταινίες σε RDF γράφο, τον φορτώνει σε GraphDB και παρέχει έτοιμα SPARQL queries για εξερεύνηση των δεδομένων.

## Στόχος Εργασίας

Στόχος είναι να μοντελοποιηθούν πληροφορίες για soundtracks ταινιών ως σημασιολογικός γράφος. Το project ξεκινά από ένα καθαρισμένο Excel dataset, δημιουργεί ενιαία JSON δομή, παράγει RDF triples με βάση RML mapping και τελικά φορτώνει τα δεδομένα σε GraphDB ώστε να μπορούν να απαντηθούν ερωτήματα με SPARQL.

Η βασική ροή είναι:

```text
Excel/CSV dataset
  -> Python preprocessing
  -> JSON dataset
  -> RML mapping
  -> generated Turtle RDF
  -> GraphDB repository
  -> SPARQL queries
```

## Τεχνολογίες

- Python 3 για καθαρισμό και μετατροπή του αρχικού dataset.
- Pandas για ανάγνωση CSV/Excel αρχείων.
- RML/R2RML mapping για παραγωγή RDF triples.
- RDF/Turtle ως τελική μορφή εξόδου.
- Java 17 με RDF4J για φόρτωση δεδομένων στο GraphDB.
- Maven για build και εκτέλεση του Java uploader.
- GraphDB για αποθήκευση RDF γράφου και εκτέλεση SPARQL queries.

## Δομή Project

```text
MiniProject/
├── common-mapping.json
├── ontology.owl
├── pom.xml
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

## Τεκμηρίωση Αρχείων

| Αρχείο | Περιγραφή |
| --- | --- |
| `.gitignore` | Δηλώνει ποια αρχεία δεν πρέπει να μπαίνουν στο Git. Αγνοεί το `output/generated.ttl` και raw datasets σε `data/raw/`, επειδή είναι παραγόμενα ή μεγάλα δεδομένα. |
| `pom.xml` | Maven configuration του Java project. Ορίζει Java 17, RDF4J dependencies, Turtle parser και το `exec-maven-plugin` με main class το `GraphDBUploader`. |
| `src/main/java/GraphDBUploader.java` | Java πρόγραμμα που συνδέεται στο GraphDB, προαιρετικά καθαρίζει το repository, φορτώνει ontology αν είναι σε υποστηριζόμενη RDF μορφή, φορτώνει το `generated.ttl` και εμφανίζει το πλήθος των triples. |
| `ontology.owl` | OWL ontology του project. Περιέχει κλάσεις όπως `dbo:Film`, `dbo:Song`, `dbo:Person`, `ex:Director`, `ex:Songwriter`, και σχέσεις όπως `ex:hasSong`, `ex:partOfFilm`, `dbo:artist`, `dbo:director`. Είναι σε OWL Functional Syntax, οπότε ο Java uploader το παραλείπει αυτόματα. |
| `common-mapping.json` | Mapping configuration σε JSON μορφή. Περιέχει namespaces, base IRI και κανόνες μετασχηματισμού για πεδία του dataset. Χρησιμεύει ως βοηθητικό/reference mapping. |
| `data/raw/final-cleaned-songs-csv.xls` | Το αρχικό Excel dataset. Περιέχει τις εγγραφές για τραγούδια, ταινίες, contributors, genres, γλώσσες, χώρες παραγωγής και metadata ταινιών. |
| `scripts/convert_dataset_to_json.py` | Python script που βρίσκει το πρώτο CSV/XLS/XLSX μέσα στο `data/raw/`, καθαρίζει τα ονόματα στηλών, χειρίζεται null values, σπάει multi-value πεδία σε λίστες και δημιουργεί slugs για IRIs. |
| `data/processed/soundtracks_clean.json` | Καθαρισμένο JSON dataset που παράγεται από το Python script. Είναι η κύρια είσοδος για το RML mapping. |
| `data/processed/columns_profile.json` | Metadata για τη μετατροπή του dataset. Περιέχει source file, αριθμό γραμμών, mapping αρχικών/καθαρισμένων στηλών, multi-value columns και output files. |
| `data/processed/soundtracks_structure.json` | Περιγραφικό αρχείο δομής των πεδίων του καθαρισμένου dataset. Δείχνει τύπους πεδίων, nullable πεδία και παραδείγματα δομής. |
| `mappings/mapping.ttl` | RML mapping σε Turtle. Δηλώνει πώς το `soundtracks_clean.json` μετατρέπεται σε RDF πόρους όπως songs, films, genres, countries, languages, companies και people roles. |
| `output/generated.ttl` | Το τελικό παραγόμενο RDF αρχείο σε Turtle. Αυτό φορτώνεται στο GraphDB. Περιέχει triples για τραγούδια, ταινίες, contributors και τις σχέσεις μεταξύ τους. |
| `queries/sparql_queries.txt` | Έτοιμα SPARQL queries για έλεγχο και παρουσίαση του γράφου. Περιλαμβάνει queries για films/songs/artists, genres/countries, directors/soundtracks, inverse properties και άτομα με πολλαπλούς ρόλους. |
| `target/classes/GraphDBUploader.class` | Compiled Java class που παράγεται από το Maven build. Δεν χρειάζεται χειροκίνητη επεξεργασία. |
| `target/maven-status/...` | Maven build metadata. Δημιουργείται αυτόματα κατά το compile και δεν είναι μέρος της λογικής του project. |

## Βήματα που Ακολουθήθηκαν

1. Καθαρίστηκαν και κανονικοποιήθηκαν οι στήλες του αρχικού dataset.
2. Τα πεδία με πολλαπλές τιμές, όπως artists, genres, countries και contributors, μετατράπηκαν σε JSON arrays.
3. Δημιουργήθηκαν slugs για σταθερά IRIs, ώστε κάθε song, film, person, genre ή company να έχει προβλέψιμο URI.
4. Γράφτηκε RML mapping που μετατρέπει το JSON σε RDF triples.
5. Το παραγόμενο Turtle αρχείο φορτώθηκε σε GraphDB repository.
6. Δημιουργήθηκαν SPARQL queries για έλεγχο του γράφου και παρουσίαση σχέσεων.

## Προαπαιτούμενα

Πριν τρέξει όλο το project χρειάζονται:

- Python 3.
- Java 17.
- Maven.
- GraphDB Desktop ή GraphDB Server.
- Ένα GraphDB repository με ID `MiniProjekt`.

Για το Python preprocessing χρειάζονται οι βιβλιοθήκες:

```powershell
pip install pandas xlrd openpyxl
```

## Πώς να Τρέξει το Project

Άνοιξε PowerShell μέσα στον φάκελο:

```powershell
cd C:\Users\LetsM\OneDrive\Desktop\MiniProjekt\MiniProject
```

### 1. Μετατροπή raw dataset σε JSON

Τρέξε:

```powershell
python scripts\convert_dataset_to_json.py
```

Αυτό διαβάζει το αρχείο από το `data/raw/` και δημιουργεί:

```text
data/processed/soundtracks_clean.json
data/processed/columns_profile.json
```

Αναμενόμενη έξοδος:

```text
Input: data\raw\final-cleaned-songs-csv.xls
Rows: 5288
Multi-value columns: ...
Output: data\processed\soundtracks_clean.json
Profile: data\processed\columns_profile.json
```

### 2. Παραγωγή RDF Turtle

Το project περιέχει ήδη το αρχείο:

```text
output/generated.ttl
```

Αν χρειαστεί να το ξαναδημιουργήσεις από το RML mapping, χρησιμοποίησε έναν RML processor, για παράδειγμα RMLMapper:

```powershell
java -jar rmlmapper.jar -m mappings\mapping.ttl -o output\generated.ttl
```

Το mapping που χρησιμοποιείται είναι:

```text
mappings/mapping.ttl
```

Η είσοδος του mapping είναι:

```text
data/processed/soundtracks_clean.json
```

### 3. Εκκίνηση GraphDB

1. Εγκατέστησε και άνοιξε το GraphDB Desktop.
2. Άνοιξε στον browser:

```text
http://localhost:7200
```

3. Πήγαινε στο `Setup` -> `Repositories`.
4. Δημιούργησε νέο repository με ID:

```text
MiniProjekt
```

5. Άφησε τις υπόλοιπες ρυθμίσεις στις default τιμές, εκτός αν η εργασία ζητά συγκεκριμένο ruleset.

### 4. Build του Java uploader

Τρέξε:

```powershell
mvn clean compile
```

Αν εμφανιστεί ότι το `mvn` δεν αναγνωρίζεται, τότε το Maven δεν είναι εγκατεστημένο ή δεν είναι στο PATH.

### 5. Φόρτωση RDF στο GraphDB

Προτεινόμενη εκτέλεση:

```powershell
mvn exec:java "-Dexec.args=http://localhost:7200 MiniProjekt output/generated.ttl false"
```

Τα arguments είναι:

```text
1ο argument: GraphDB base URL
2ο argument: Repository ID
3ο argument: Path του Turtle αρχείου
4ο argument: clearBeforeLoad
```

Το τελευταίο argument είναι σημαντικό:

- `false`: δεν σβήνει τα υπάρχοντα triples πριν φορτώσει το αρχείο.
- `true`: καθαρίζει πρώτα όλο το repository και μετά φορτώνει το αρχείο.

Αν τρέξεις το πρόγραμμα χωρίς arguments, χρησιμοποιεί τις προεπιλογές:

```text
GraphDB URL: http://localhost:7200
Repository: MiniProjekt
TTL file: output/generated.ttl
clearBeforeLoad: false
```

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

### Εναλλακτική Εκτέλεση χωρίς Maven

Αν το `mvn` δεν είναι διαθέσιμο στο PATH αλλά υπάρχει Java 17 και τα dependencies έχουν ήδη κατέβει στο local Maven cache, μπορείς να κάνεις compile και run με PowerShell:

```powershell
$jars = Get-ChildItem -Recurse -Filter *.jar "$env:USERPROFILE\.m2\repository" | Select-Object -ExpandProperty FullName
$cp = $jars -join ";"
& "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot\bin\javac.exe" -cp $cp -d target\classes src\main\java\GraphDBUploader.java
$runCp = (@("target\classes") + $jars) -join ";"
& "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot\bin\java.exe" -cp $runCp GraphDBUploader
```

## Εκτέλεση SPARQL Queries

Αφού φορτωθούν τα δεδομένα:

1. Άνοιξε το GraphDB Workbench στο `http://localhost:7200`.
2. Επίλεξε το repository `MiniProjekt`.
3. Πήγαινε στην καρτέλα SPARQL.
4. Άνοιξε το αρχείο:

```text
queries/sparql_queries.txt
```

5. Κάνε copy/paste ένα query και πάτησε Run.

Τα διαθέσιμα queries είναι:

| Query | Σκοπός |
| --- | --- |
| Query 1 | Εμφανίζει ταινίες, τραγούδια και artists. |
| Query 2 | Εμφανίζει ταινίες ανά genre και χώρα παραγωγής. |
| Query 3 | Συνδέει directors με films και soundtracks. |
| Query 4 | Ελέγχει τη σχέση `ex:partOfFilm` ανάμεσα σε song και film. |
| Query 5 | Βρίσκει άτομα που εμφανίζονται με πολλαπλούς ρόλους, π.χ. artist, composer, director, actor. |

## Σημειώσεις για το Ontology

Το αρχείο `ontology.owl` είναι σε OWL Functional Syntax. Ο Java uploader μπορεί να ανεβάσει αυτόματα ontology μόνο αν βρει:

```text
ontology/ontology.ttl
ontology.ttl
ontology/ontology.rdf
ontology.rdf
ontology/ontology.owl
ontology.owl
```

Όμως για `.owl` περιμένει RDF/XML μορφή. Επειδή το τωρινό `ontology.owl` ξεκινά με `Prefix(` και `Ontology(`, αναγνωρίζεται ως OWL Functional Syntax και παραλείπεται.

Αν θέλεις να φορτώνεται αυτόματα το ontology, μπορείς να το εξάγεις από Protege σε Turtle ή RDF/XML και να το αποθηκεύσεις ως:

```text
ontology.ttl
```

ή:

```text
ontology.rdf
```

## Troubleshooting

### `mvn` is not recognized

Το Maven δεν είναι εγκατεστημένο ή δεν είναι στο PATH. Εγκατέστησε Maven και άνοιξε ξανά το terminal.

### `java version "1.8..."`

Το project θέλει Java 17. Αν το PATH δείχνει σε Java 8, εγκατέστησε Java 17 και βάλε το στο PATH.

### `Connection refused: localhost:7200`

Το GraphDB δεν τρέχει. Άνοιξε GraphDB Desktop και έλεγξε ότι το Workbench ανοίγει στο:

```text
http://localhost:7200
```

### `Repository not found`

Δεν υπάρχει repository με ID `MiniProjekt`. Δημιούργησέ το από το GraphDB Workbench.

### `TTL file not found`

Το `output/generated.ttl` δεν υπάρχει ή το path είναι λάθος. Δημιούργησέ το ξανά από το RML mapping ή δώσε σωστό path στο τρίτο argument.

### `Skipped ontology.owl`

Δεν είναι σφάλμα. Σημαίνει ότι το ontology είναι σε OWL Functional Syntax και δεν μπορεί να φορτωθεί αυτόματα από τον uploader σε αυτή τη μορφή.

## Προβλήματα και Σημειώσεις

- Το GraphDB πρέπει να τρέχει πριν εκτελεστεί ο Java uploader, αλλιώς εμφανίζεται `Connection refused`.
- Το repository ID πρέπει να είναι `MiniProjekt`, επειδή αυτό χρησιμοποιείται ως default από το πρόγραμμα.
- Το ontology υπάρχει στο project, αλλά για αυτόματο upload χρειάζεται εξαγωγή σε Turtle ή RDF/XML.
- Το `output/generated.ttl` είναι παραγόμενο αρχείο και μπορεί να ξαναδημιουργηθεί από το RML mapping.
- Το `target/` είναι build output του Maven/Java και δεν χρειάζεται να εκδίδεται ως source code.

## Προτεινόμενη Σειρά Παρουσίασης

Για README ή αναφορά εργασίας, η λογική σειρά είναι:

1. Περιγραφή dataset και στόχου.
2. Περιγραφή ontology.
3. Περιγραφή preprocessing.
4. Περιγραφή RML mapping.
5. Παραγωγή RDF.
6. Φόρτωση σε GraphDB.
7. Εκτέλεση SPARQL queries.
8. Παραδείγματα αποτελεσμάτων από GraphDB.
