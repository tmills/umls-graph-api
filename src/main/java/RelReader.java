import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;


/*
 * This class uses code from the demos in the neo4j documentation:
 * https://github.com/neo4j/neo4j/blob/2.2.5/community/embedded-examples/src/main/java/org/neo4j/examples/EmbeddedNeo4j.java
 * 
 */
public class RelReader {
  GraphDatabaseService graphDb;
  String DB_PATH = "";
  
  private static enum RelTypes implements RelationshipType{
    ISA, INVERSE_ISA
  }
  
  public void buildGraph(String relsFile) throws FileNotFoundException{
    graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
    registerShutdownHook(graphDb);
    
    try(Scanner scanner = new Scanner(new File(relsFile)); Transaction tx = graphDb.beginTx(); ) {
      while(scanner.hasNextLine()){
        String[] fields = scanner.nextLine().trim().split("|");
        String cui1 = fields[0];
        String cui2 = fields[4];
        String relType = fields[7];
        assert (relType.equals("isa"));

        Node arg1 = graphDb.createNode();
        arg1.setProperty("cui", cui1);
        Node arg2 = graphDb.createNode();
        arg2.setProperty("cui", cui2);
        Relationship rel = arg1.createRelationshipTo(arg2, RelTypes.ISA);
        Relationship rel2 = arg2.createRelationshipTo(arg1, RelTypes.INVERSE_ISA);
        
        tx.success();
      }
    }
  }
  
  private void registerShutdownHook( final GraphDatabaseService graphDb )
  {
      // Registers a shutdown hook for the Neo4j instance so that it
      // shuts down nicely when the VM exits (even if you "Ctrl-C" the
      // running application).
      Runtime.getRuntime().addShutdownHook( new Thread()
      {
          @Override
          public void run()
          {
              graphDb.shutdown();
          }
      } );
  }
}

