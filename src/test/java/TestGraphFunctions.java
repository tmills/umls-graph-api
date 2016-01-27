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
import org.chboston.cnlp.graphapi.GraphFunctions;
import org.junit.Assert;
import org.junit.Test;

public class TestGraphFunctions {
  public static final String COLON_TUMOR_CUI = "C0007102";
  public static final String FINDING_CUI = "C3662248";
  
  @Test
  public void testHypernyms(){
    
    for(String cui : GraphFunctions.getAncestors(COLON_TUMOR_CUI)){
      System.out.println("Ancestor (hypernym) is: " + cui);
    }
    
    for(String cui : GraphFunctions.getDescendents(COLON_TUMOR_CUI)){
      System.out.println("Descendent (hyponym) is: " + cui);      
    }
    
    Assert.assertTrue(GraphFunctions.isa(COLON_TUMOR_CUI, FINDING_CUI));
    Assert.assertFalse(GraphFunctions.isa(FINDING_CUI, COLON_TUMOR_CUI));
    
  }
}
