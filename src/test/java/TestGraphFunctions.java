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
