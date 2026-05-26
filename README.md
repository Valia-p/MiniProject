# MiniProject - RML Mapping + RDF4J + GraphDB

## 1. Σκοπός Project
Το project υλοποιεί ένα πλήρες pipeline σε Java για:
1. Μετασχηματισμό δεδομένων soundtrack από JSON σε RDF/Turtle με RML.
2. Φόρτωση ontology και RDF δεδομένων σε GraphDB repository `MiniProject`.
3. Εκτέλεση SPARQL queries και αποθήκευση αποτελεσμάτων σε αρχείο `.txt`.

Η ροή που καλύπτει το project είναι:

```text
Raw dataset (Excel)
  -> preprocessing (Python, optional)
  -> cleaned JSON
  -> RML mapping
  -> generated TTL
  -> GraphDB upload
  -> SPARQL execution
  -> query_results.txt
```

## 2. Κύρια Αρχεία και Ρόλοι
1. `data/raw/final-cleaned-songs-csv.xls`: αρχικό dataset.
2. `data/processed/soundtracks_clean.json`: καθαρισμένο dataset που διαβάζει το mapping.
3. `data/processed/columns_profile.json`: profile του preprocessing (rows, columns, mappings).
4. `mappings/mapping.ttl`: RML mapping κανόνες.
5. `output/generated.ttl`: RDF/Turtle που παράγεται από το mapping.
6. `queries/sparql_queries.txt`: τα SPARQL queries που εκτελούνται.
7. `output/query_results.txt`: αποθηκευμένα αποτελέσματα των queries.
8. `ontology.owl`: ontology σε OWL Functional Syntax.
9. `ontology.rdf`: ontology σε RDF/XML (φορτώνεται αυτόματα από uploader).
10. `src/main/java/RMLMapping.java`: εκτέλεση RML και παραγωγή TTL.
11. `src/main/java/GraphDBUploader.java`: φόρτωση ontology + TTL στο GraphDB.
12. `src/main/java/QueryRunner.java`: εκτέλεση queries και export σε txt.
13. `src/main/java/Pipeline.java`: ενιαία ροή (mapping -> upload -> queries).
14. `pom.xml`: dependencies, Maven plugins, και έτοιμα execution profiles.

## 3. Προαπαιτούμενα
1. Java 17
2. Maven 3.x
3. GraphDB σε λειτουργία στο `http://localhost:7200`
4. Repository στο GraphDB με ακριβές ID: `MiniProject`

## 4. GraphDB Setup (πριν τα commands)
1. Άνοιξε GraphDB Workbench στο `http://localhost:7200`.
2. Δημιούργησε repository με όνομα/ID `MiniProject`.
3. Ενεργοποίησε reasoning ruleset στο repository (όχι `empty`), αν θέλεις να φαίνονται και inferred αποτελέσματα στο Query 4.

## 5. Εκτέλεση - Γρήγορη Έναρξη
Αν θέλεις με τις default ρυθμίσεις να τρέξουν όλα:

```powershell
cd C:\Users\vaspn\Downloads\MiniProject
mvn clean compile
mvn exec:java
```

Η `mvn exec:java` τρέχει την `Pipeline` class με σειρά:
1. RML mapping
2. Upload σε GraphDB
3. SPARQL queries

## 6. Εκτέλεση - Βήμα Βήμα (αναλυτικά)

### 6.1 Compile
```powershell
cd C:\Users\vaspn\Downloads\MiniProject
mvn clean compile
```

### 6.2 Μόνο RML Mapping (JSON -> TTL)
```powershell
mvn "exec:java@run-rml"
```

Τι κάνει:
1. Διαβάζει `mappings/mapping.ttl`.
2. Διαβάζει `data/processed/soundtracks_clean.json`.
3. Παράγει/ενημερώνει το `output/generated.ttl`.

### 6.3 Μόνο Upload στο GraphDB
```powershell
mvn "exec:java@run-upload"
```

Τι κάνει:
1. Συνδέεται στο `http://localhost:7200`, repository `MiniProject`.
2. Κάνει `clear` στο repository.
3. Φορτώνει ontology από `ontology.rdf` (ή `ontology.ttl` αν υπάρχει).
4. Φορτώνει το `output/generated.ttl`.
5. Τυπώνει triple count.

### 6.4 Μόνο SPARQL Queries
```powershell
mvn "exec:java@run-queries"
```

