import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.io.fs.FileUtils;

public class TestRelReader{

  String neo4jLocation = "testNeo4j";
  
  @Before
  public void setup(){
    File dbLoc = new File(neo4jLocation);
    if(dbLoc.exists()){
      throw new RuntimeException("Temporary database location exists! Not overwriting");
    }
    dbLoc.mkdir();
  }
  
  @After
  public void breakDown() throws IOException{
    FileUtils.deleteRecursively(new File(neo4jLocation));
  }
  
  @Test
  public void testRelReader() throws IOException{
    
    RelReader reader = new RelReader(neo4jLocation);
    reader.batchBuildGraph(new File("my_test_umls/"), "CtakesAllTuis.txt", "SNOMEDCT_US");
    GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(neo4jLocation));
    
    try ( Transaction tx = db.beginTx() ){
      TraversalDescription td = db.traversalDescription()
          .breadthFirst()
          .relationships(RelReader.RelTypes.ISA, Direction.INCOMING)
          .evaluator(Evaluators.excludeStartPosition());

      Node cuiNode = db.findNode(RelReader.DictLabels.Concept, RelReader.CUI_PROPERTY, "C0007102");
      Assert.assertNotNull(cuiNode);
      Traverser traverser = td.traverse(cuiNode);
      for(Path path : traverser){
        System.out.println("At depth " + path.length() + " => " + path.endNode().getProperty("cui"));
      }
    }
    db.shutdown();
  }
  
  
}
