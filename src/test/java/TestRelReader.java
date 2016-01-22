import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

public class TestRelReader{
  
  @Before
  public void setup(){
    
  }
  
  @Test
  public void testRelReader() throws IOException{
    RelReader reader = new RelReader();
    GraphDatabaseService db = reader.buildGraph("cc_isa.txt");
    try ( Transaction tx = db.beginTx() ){
      TraversalDescription td = db.traversalDescription()
          .breadthFirst()
          .relationships(RelReader.RelTypes.ISA)
          .evaluator(Evaluators.excludeStartPosition());

      Node cuiNode = db.findNode(DynamicLabel.label("SNOMEDCT"), "cui", "C0007102");
      Traverser traverser = td.traverse(cuiNode);
      for(Path path : traverser){
        System.out.println("At depth " + path.length() + " => " + path.endNode().getProperty("cui"));
      }
    }
    db.shutdown();
  }
  
  
}
