/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.chboston.cnlp.graphapi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.chboston.cnlp.graphbuilder.RelReader;
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
  
  public static int minDistance(String cui1, String cui2){
    int distance = -1;
    try ( Transaction tx = graphDb.beginTx() ){
      Node cui1Node = graphDb.findNode(RelReader.DictLabels.Concept, RelReader.CUI_PROPERTY, cui1);
      Node cui2Node = graphDb.findNode(RelReader.DictLabels.Concept, RelReader.CUI_PROPERTY, cui2);
      if(cui1Node == null || cui2Node == null) return distance;
      
      TraversalDescription td = graphDb.traversalDescription()
          .breadthFirst()
          .relationships(RelReader.RelTypes.ISA, Direction.OUTGOING)
          .evaluator(Evaluators.excludeStartPosition())
          .evaluator(Evaluators.includeWhereEndNodeIs(cui2Node));

      Traverser traverser = td.traverse(cui1Node);
      for(Path path : traverser){
        int len = path.length();
        if(distance == -1 || len < distance){
          distance = len;
        }
      }
      tx.success();
    }
    return distance;
  }
}