Τι κάνει:
1. Διαβάζει `queries/sparql_queries.txt`.
2. Εκτελεί τα queries στο repository `MiniProject`.
3. Τυπώνει αποτελέσματα στο terminal.
4. Αποθηκεύει αποτελέσματα στο `output/query_results.txt`.

## 7. Εναλλακτικές Ροές Χρήσης

### 7.1 Θέλω μόνο να ξανατρέξω queries
```powershell
mvn "exec:java@run-queries"
```

### 7.2 Θέλω μόνο να ξαναφορτώσω το TTL σε GraphDB
```powershell
mvn "exec:java@run-upload"
```

### 7.3 Θέλω να ξαναφτιάξω το processed JSON από raw Excel (optional)
```powershell
python scripts\convert_dataset_to_json.py
```

Μετά από αυτό, τρέχεις:
```powershell
mvn "exec:java@run-rml"
mvn "exec:java@run-upload"
mvn "exec:java@run-queries"
```

## 8. Σημαντικές Συμπεριφορές
1. `mvn "exec:java@run-upload"`: κάνει clear επειδή στο `pom.xml` το 4ο argument είναι `true`.
2. `mvn exec:java` (Pipeline default): δεν κάνει clear, επειδή στο `Pipeline` το default `clearBeforeLoad` είναι `false`.
3. Αν υπάρχει μόνο `ontology.owl` σε Functional Syntax, ο uploader το παραλείπει αυτόματα.
4. Αν φορτώνεται `ontology.rdf`, η ontology μπαίνει κανονικά μαζί με τα data triples.

## 9. Query Set που Περιλαμβάνεται
Το `queries/sparql_queries.txt` περιέχει:
1. Films, Songs, Artists
2. Films by Genre and Production Country
3. Directors and Soundtracks
4. Reasoning via inverse property (`ex:actedIn` από `dbo:starring`)
5. People with Multiple Roles

## 10. Έλεγχος Επιτυχίας
Μετά από πλήρη εκτέλεση ελέγχεις:
1. `output/generated.ttl` υπάρχει και έχει περιεχόμενο.
2. `output/query_results.txt` υπάρχει και περιέχει αποτελέσματα για όλα τα queries.
3. Στο terminal εμφανίζεται:
   1. επιτυχής ολοκλήρωση mapping
   2. επιτυχής σύνδεση/φόρτωση GraphDB
   3. εκτέλεση queries με `Rows returned: ...`

## 11. Troubleshooting

### 11.1 `Repository not found`
Αιτία: δεν υπάρχει repository `MiniProject`.
Λύση: δημιούργησέ το στο GraphDB Workbench.

### 11.2 `Connection refused` ή timeout στο `localhost:7200`
Αιτία: GraphDB δεν τρέχει.
Λύση: ξεκίνα GraphDB και ξανατρέξε upload/queries.

### 11.3 `TTL file not found`
Αιτία: δεν έχει παραχθεί το `output/generated.ttl`.
Λύση: τρέξε πρώτα `mvn "exec:java@run-rml"`.

### 11.4 `songwriter_slug[*] did not provide any results` warnings
Αιτία: σε πολλές εγγραφές το πεδίο είναι κενό στο dataset.
Σημείωση: είναι αναμενόμενο data warning και δεν σημαίνει αποτυχία mapping.

### 11.5 `Target host is not specified`
Αιτία: λάθος arguments σειρά όταν τρέχει χειροκίνητα class με custom args.
Λύση: χρησιμοποίησε τα έτοιμα executions από `pom.xml` (`run-rml`, `run-upload`, `run-queries`).

## 12. Command Cheat Sheet
Από το project root:

```powershell
mvn clean compile
mvn "exec:java@run-rml"
mvn "exec:java@run-upload"
mvn "exec:java@run-queries"
mvn exec:java
```

## 13. Παραδοτέα που Υπάρχουν στο Repo
1. Dataset (raw + processed)
2. Ontology (`ontology.owl`, `ontology.rdf`)
3. RML mapping (`mappings/mapping.ttl`)
4. Generated TTL (`output/generated.ttl`)
5. SPARQL queries (`queries/sparql_queries.txt`)
6. Java code pipeline (RML + RDF4J + GraphDB + query runner)
7. Query results export (`output/query_results.txt`)
