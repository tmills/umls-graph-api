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

import java.io.File;
import java.io.IOException;

import org.chboston.cnlp.graphbuilder.RelReader;
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
