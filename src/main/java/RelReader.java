import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;


/*
 * This class uses code from the demos in the neo4j documentation:
 * https://github.com/neo4j/neo4j/blob/2.2.5/community/embedded-examples/src/main/java/org/neo4j/examples/EmbeddedNeo4j.java
 * 
 */
public class RelReader {
  File DB_DIR = null;
  
  public RelReader() throws IOException{
    DB_DIR = Files.createTempDirectory("neo4j").toFile();
  }
  
  public static enum RelTypes implements RelationshipType{
    ISA, INVERSE_ISA
  }
  
  public GraphDatabaseService buildGraph(String relsFile) throws FileNotFoundException{
    GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_DIR);
    registerShutdownHook(graphDb);
    Index<Node> index = null;
    Index<Relationship> rIndex = null; 
    
    try(Scanner scanner = new Scanner(new File(relsFile)); Transaction tx = graphDb.beginTx(); ) {
      index = graphDb.index().forNodes("nodes");
      rIndex = graphDb.index().forRelationships("relationships");
      while(scanner.hasNextLine()){
        String[] fields = scanner.nextLine().trim().split("\\|");
        String cui1 = fields[0];
        String cui2 = fields[4];
        String relType = fields[7];
        assert (relType.equals("isa"));

        Node arg1 = createOrFindNode(graphDb, cui1, fields[10]);
        Node arg2 = createOrFindNode(graphDb, cui2, fields[10]);
        
        index.putIfAbsent(arg1, "cui", cui1);
        index.putIfAbsent(arg2, "cui", cui2);
        
        Relationship rel = arg1.createRelationshipTo(arg2, RelTypes.ISA);
        Relationship rel2 = arg2.createRelationshipTo(arg1, RelTypes.INVERSE_ISA);
        rIndex.add(rel, "category", RelTypes.ISA);
        rIndex.add(rel2, "category", RelTypes.INVERSE_ISA);
      }
      tx.success();
    }
    return graphDb;
  }
  
  private Node createOrFindNode(GraphDatabaseService graphDb, String cui, String label){
    Node node = graphDb.findNode(DynamicLabel.label(label), "cui", cui);
    if(node == null){
      node = graphDb.createNode(DynamicLabel.label(label));
      node.setProperty("cui", cui);
    }
    return node;
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
  
  public static void main(String[] args) throws IOException{
    RelReader reader = new RelReader();
    GraphDatabaseService db = reader.buildGraph(args[0]);
    
    
  }
}

