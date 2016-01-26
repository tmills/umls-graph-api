package org.chboston.cnlp.graphbuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;


/*
 * This class uses code from the demos in the neo4j documentation:
 * https://github.com/neo4j/neo4j/blob/2.2.5/community/embedded-examples/src/main/java/org/neo4j/examples/EmbeddedNeo4j.java
 * 
 */
public class RelReader {
  File DB_DIR = null;
  
  public RelReader() throws IOException{
    this("neo4j/");
  }
  
  public RelReader(String location){
    DB_DIR = new File(location);
  }
  
  public static final String CUI_PROPERTY = "cui";
  
  public static enum DictLabels implements Label{
    Concept
  }
  public static enum RelTypes implements RelationshipType{
    ISA, INVERSE_ISA
  }
  
  public void batchBuildGraph(File umlsDir, String tuisFilename, String... sources) throws IOException{
    Set<String> tuiSet = getTuisFromFile(tuisFilename);
    Set<String> cuiSet = getCuisFromUmls(umlsDir, tuiSet);
    Set<String> sourceSet = getSourceSet(sources);

    BatchInserter inserter = null;
    Label conceptLabel = DictLabels.Concept;
    Map<String, Object> properties = new HashMap<>();
    
    try{
      inserter = BatchInserters.inserter( DB_DIR );
      inserter.createDeferredSchemaIndex( conceptLabel ).on( CUI_PROPERTY ).create();

      File relsFile = new File(umlsDir, "MRREL.RRF");
      int line = 0;
      Map<String,Long> insertedCuis = new HashMap<>();

      try( Scanner scanner = new Scanner(relsFile) ) {
        System.out.println("Finding relations between CUIs in cTAKES from selected sources...");
        while(scanner.hasNextLine()){
          if(++line % 10000 == 0){
            System.out.print(".");
          }
          String[] fields = scanner.nextLine().trim().split("\\|");
          String cui1 = fields[0];
          String cui2 = fields[4];
          // both cuis in relation must be of interest to us:
          if(!cuiSet.contains(cui1) || !cuiSet.contains(cui2)) continue;

          String relType = fields[7];
          // only interested in isa for now
          if(!relType.equals("isa")) continue;

          // only use specified dictionaries (e.g., SNOMEDCT_US)
          if(!sourceSet.contains(fields[10])) continue;

          long id1,id2;
          if(!insertedCuis.containsKey(cui1)){
            properties.put("cui", cui1);
            long id = inserter.createNode(properties, conceptLabel);
            insertedCuis.put(cui1, id);
          }
          id1 = insertedCuis.get(cui1);

          if(!insertedCuis.containsKey(cui2)){
            properties.put("cui", cui2);
            long id = inserter.createNode(properties, conceptLabel);
            insertedCuis.put(cui2, id);
          }
          id2 = insertedCuis.get(cui2);

          inserter.createRelationship(id2, id1, RelReader.RelTypes.ISA, null);            
        }
      }
    }
    finally{
      if ( inserter != null ){
        inserter.shutdown();
      }
    }
  }
    
  private Set<String> getSourceSet(String[] sources) {
    Set<String> sourceSet = new HashSet<>();
    for(String source : sources){
      sourceSet.add(source);
    }
    return sourceSet;
  }

  private Set<String> getCuisFromUmls(File umlsDir, Set<String> tuiSet) throws FileNotFoundException {
    System.out.println("Filtering UMLS to CUIs with cTAKES-included TUIs");
    Set<String> cuis = new HashSet<>();
    int lines = 0;
    File mrSty = new File(umlsDir, "MRSTY.RRF");
    try(Scanner scanner = new Scanner(mrSty)){
      String line = null;
      while(scanner.hasNextLine()){
        if(++lines % 10000 == 0) System.out.print('.');
        line = scanner.nextLine().trim();
        String[] fields = line.split("\\|");
        if(tuiSet.contains(fields[1])){
          cuis.add(fields[0]);
        }
      }
      System.out.println();
    }
    return cuis;
  }

  private Set<String> getTuisFromFile(String tuisFilename) throws FileNotFoundException {
    Set<String> tuis = new HashSet<>();
    try(Scanner scanner = new Scanner(new File(tuisFilename))){
      String line = null;
      while(scanner.hasNextLine()){
        line = scanner.nextLine().trim();
        if(line.startsWith("T")){
          tuis.add(line);
        }
      }
    }
    return tuis;
  }
  
  public static void main(String[] args) throws IOException{
    if(args.length < 1){
      System.err.println("Error: One required argument: UMLS META directory.");
      System.exit(-1);
    }
    
    RelReader reader = new RelReader();
    reader.batchBuildGraph(new File(args[0]), "CtakesAllTuis.txt", "SNOMEDCT_US");
  }
}

