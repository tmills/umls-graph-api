import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

public class GraphFunctions {
  static GraphDatabaseService graphDb = null;
  
  static{
    graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File("neo4j"));
  }
  
  public static List<String> getHypernyms(String cui){
    List<String> hypers = new ArrayList<>();
    
    try ( Transaction tx = graphDb.beginTx() ){
      TraversalDescription td = graphDb.traversalDescription()
          .breadthFirst()
          .relationships(RelReader.RelTypes.ISA, Direction.OUTGOING)
          .evaluator(Evaluators.excludeStartPosition());

      Node cuiNode = graphDb.findNode(RelReader.DictLabels.Concept, RelReader.CUI_PROPERTY, cui);
      if(cuiNode == null) return hypers;
      
      Traverser traverser = td.traverse(cuiNode);
      for(Path path : traverser){
        hypers.add(path.endNode().getProperty(RelReader.CUI_PROPERTY).toString());
      }
      tx.success();
    }
    return hypers;
  }
  
  public static List<String> getAncestors(String cui){
    return getHypernyms(cui);
  }
  
  public static List<String> getHyponyms(String cui){
    List<String> hypos = new ArrayList<>();
    
    try ( Transaction tx = graphDb.beginTx() ){
      TraversalDescription td = graphDb.traversalDescription()
          .breadthFirst()
          .relationships(RelReader.RelTypes.ISA, Direction.INCOMING)
          .evaluator(Evaluators.excludeStartPosition());

      Node cuiNode = graphDb.findNode(RelReader.DictLabels.Concept, RelReader.CUI_PROPERTY, cui);
      if(cuiNode == null) return hypos;
      
      Traverser traverser = td.traverse(cuiNode);
      for(Path path : traverser){
        hypos.add(path.endNode().getProperty(RelReader.CUI_PROPERTY).toString());
      }
      tx.success();
    }
    return hypos;    
  }
  
  public static List<String> getDescendents(String cui){
    return getHyponyms(cui);
  }
  
  public static boolean isa(String cui1, String cui2){
    boolean match=false;
    try ( Transaction tx = graphDb.beginTx() ){
      Node cui1Node = graphDb.findNode(RelReader.DictLabels.Concept, RelReader.CUI_PROPERTY, cui1);
      Node cui2Node = graphDb.findNode(RelReader.DictLabels.Concept, RelReader.CUI_PROPERTY, cui2);
      if(cui1Node == null || cui2Node == null) return match;
      
      TraversalDescription td = graphDb.traversalDescription()
          .breadthFirst()
          .relationships(RelReader.RelTypes.ISA, Direction.OUTGOING)
          .evaluator(Evaluators.excludeStartPosition())
          .evaluator(Evaluators.includeWhereEndNodeIs(cui2Node));

      Traverser traverser = td.traverse(cui1Node);
      if(traverser.iterator().hasNext()){
        match = true;
      }
      tx.success();
    }
    return match;
  }
}
